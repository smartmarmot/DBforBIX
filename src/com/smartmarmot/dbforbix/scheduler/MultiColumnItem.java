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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.smartmarmot.dbforbix.config.Config.ZServer;
import com.smartmarmot.dbforbix.zabbix.ZabbixItem;


public class MultiColumnItem extends AbstractMultiItem {
	
	private static final Logger		LOG				= Logger.getLogger(MultiColumnItem.class);

	public MultiColumnItem(String prefix, String itemList, String query, Map<String, String> itemConfig, ZServer zs) {
		super(prefix, itemList, query, itemConfig, zs);
	}

	@Override
	public ZabbixItem[] getItemData(Connection con, int timeout) throws SQLException {
		Long clock = new Long(System.currentTimeMillis() / 1000L);
		List<ZabbixItem> values = new ArrayList<>();
		/**
		 * Statement has to be closed or "Error message: ORA-01000: maximum open cursors exceeded" occurs with time otherwise
		 */
		try(PreparedStatement pstmt = con.prepareStatement(query)){
			pstmt.setQueryTimeout(timeout);
			try(ResultSet rs = pstmt.executeQuery()){
							
				/**
				 * <multiquery time="60" item="index[%1]|free[%1]" type="column">
				 * SELECT table_schema "database", SUM(index_length) "size", SUM(data_free) "free" 
				 * FROM INFORMATION_SCHEMA.TABLES 
				 * WHERE table_schema NOT IN ('information_schema','performance_schema') GROUP BY table_schema
				 * </multiquery>
				 */	
				
				while (rs.next()) {//it can be multirows
					ResultSetMetaData meta = rs.getMetaData();
					if (meta.getColumnCount() < items.length) {
						LOG.error("Number of columns in select of item "+name+items+"\nof item config: "+this.getItemConfig().keySet()+
								"\nis less than in item config");
						break;
					}
					else {
						//from the last column to first
						for (int it=items.length-1, column=meta.getColumnCount();it>=0;--it,--column){
							//name[%1_%2_%5] -> name[one_two_three]
							String realName = items[it];
							for(int i = 1; i<= meta.getColumnCount(); i++)
								realName = realName.replace("%"+i, (null != rs.getString(i)) ? rs.getString(i) : noData );
							//get value
							values.add(new ZabbixItem(name+realName, 
									(null != rs.getString(column)) ? rs.getString(column) : noData,
									ZabbixItem.ZBX_STATE_NORMAL,clock, this));
						}
					}
				}
			}
			/**
			 * Autoclose rs statement
			 */
			catch(SQLException ex){
				throw ex;
			}
		}catch(SQLException ex){
			LOG.error("Cannot get data for items:\n" + name+itemList +"\nQuery:\n"+query, ex);
			throw ex;
		}
		return values.toArray(new ZabbixItem[0]);
	}


}
