package com.smartmarmot.common;

import com.smartmarmot.dbforbix.zabbix.ZabbixItem;

public interface Persistence {


	public ZabbixItem pop();

	public Long size();

	public void push(ZabbixItem zi);

}

