package com.smartmarmot.dbforbix.db;

public class DBConstants {
	public static final String ORACLE= "ORACLE";
	public static final String ORACLE_VALIDATION_QUERY = "SELECT SYSDATE FROM DUAL";
	public static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
	public static final String ORACLE_WHOAMI_QUERY = "SELECT SYS_CONTEXT ('USERENV', 'SESSION_USER') FROM DUAL";
	public static final String ORACLE_DBNAME_QUERY = "SELECT SYS_CONTEXT ('USERENV', 'DB_NAME') FROM DUAL";
	
	public static final String MYSQL="MYSQL";
	public static final String MYSQL_VALIDATION_QUERY = "SELECT 1 FROM DUAL";
	public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	public static final String MYSQL_WHOAMI_QUERY = "SELECT USER()";
	public static final String MYSQL_DBNAME_QUERY = "SELECT DATABASE()";
	
	public static final String PGSQL = "PGSQL";
	public static final String PGSQL_VALIDATION_QUERY = "SELECT 1";
	public static final String PGSQL_WHOAMI_QUERY	 = "SELECT CURRENT_USER";
	public static final String PGSQL_DBNAME_QUERY	 = "SELECT CURRENT_DATABASE()";
	public static final String PGSQL_DRIVER     	     = "org.postgresql.Driver";
	
	public static final String DB2 = "DB2";
	public static final String DB2_VALIDATION_QUERY = "SELECT 1 FROM SYSIBM.DUAL";
	public static final String DB2_WHOAMI_QUERY	= " SELECT SYSTEM_USER FROM SYSIBM.DUAL";
	public static final String DB2_DBNAME_QUERY	= "SELEC T DB_NAME FROM TABLE(SNAP_GET_DB('',-1))";
	public static final String DB2_DRIVER     	= "com.ibm.db2.jcc.DB2Driver";
	
	public static final String MSSQL = "MSSQL";
	public static final String MSSQL_VALIDATION_QUERY = "SELECT 1";
	public static final String MSSQL_WHOAMI_QUERY	= "SELECT CURRENT_USER";
	public static final String MSSQL_DBNAME_QUERY	= "SELECT db_name()";
	public static final String MSSQL_DRIVER     	= "net.sourceforge.jtds.jdbc.Driver";

	public static final String SQLANY = "SQLANY" ;
	public static final String SQLANY_VALIDATION_QUERY = "SELECT 1";
	public static final String SQLANY_WHOAMI_QUERY	= "SELECT CURRENT_USER";
	public static final String SQLANY_DBNAME_QUERY	= "SELECT db_name()";
	public static final String SQLANY_DRIVER     	= "com.sybase.jdbc4.jdbc.SybDriver";
	
	public static final String SYBASE = "SYBASE";
	public static final String SYBASE_VALIDATION_QUERY = "SELECT 1";
	public static final String SYBASE_WHOAMI_QUERY	= "SELECT suser_name()";
	public static final String SYBASE_DBNAME_QUERY	= "SELECT db_name()";
	public static final String SYBASE_DRIVER     	= "net.sourceforge.jtds.jdbc.Driver";

	public static final String ALLBASE = "ALLBASE";
	public static final String ALLBASE_VALIDATION_QUERY = "SELECT 1 FROM MGR@GUSSNM.DUMMY";
	public static final String ALLBASE_WHOAMI_QUERY	= "SELECT 'User' FROM MGR@GUSSNM.DUMMY";
	public static final String ALLBASE_DBNAME_QUERY	= "SELECT 'DBName' FROM MGR@GUSSNM.DUMMY";
	public static final String ALLBASE_DRIVER     	= "com.mbf.jdbc.MBFDriver";

}
