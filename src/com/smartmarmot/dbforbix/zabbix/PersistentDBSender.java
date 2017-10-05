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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.smartmarmot.common.PersistentDB;
import com.smartmarmot.dbforbix.config.ZabbixServer;
import com.smartmarmot.dbforbix.zabbix.protocol.Sender18;
import com.smartmarmot.dbforbix.zabbix.protocol.ISenderProtocol;

/**
 * Sender query handler
 * 
 * @author Andrea Dalle Vacche
 */
public class PersistentDBSender extends Thread {

	public enum PROTOCOL {
		V14, V18
	}

	private static final Logger	LOG					= Logger.getLogger(PersistentDBSender.class);
	private boolean				terminate			= false;
	private ZabbixServer[]	configuredServers	= new ZabbixServer[0];
	private ISenderProtocol		protocol;

	
	public PersistentDBSender(PROTOCOL protVer) {
		super("PersistentDBSender");
		switch (protVer) {
		default:
			protocol = new Sender18();
			break;
		}
		setDaemon(true);
	}

	@Override
	public void run() {
		LOG.debug("PersistentDBSender - starting sender thread");
		while (!terminate) {
			try {
				if (PersistentDB.getInstance().size() == 0L) {
					Thread.sleep(60000);
				}
				else {
					ZabbixServer[] servers;
					synchronized (configuredServers) {
						servers = configuredServers;
					}

					LOG.info("PersistentDBSender - retrieving the first element to send");
					while (PersistentDB.getInstance().size() != 0L ){
						LOG.info("PersistentDBSender - found "+PersistentDB.getInstance().size()+" persistent items to send");
						ZabbixItem zx = (ZabbixItem) PersistentDB.getInstance().pop();
						for (ZabbixServer serverConfig : servers) {
							try {
								Socket zabbix = null;
								OutputStreamWriter out = null;
								InputStream in = null;
								byte[] response = new byte[1024];

								zabbix = new Socket();
								zabbix.setSoTimeout(5000);
								zabbix.connect(new InetSocketAddress(serverConfig.getZServerHost(), serverConfig.getZServerPort()));
								OutputStream os = zabbix.getOutputStream();
								LOG.debug("PersistentDBSender - Sending to " +zx.getHost() + " Item=" + zx.getKey() + " Value=" + zx.getValue());
								String data = protocol.encodeItem(zx);
								out = new OutputStreamWriter(os);
								out.write(data);
								out.flush();

								in = zabbix.getInputStream();
								final int read = in.read(response);
								if (!protocol.isResponeOK(read, response))
									LOG.warn("PersistentDBSender - Received unexpected response '" + new String(response).trim() + "' for key '" + zx.getKey()
									+ "'");
								in.close();
								out.close();
								zabbix.close();
							}			 
							catch (Exception ex) {
								LOG.error("PersistentDBSender - Error contacting Zabbix server " + configuredServers[0].getZServerHost() +" port "+ configuredServers[0].getZServerPort()+ " - " + ex.getMessage());
								LOG.debug("PersistentDBSender - Current PersistentDB size ="+PersistentDB.getInstance().size());
								LOG.info("PersistentDBSender - Adding the item Adding the item="+zx.getHost()+" key="+zx.getKey()+" value="+zx.getValue()+" clock="+zx.getClock()+ " back to the persisent stack");
								PersistentDB.getInstance().push(zx);
								LOG.info("PersistentDBSender - going to sleep for 1 minute");
								Thread.sleep(60000);	
							}

						}
					}
				}
			}
			catch (Exception e) {
				LOG.debug("PersistentDBSender - issue "+e);
			}
		}
	}

	synchronized public void updateServerList(ZabbixServer[] newServers) {
		synchronized (configuredServers) {
			configuredServers = newServers;
		}
	}

	public void terminate() {
		terminate = true;
	}
}
