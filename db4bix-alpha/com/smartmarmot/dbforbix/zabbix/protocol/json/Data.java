package com.smartmarmot.dbforbix.zabbix.protocol.json;

class Data {

	private String	host;
	private String	key;
	private Object	value;
	private Long	clock;

	public Data() {}

	public Data(String host, String key, Object value) {
		super();
		this.host = host;
		this.key = key;
		this.value = value;
		this.clock = null;
	}

	public Data(String host, String key, Object value, Long clock) {
		super();
		this.host = host;
		this.key = key;
		this.value = value;
		this.clock = clock;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Long getClock() {
		return clock;
	}

	public void setClock(Long clock) {
		this.clock = clock;
	}

}
