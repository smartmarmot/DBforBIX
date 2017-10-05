package com.smartmarmot.dbforbix.db.adapter;

import java.util.Set;

import com.smartmarmot.dbforbix.db.DBType;


public class SYBASE extends AbstractDBAdapter {

	public SYBASE(String name, String url, String user, String passwd,Integer maxactive, Integer maxidle,Integer maxwaitmillis, Integer queryTimeout, Set<String> set,Boolean pers) {
		this.name = name;
		this.url = url;
		this.user = user;
		this.passwd = passwd;
		this.maxactive = maxactive.intValue();
		this.maxidle = maxidle.intValue();
		this.maxwaitmillis=maxwaitmillis.intValue();
		this.queryTimeout = queryTimeout.intValue();
		this.configurationUIDs=set;
		this.persistence = pers;
	}
	
	@Override
	public DBType getType() {
		return DBType.SYBASE;
	}

}
