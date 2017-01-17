/*
 * This file is part of DBforBix.
 *
 * DBforBix is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DBforBix is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * DBforBix. If not, see <http://www.gnu.org/licenses/>.
 */

package com.smartmarmot.dbforbix.zabbix;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.smartmarmot.common.PersistentDB;
import com.smartmarmot.common.StackSingletonPersistent;
import com.smartmarmot.dbforbix.config.Config;
import com.smartmarmot.dbforbix.config.Config.ZServer;
import com.smartmarmot.dbforbix.zabbix.protocol.Sender32;
import com.smartmarmot.dbforbix.zabbix.protocol.SenderProtocol;

/**
 * Sender query handler
 * 
 * @author Andrea Dalle Vacche
 */
public class ZabbixSender extends Thread {

	public enum PROTOCOL {
		V14, V18, V32
	}

	private static final Logger	LOG					= Logger.getLogger(ZabbixSender.class);

	private Queue<ZabbixItem>	items				= new LinkedBlockingQueue<ZabbixItem>(1000);
	private boolean				terminate			= false;
	private Config.ZServer[]	configuredServers	= new Config.ZServer[0];
	private SenderProtocol		protocol;

	public ZabbixSender(PROTOCOL protVer) {
		super("ZabbixSender");
		switch (protVer) {
			default:
				protocol = new Sender32();
			break;
		}
		setDaemon(true);
	}

	@Override
	public void run() {
		LOG.debug("ZabbixSender - starting sender thread");
		while (!terminate) {
			if (items.peek() == null) {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {}
			}
			else {
				//TODO maxItems should be taken from configuration file
				//take bulk of items to send
				int maxItems=100;
				//ZabbixItem[] itemsReady=new ZabbixItem[maxItems];
				Map<Config.ZServer,Collection<ZabbixItem>> mZServer2ZItems = new HashMap<>();
				for(int i=0;(i<maxItems)&&(items.peek()!=null);++i){
					ZabbixItem nextItem=items.poll();					
					if(nextItem.getValue().isEmpty()) {
						LOG.warn("Item "+nextItem+" has empty value!");
						continue;
					}
					Config.ZServer zs=nextItem.getConfItem().getZServer();
					if(mZServer2ZItems.get(zs)==null) mZServer2ZItems.put(zs, new HashSet<ZabbixItem>());
					mZServer2ZItems.get(zs).add(nextItem);
				}

				for(Entry<ZServer, Collection<ZabbixItem>> m:mZServer2ZItems.entrySet()){
					LOG.debug("ZabbixSender: Sending to " + m.getKey() + " Items[" + m.getValue().size() + "]=" + m.getValue());
				}
				
				Config config = Config.getInstance();
				
				for (Entry<ZServer, Collection<ZabbixItem>> entry : mZServer2ZItems.entrySet()) {					
					ZServer zs=entry.getKey();
					Collection<ZabbixItem> zItems=entry.getValue();
//					Collection<ZabbixItem> zDiscoveries= new HashSet<ZabbixItem>();
//					Collection<ZabbixItem> zHistories = new HashSet<ZabbixItem>();
//
//					// separate Discovery and History data: they should be run in different requests with different types 
//					for(ZabbixItem zItem:zItems){
//						if(zItem.getConfItem() instanceof Discovery){
//							zDiscoveries.add(zItem);
//						}else{
//							zHistories.add(zItem);
//						}
//					}					
					boolean persistent = false;
					for (int i = 0; i < 3; ++i) {
						String resp=new String();
						try {								
							String data = protocol.encodeItems(zItems.toArray(new ZabbixItem[0]));						
							LOG.debug("ZabbixSender[data]: "+data);
							resp=config.requestZabbix(zs.getZServerHost(),zs.getZServerPort(),data);
							LOG.debug("ZabbixSender[resp]: "+resp);
							break;
						}
						catch (Exception ex) {
							LOG.error("ZabbixSender: Error contacting Zabbix server " + zs.getZServerHost() + " - " + ex.getMessage());
							if (persistent){
								LOG.debug("ZabbixSender: Current PeristentStack size ="+StackSingletonPersistent.getInstance().size());
								LOG.info("ZabbixSender - Adding to the persisent stack items: "+zItems);
								PersistentDB.getInstance().add(zItems);
								LOG.debug("ZabbixSender - Current PersistentDB size ="+PersistentDB.getInstance().size());	
							}
						}
						finally{
							persistent = false;
						}
					}
				}
			}
		}
	}

	public void addItem(ZabbixItem item) {
		if (items.size() < 1000)
			items.offer(item);
	}

	synchronized public void updateServerList(Config.ZServer[] newServers) {
		synchronized (configuredServers) {
			configuredServers = newServers;
		}
	}

	public void terminate() {
		terminate = true;
	}
}
