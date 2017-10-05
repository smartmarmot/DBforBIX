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

package com.smartmarmot.dbforbix.scheduler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;
import org.apache.log4j.Logger;

import com.smartmarmot.dbforbix.DBforBix;
import com.smartmarmot.dbforbix.config.element.IConfigurationElement;
import com.smartmarmot.dbforbix.db.DBManager;
import com.smartmarmot.dbforbix.db.DBType;
import com.smartmarmot.dbforbix.db.adapter.DBAdapter;
import com.smartmarmot.dbforbix.db.adapter.DBAdapter.DBNotDefinedException;
import com.smartmarmot.dbforbix.zabbix.ZabbixItem;
import com.smartmarmot.dbforbix.zabbix.ZabbixSender;

/**
 * The item fetching class
 * 
 * @author Andrea Dalle Vacche
 */
public class Scheduler extends TimerTask {

	private static final Logger		LOG		= Logger.getLogger(Scheduler.class);
	private boolean					working	= false;
	private int						pause;

	private Set<IConfigurationElement>	configurationElements = new HashSet<>();

	/**
	 * Creates a new TimeTask for item fetching
	 * 
	 * @param taskGroup
	 *            the schedule group of this worker
	 * @param databaseCfg
	 *            the database config to use
	 */
	public Scheduler(int pause) {
		this.pause = pause;
	}

	public void addConfigurationElement(IConfigurationElement configurationElement) {		
		configurationElements.add(configurationElement);
	}

	public int getPause() {
		return pause;
	}

	@Override
	public void run() {
		if (working)
			return;
		working = true;
		DBManager dbManager = DBManager.getInstance();
		ZabbixSender zabbixSender = DBforBix.getZabbixSender();
		try {
			LOG.debug("Scheduler.run() " + getPause());
			// <configurationUID>:<ConfigurationItem>
			for (IConfigurationElement configurationElement : configurationElements) {
				// <configurationUID> -> DBs monitored
				DBAdapter[] targetDB = dbManager.getDBsByConfigurationUID(configurationElement.getConfigurationItem().getConfigurationUID());
				if (targetDB != null && targetDB.length > 0) {
					for (DBAdapter db : targetDB) {
						try(Connection dbConnection = db.getConnection()) {
							try {
								ZabbixItem[] result = configurationElement.getZabbixItemsData(dbConnection, db.getQueryTimeout());									
								for (ZabbixItem i : result)	zabbixSender.addItem(i);
							}
							catch (NullPointerException e){
								LOG.warn("No data has been returned for item "+ configurationElement.getElementID());
							}
							catch (SQLTimeoutException sqlex) {
								LOG.warn("Timeout after "+db.getQueryTimeout()+"s for item: " + configurationElement.getElementID(), sqlex);
							}
							catch (SQLException sqlex) {
								LOG.warn("could not fetch value of [" + configurationElement.getElementID() +"]\nError code: "+
										sqlex.getErrorCode()+"\nError message: "+sqlex.getLocalizedMessage()+"\n",
										sqlex);
								//propagate error to Zabbix Web interface
								zabbixSender.addItem(
										new ZabbixItem(
												configurationElement.getElementID(),
												"Could not fetch value of [" + configurationElement.getElementID() +"] for db "+ db.getName()+":\n"+sqlex.getLocalizedMessage(),
												ZabbixItem.ZBX_STATE_NOTSUPPORTED,
												new Long(System.currentTimeMillis() / 1000L),
												configurationElement
												)
										);
								//handle ORACLE closed connection exception
								if(DBType.ORACLE==db.getType()
										&& sqlex.getLocalizedMessage().toLowerCase().contains("closed connection"))
									db.reconnect();
							}
						}
						catch(SQLException sqlex){
							LOG.error("Could not get connection to db: " + db.getName(), sqlex);
							zabbixSender.addItem(
									new ZabbixItem(
											configurationElement.getElementID(),
											"Could not connect to DB " + db.getName()+":\n"+sqlex.getLocalizedMessage(),
											ZabbixItem.ZBX_STATE_NOTSUPPORTED,
											new Long(System.currentTimeMillis() / 1000L),
											configurationElement
											)
									);
						}
						catch (DBNotDefinedException nodbex){
							LOG.error(nodbex.getLocalizedMessage());
							zabbixSender.addItem(
									new ZabbixItem(
											configurationElement.getElementID(),
											nodbex.getLocalizedMessage(),
											ZabbixItem.ZBX_STATE_NOTSUPPORTED,
											new Long(System.currentTimeMillis() / 1000L),
											configurationElement
											)
									);
						}
					}
				}
			}
		}
		catch(Exception ex){
			LOG.error("Scheduler exception: " + ex.getLocalizedMessage(),ex);
		}
		catch (Throwable th) {
			LOG.error("Scheduler - Error "+th.getLocalizedMessage());
			th.printStackTrace();
		}
		working = false;
	}
}
