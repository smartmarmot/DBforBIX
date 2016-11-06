package com.smartmarmot.dbforbix.db.adapter;

import com.smartmarmot.dbforbix.db.DBType;


public class SQLANY extends AbstractAdapter {

	public SQLANY(String name, String url, String user, String passwd,Integer maxactive, Integer maxidle,Integer maxwaitmillis, String itemfile,Boolean pers) {
		this.name = name;
		this.url = url;
		this.user = user;
		this.passwd = passwd;
		this.maxactive = maxactive.intValue();
		this.maxidle = maxidle.intValue();
		this.maxwaitmillis=maxwaitmillis.intValue();
		this.itemfile=itemfile;
		this.persistence = pers;
	}
	
	@Override
	public DBType getType() {
		return DBType.SQLANY;
	}

}
