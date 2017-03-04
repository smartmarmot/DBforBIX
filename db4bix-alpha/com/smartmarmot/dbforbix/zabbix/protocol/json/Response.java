package com.smartmarmot.dbforbix.zabbix.protocol.json;


import java.io.Serializable;

public class Response implements Serializable {
	private static final long serialVersionUID = 4944927703885182914L;
	
	private String response;     
    private String info;

	public Response() {
	}
    
	public String getResponse() {
		return response;
	}
	
	public void setResponse(String response) {
		this.response = response;
	}
	
	public String getInfo() {
		return info;
	}
	
	public void setInfo(String info) {
		this.info = info;
	}
    
}
