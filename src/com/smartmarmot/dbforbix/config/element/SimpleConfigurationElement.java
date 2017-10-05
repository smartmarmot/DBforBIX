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

package com.smartmarmot.dbforbix.config.element;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

import com.smartmarmot.dbforbix.zabbix.ZabbixItem;


public class SimpleConfigurationElement extends AbstractConfigurationElement {

	private static final Logger		LOG				= Logger.getLogger(SimpleConfigurationElement.class);
	private String simpleItemKey;
	
	public SimpleConfigurationElement(String _prefix, int _time, String _item, String _noData, String _query) {
		super(_prefix,_time,_item,_noData,_query);
		simpleItemKey=_prefix+_item;
	}
	
	@Override
	public ZabbixItem[] getZabbixItemsData(Connection con, int timeout) throws SQLException {
	 	List<ZabbixItem> values = new ArrayList<ZabbixItem>();
	 	try(PreparedStatement pstmt = con.prepareStatement(getQuery())){
			pstmt.setQueryTimeout(timeout);
			try(ResultSet resultSet = pstmt.executeQuery()){
				String val = getNoData();
				Long clock = new Long(System.currentTimeMillis() / 1000L);
				
				while (resultSet.next()) {
					
					/**
					 * Get item value
					 */
					val=getNoData();
					ResultSetMetaData meta = resultSet.getMetaData();
					if (meta.getColumnCount() == 1) {
						String fetchedVal = resultSet.getString(1);
						if (fetchedVal != null)
							val = fetchedVal;
					}
					else {
						int colNum = 1;
						try {
							colNum = resultSet.findColumn("value");
						}
						catch (SQLException sqlex) {
							colNum = meta.getColumnCount(); // to retrieve the last column
						}
						String fetchedVal = resultSet.getString(colNum);
						if (fetchedVal != null)
							val = fetchedVal;
					}
					
					/**
					 * Get item key
					 */
					String realName = simpleItemKey;
					for(int i = 1; i<= meta.getColumnCount(); i++){
							realName = realName.replace("%"+i, (null != resultSet.getString(i)) ? resultSet.getString(i) : getNoData() );
					}
					
					
					values.add(new ZabbixItem(realName, val,ZabbixItem.ZBX_STATE_NORMAL, clock, this));
				}
			}catch(SQLException ex){
				throw ex;
			}
		}catch(SQLException ex){
			LOG.error("Cannot get data for item:\n" + getElementID() +"\nQuery:\n"+getQuery(), ex);
			throw ex;
		}
				

		return values.toArray(new ZabbixItem[0]);
	}

	

}
