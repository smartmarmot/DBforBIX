package com.smartmarmot.dbforbix.db.adapter;

import com.smartmarmot.dbforbix.db.DBType;


public class SYBASE extends AbstractAdapter {

	public SYBASE(String name, String url, String user, String passwd,Integer maxactive, Integer maxidle,Integer maxwaitmillis, String itemGroupName,Boolean pers) {
		this.name = name;
		this.url = url;
		this.user = user;
		this.passwd = passwd;
		this.maxactive = maxactive.intValue();
		this.maxidle = maxidle.intValue();
		this.maxwaitmillis=maxwaitmillis.intValue();
		this.itemGroupName=itemGroupName;
		this.persistence = pers;
	}
	
	@Override
	public DBType getType() {
		return DBType.SYBASE;
	}

}
