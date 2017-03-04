/*
 * This file is part of DB4bix.
 *
 * DB4bix is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DB4bix is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * DB4bix. If not, see <http://www.gnu.org/licenses/>.
 */

package com.vagabondan.db4bix.scheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vagabondan.db4bix.config.Config.ZServer;
import com.vagabondan.db4bix.zabbix.ZabbixItem;


public class SimpleItem extends AbstractItem {

	private String query;
	private String noData = "";

	
	public SimpleItem(String name, String query, String nodata, Map<String, String> itemConfig, ZServer zs) {
		super(itemConfig,zs);
		this.name = name;
		this.query = query;
		this.noData = nodata;
	}
	
	@Override
	public ZabbixItem[] getItemData(Connection con, int timeout) throws SQLException {
	 	List<ZabbixItem> values = new ArrayList<ZabbixItem>();
	 	PreparedStatement pstmt = con.prepareStatement(query);
		pstmt.setQueryTimeout(timeout);
		ResultSet rs = pstmt.executeQuery();
		String val = noData;
		Long clock = new Long(System.currentTimeMillis() / 1000L);
		
		while (rs.next()) {
			val=noData;
			ResultSetMetaData meta = rs.getMetaData();
			if (meta.getColumnCount() == 1) {
				String fetchedVal = rs.getString(1);
				if (fetchedVal != null)
					val = fetchedVal;
			}
			else {
				int colNum = 1;
				try {
					colNum = rs.findColumn("value");
				}
				catch (SQLException sqlex) {
					colNum = meta.getColumnCount(); // to retrieve the last column
				}
				String fetchedVal = rs.getString(colNum);
				if (fetchedVal != null)
					val = fetchedVal;
			}
			
			String realName = name;
			for(int i = 1; i<= meta.getColumnCount(); i++){
				if(null!=rs.getString(i))
					realName = realName.replace("%"+i, rs.getString(i));
			}
			values.add(new ZabbixItem(realName, val,clock, this));
		}
		rs.close();
		pstmt.close();

		
//		if (val == null)
//			val = noData;
//		if (val == noData){
//			String realName = name;
//			values.add(new ZabbixItem(realName, val,clock, this));
//			}
			

		return values.toArray(new ZabbixItem[0]);
	}

	

}
