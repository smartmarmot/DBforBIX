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

package com.smartmarmot.dbforbix.db;

/**
 * SQL database type
 * 
 * @author Andrea Dalle Vacche
 */
public enum DBType {

	DB2, ORACLE, MSSQL, MYSQL, PGSQL, ALLBASE, SYBASE, SQLANY;

	/**
	 * Parses a string and returns a matching database type object
	 * @param data
	 * @return
	 */
	
	public static DBType fromString(String data) {
		if (DBConstants.DB2.equalsIgnoreCase(data))
			return DB2;
		if (DBConstants.ORACLE.equalsIgnoreCase(data))
			return ORACLE;
		if (DBConstants.MSSQL.equalsIgnoreCase(data))
			return MSSQL;
		if (DBConstants.MYSQL.equalsIgnoreCase(data))
			return MYSQL;
		if (DBConstants.PGSQL.equalsIgnoreCase(data))
			return PGSQL;
		if (DBConstants.ALLBASE.equalsIgnoreCase(data))
			return ALLBASE;
		if (DBConstants.SYBASE.equalsIgnoreCase(data))
			return SYBASE;
		if (DBConstants.SQLANY.equalsIgnoreCase(data))
			return SQLANY;
		return null;
	}

	/**
	 * @return the known JDBC driver class name
	 * DB2, ORACLE, MSSQL, MYSQL, PGSQL, ALLBASE, SYBASE, SQLANY;
	 */
	public String getJDBCDriverClass() {
		switch (this) {
			case DB2:
				return DBConstants.DB2_DRIVER;
			case ORACLE:
				return DBConstants.ORACLE_DRIVER;
			case MSSQL:
				return DBConstants.MSSQL_DRIVER;
			case MYSQL:
				return DBConstants.MYSQL_DRIVER;
			case PGSQL:
				return DBConstants.PGSQL_DRIVER;
			case ALLBASE :	
				return DBConstants.ALLBASE_DRIVER;
			case SYBASE :	
				return DBConstants.SYBASE_DRIVER;
			case SQLANY :	
				return DBConstants.SQLANY_DRIVER;
		}
		return null;
	}

	/**
	 * @return A simple statement to check if the database respond to queries
	 * DB2, ORACLE, MSSQL, MYSQL, PGSQL, ALLBASE, SYBASE, SQLANY;
	 */
	public String getAliveSQL() {
		switch (this) {
			case DB2:
				return DBConstants.DB2_VALIDATION_QUERY;
			case ORACLE:
				return DBConstants.ORACLE_VALIDATION_QUERY;
			case MSSQL:
				return DBConstants.MSSQL_VALIDATION_QUERY;
			case MYSQL:
				return DBConstants.MYSQL_VALIDATION_QUERY;
			case PGSQL:
				return DBConstants.PGSQL_VALIDATION_QUERY;
			case ALLBASE :	
				return DBConstants.ALLBASE_VALIDATION_QUERY;
			case SYBASE :	
				return DBConstants.SYBASE_VALIDATION_QUERY;
			case SQLANY :	
				return DBConstants.SQLANY_VALIDATION_QUERY;
		}
		return null;
	}

	/**
	 * @return A query that returns the current user in the database
	 * DB2, ORACLE, MSSQL, MYSQL, PGSQL, ALLBASE, SYBASE, SQLANY;
	 */
	public String getWhoAmISQL() {
		switch (this) {
			case DB2:
				return DBConstants.DB2_WHOAMI_QUERY;
			case ORACLE:
				return DBConstants.ORACLE_WHOAMI_QUERY;
			case MSSQL:
				return DBConstants.MSSQL_WHOAMI_QUERY;
			case MYSQL:
				return DBConstants.MYSQL_WHOAMI_QUERY;
			case PGSQL:
				return DBConstants.PGSQL_WHOAMI_QUERY;
			case ALLBASE :	
				return DBConstants.ALLBASE_WHOAMI_QUERY;
			case SYBASE :	
				return DBConstants.SYBASE_WHOAMI_QUERY;
			case SQLANY :	
				return DBConstants.SQLANY_WHOAMI_QUERY;
		}
		return null;
	}

	/**
	 * @return A query which returns the current database name
	 * DB2, ORACLE, MSSQL, MYSQL, PGSQL, ALLBASE, SYBASE, SQLANY;
	 */
	public String getDbNameSQL() {
		switch (this) {
			case DB2:
				return DBConstants.DB2_DBNAME_QUERY;
			case ORACLE:
				return DBConstants.ORACLE_DBNAME_QUERY;
			case MSSQL:
				return DBConstants.MSSQL_DBNAME_QUERY;
			case MYSQL:
				return DBConstants.MYSQL_DBNAME_QUERY;
			case PGSQL:
				return DBConstants.PGSQL_DBNAME_QUERY;
			case ALLBASE :	
				return DBConstants.ALLBASE_DBNAME_QUERY;
			case SYBASE :	
				return DBConstants.SYBASE_DBNAME_QUERY;
			case SQLANY :	
				return DBConstants.SQLANY_DBNAME_QUERY;	
		}
		return null;
	}

}
