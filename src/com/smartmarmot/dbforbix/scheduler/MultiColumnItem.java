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

import com.smartmarmot.dbforbix.zabbix.ZabbixItem;


public class MultiColumnItem extends AbstractMultiItem {

	public MultiColumnItem(String prefix, String[] items, String query) {
		super(prefix, items, query);
	}

	@Override
	public ZabbixItem[] getItemData(Connection con, String hostname, int timeout) throws SQLException {
		Long clock = new Long(System.currentTimeMillis() / 1000L);
		PreparedStatement pstmt = con.prepareStatement(query);
		pstmt.setQueryTimeout(timeout);
		ResultSet rs = pstmt.executeQuery();
		String val = noData;
		Map<String,ZabbixItem> values = new HashMap<>();
		
//		// fill with base items
//		for (String item: items)
//			values.put(item, new ZabbixItem(name, "", hostname));
//		
//		// now check if we find better values
		while (rs.next()) {
//			String fetchedName = rs.getString(1);
//			String fetchedVal = rs.getString(2);
//				if (fetchedVal != null)
//					val = fetchedVal;
//			}
//			else {
//				int colNum = 1;
//				try {
//					colNum = rs.findColumn("value");
//				}
//				catch (SQLException sqlex) {
//					colNum = meta.getColumnCount(); // last column
//				}
//				String fetchedVal = rs.getString(colNum);
//				if (fetchedVal != null)
//					val = fetchedVal;
//			}
		}
		rs.close();
		pstmt.close();

		if (val == null)
			val = "";

		return new ZabbixItem[]{new ZabbixItem(hostname, name, val,clock)};
	}


}
