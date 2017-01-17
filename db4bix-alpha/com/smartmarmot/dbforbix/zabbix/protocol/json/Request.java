package com.smartmarmot.dbforbix.zabbix.protocol.json;

public class Request {
	private String request = "sender data";
	private Data[] data;
	private Long clock;
	
	public String getRequest() {
		return request;
	}
	
	public void setRequest(String request) {
		this.request = request;
	}
	
	public Data[] getData() {
		return data;
	}
	
	public void setData(Data[] data) {
		this.data = data;
	}
	
	public Long getClock() {
		return clock;
	}
	
	public void setClock(Long clock) {
		this.clock = clock;
	}
	
}