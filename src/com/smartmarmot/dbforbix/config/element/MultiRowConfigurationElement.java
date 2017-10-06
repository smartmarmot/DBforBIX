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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import com.smartmarmot.dbforbix.zabbix.ZabbixItem;

public class MultiRowConfigurationElement extends AbstractMultiConfigurationElement {
	
	private static final Logger		LOG				= Logger.getLogger(MultiRowConfigurationElement.class);

	public MultiRowConfigurationElement(String _prefix, int _time, String _items, String _noData, String _query) {
		//noData shouldn't be null!!! Initialize by empty string at least!
		super(_prefix, _time, _items, _noData==null?"":_noData, _query);
	}

	@Override
	public ZabbixItem[] getZabbixItemsData(Connection con, int timeout) throws SQLException {		
		Long clock = new Long(System.currentTimeMillis() / 1000L);
		Map<String,ZabbixItem> values = new HashMap<>();
		
		try(PreparedStatement pstmt = con.prepareStatement(getQuery())){		
			pstmt.setQueryTimeout(timeout);
			try(ResultSet rs = pstmt.executeQuery()){
			
				// fill with base items
				for (String itemKey: itemKeys)
					values.put(itemKey, new ZabbixItem(getPrefix()+itemKey, getNoData(), ZabbixItem.ZBX_STATE_NORMAL,clock, this));
				
				// now check if we find better values
				while (rs.next()) {
					String fetchedName = rs.getString(1).toLowerCase();
					String fetchedVal = rs.getString(2);
					if (fetchedVal != null && values.containsKey(fetchedName)) {
						clock = new Long(System.currentTimeMillis() / 1000L);
						values.put(fetchedName, new ZabbixItem(getPrefix()+fetchedName, fetchedVal,ZabbixItem.ZBX_STATE_NORMAL,clock,this));
						
					}
				}
			}catch(SQLException ex){
				throw ex;
			}
		}catch(SQLException ex){
			LOG.error("Cannot get data for items:\n" + getElementID() +"\nQuery:\n"+getQuery(), ex);
			throw ex;
		}

		return values.values().toArray(new ZabbixItem[0]);
	}


}
