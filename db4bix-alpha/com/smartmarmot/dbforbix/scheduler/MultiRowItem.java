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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.smartmarmot.dbforbix.config.Config.ZServer;
import com.smartmarmot.dbforbix.zabbix.ZabbixItem;

public class MultiRowItem extends AbstractMultiItem {
	
	private static final Logger		LOG				= Logger.getLogger(MultiRowItem.class);

	public MultiRowItem(String prefix, String itemList, String query, Map<String, String> itemConfig, ZServer zs) {
		super(prefix, itemList, query, itemConfig, zs);
	}

	@Override
	public ZabbixItem[] getItemData(Connection con, int timeout) throws SQLException {		
		Long clock = new Long(System.currentTimeMillis() / 1000L);
		Map<String,ZabbixItem> values = new HashMap<>();
		
		try(PreparedStatement pstmt = con.prepareStatement(query)){		
			pstmt.setQueryTimeout(timeout);
			try(ResultSet rs = pstmt.executeQuery()){
			
				// fill with base items
				for (String item: items)
					values.put(item, new ZabbixItem(name+item, noData,ZabbixItem.ZBX_STATE_NORMAL,clock, this));
				
				// now check if we find better values
				while (rs.next()) {
					String fetchedName = rs.getString(1).toLowerCase();
					String fetchedVal = rs.getString(2);
					if (fetchedVal != null && values.containsKey(fetchedName)) {
						clock = new Long(System.currentTimeMillis() / 1000L);
						values.put(fetchedName, new ZabbixItem(name+fetchedName, fetchedVal,ZabbixItem.ZBX_STATE_NORMAL,clock,this));
						
					}
				}
			}catch(SQLException ex){
				throw ex;
			}
		}catch(SQLException ex){
			LOG.error("Cannot get data for items:\n" + name+itemList +"\nQuery:\n"+query, ex);
			throw ex;
		}

		return values.values().toArray(new ZabbixItem[0]);
	}


}
