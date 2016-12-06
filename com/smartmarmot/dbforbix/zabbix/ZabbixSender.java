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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.smartmarmot.common.StackSingletonPersistent;
import com.smartmarmot.dbforbix.config.Config;
import com.smartmarmot.dbforbix.config.Config.Database;
import com.smartmarmot.dbforbix.config.Config.ZServer;
import com.smartmarmot.dbforbix.zabbix.protocol.Sender18;
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
		LOG.debug("ZabbixSender: starting sender thread");
		while (!terminate) {
			if (items.peek() == null) {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {}
			}
			else {
				//take bulk of items to send
				int maxItems=100;
				//ZabbixItem[] itemsReady=new ZabbixItem[maxItems];
				Map<Config.ZServer,Collection<ZabbixItem>> mZS2Items = new HashMap<Config.ZServer, Collection<ZabbixItem> >();
				for(int i=0;(i<maxItems)&&(items.peek()!=null);++i){
					ZabbixItem nextItem=items.poll();
					if(nextItem.getValue().isEmpty()) {
						LOG.warn("Item "+nextItem+" has empty value!");
						continue;
					}
					Config.ZServer zs=nextItem.getConfItem().getZServer();
					if(mZS2Items.get(zs)==null) mZS2Items.put(zs, new HashSet<ZabbixItem>());
					mZS2Items.get(zs).add(nextItem);
				}

//				Config.ZServer[] servers;
//				synchronized (configuredServers) {
//					servers = configuredServers;
//				}
				
				

				for(Entry<ZServer, Collection<ZabbixItem>> m:mZS2Items.entrySet()){
					LOG.debug("ZabbixSender: Sending to " + m.getKey() + " Items[" + m.getValue().size() + "]=" + m.getValue());
				}
				boolean persistent = false;
				Config config = Config.getInstance();
//				Collection<Database> dbs = config.getDatabases();
//				Iterator<Database> iter = dbs.iterator();
//				while (iter.hasNext()){
//					Database myDBItem = iter.next();
//					if (myDBItem.getName().equals(nextItem.getHost())){
//						persistent=myDBItem.getPersistence();
//					}
//				}
				
				for (Config.ZServer zs : mZS2Items.keySet()) {
					for (int i = 0; i < 3; ++i) {
						String resp=new String();
						try {							
							String data = protocol.encodeItem(mZS2Items.get(zs).toArray(new ZabbixItem[0]));						
							LOG.debug("ZabbixSender[data]: "+data);
							resp=config.requestZabbix(zs.getHost(),zs.getPort(),data);
							LOG.debug("ZabbixSender[resp]: "+resp);
							break;
						}
						catch (Exception ex) {
							LOG.error("ZabbixSender: Error contacting Zabbix server " + zs.getHost() + " - " + ex.getMessage());
							if (!persistent){
								//LOG.debug("ZabbixSender: NOTE: the item="+nextItem.getHost()+" is not marked to be persistent, the item will be lost");
							}
							if (persistent){
								LOG.debug("ZabbixSender: Current PeristentStack size ="+StackSingletonPersistent.getInstance().size());
								//LOG.info("ZabbixSender: Adding the item="+nextItem.getHost()+" key="+nextItem.getKey()+" value="+nextItem.getValue()+" clock="+nextItem.getClock()+ " to the persisent stack");
//								try {
//									StackSingletonPersistent.getInstance().push(new ZabbixItem(nextItem.getHost(),nextItem.getKey(),nextItem.getValue(), nextItem.getClock()));
//									LOG.debug("ZabbixSender: Current PeristentStack size ="+StackSingletonPersistent.getInstance().size());
//								} catch (IOException e) {
//									LOG.debug("ZabbixSender: PeristentStack issue "+e);
//								}								
							}
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
