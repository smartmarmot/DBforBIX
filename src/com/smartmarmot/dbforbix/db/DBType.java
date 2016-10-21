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

import org.apache.commons.lang.NotImplementedException;

import com.smartmarmot.dbforbix.db.DBConstants;

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
		if ("db2".equalsIgnoreCase(data))
			return DB2;
		if ("oracle".equalsIgnoreCase(data))
			return ORACLE;
		if ("mssql".equalsIgnoreCase(data))
			return MSSQL;
		if ("mysql".equalsIgnoreCase(data))
			return MYSQL;
		if ("pgsql".equalsIgnoreCase(data) || "postgresql".equalsIgnoreCase(data))
			return PGSQL;
		if ("allbase".equalsIgnoreCase(data))
			return ALLBASE;
		if ("sybase".equalsIgnoreCase(data))
			return SYBASE;
		if ("sqlany".equalsIgnoreCase(data))
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
				return DBConstants.POSTGRESQL_DRIVER;
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
				throw new NotImplementedException();
			case ORACLE:
				return DBConstants.ORACLE_VALIDATION_QUERY;
			case MSSQL:
				return DBConstants.MSSQL_VALIDATION_QUERY;
			case MYSQL:
				return DBConstants.MYSQL_VALIDATION_QUERY;
			case PGSQL:
				return DBConstants.POSTGRESQL_VALIDATION_QUERY;
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
				return DBConstants.POSTGRESQL_VALIDATION_QUERY;
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
				return DBConstants.POSTGRESQL_DBNAME_QUERY;
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
