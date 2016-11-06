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

import com.smartmarmot.dbforbix.zabbix.ZabbixItem;

public class Discovery implements Item {

	private String		name;
	private String		query;
	private String[]	altNames;

	public Discovery(String name, String query) {
		this.name = name;
		this.query = query;
	}
	
	@Override
	public String getName() {
		return name;
	}

	public void setAltNames(String[] altNames) {
		this.altNames = altNames;
	}

	public String[] getAltNames() {
		return altNames;
	}

	@Override
	public ZabbixItem[] getItemData(Connection con, String hostname, int timeout) throws SQLException {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		Long clock = new Long(System.currentTimeMillis() / 1000L);

		PreparedStatement pstmt = con.prepareStatement(query);
		pstmt.setQueryTimeout(timeout);
		ResultSet rs = pstmt.executeQuery();
		ResultSetMetaData meta = rs.getMetaData();

		builder.append("{\"data\":[");
		while (rs.next()) {
			if (!first)
				builder.append(",");
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				if (altNames == null)
					builder.append("{\"{#" + meta.getColumnName(i).toUpperCase() + "}\":\"" + rs.getString(i) + "\"}");
				else
					builder.append("{\"{#" + altNames[i-1].toUpperCase() + "}\":\"" + rs.getString(i) + "\"}");
			}
			first = false;
		}

		builder.append("]}");
		return new ZabbixItem[] { new ZabbixItem(hostname, name, builder.toString(),clock) };
	}


}
