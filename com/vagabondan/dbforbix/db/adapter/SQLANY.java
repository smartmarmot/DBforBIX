package com.vagabondan.dbforbix.db.adapter;

import java.util.Set;

import com.vagabondan.dbforbix.db.DBType;


public class SQLANY extends AbstractAdapter {

	public SQLANY(String name, String url, String user, String passwd,Integer maxactive, Integer maxidle,Integer maxwaitmillis, Set<String> set, Boolean pers) {
		this.name = name;
		this.url = url;
		this.user = user;
		this.passwd = passwd;
		this.maxactive = maxactive.intValue();
		this.maxidle = maxidle.intValue();
		this.maxwaitmillis=maxwaitmillis.intValue();
		this.persistence = pers;
		this.itemGroupName=set;
	}
	
	@Override
	public DBType getType() {
		return DBType.SQLANY;
	}

	

}
