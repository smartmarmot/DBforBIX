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

package com.smartmarmot.dbforbix.zabbix;

import java.io.Serializable;

import com.smartmarmot.dbforbix.config.element.IConfigurationElement;

public final class ZabbixItem implements Serializable {

	public static int ZBX_STATE_NORMAL=0;
	public static int ZBX_STATE_NOTSUPPORTED=1;
	
	private static final long	serialVersionUID	= 1374520722821228793L;

	private String					key;
	private String					value;
	private String					host;
	private int						state;
	private Long					clock;
	private String					lastlogsize;
	private IConfigurationElement 	configurationElement;


	//main case
	public ZabbixItem(String key, String value, int state, Long clock, IConfigurationElement configurationElement) {
		if (key == null || "".equals(key.trim()))
			throw new IllegalArgumentException("empty key");
		if (value == null)
			throw new IllegalArgumentException("null value for key '" + key + "'");
		if (configurationElement == null)
			throw new IllegalArgumentException("null configuration element for '" + host + "." + key + "'");

		this.key = key;
		this.value = value;
		this.clock = clock;
		this.host=configurationElement.getConfigurationItem().getHost();
		this.setConfigurationElement(configurationElement);
		this.setState(state);
	}

	
	//For persistence usage
	public ZabbixItem(String host, String key, String value, Long clock) {
		if (key == null || "".equals(key.trim()))
			throw new IllegalArgumentException("empty key");
		if (value == null)
			throw new IllegalArgumentException("null value for key '" + key + "'");
		if (configurationElement == null)
			throw new IllegalArgumentException("null configuration element for '" + host + "." + key + "'");

		this.key = key;
		this.value = value;
		this.clock = clock;
		this.host=host;
		this.configurationElement=null;
	}




	/**
	 * @return The current hostname for this item.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return The monitoring server's key for this item.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return The current value for this item.
	 */
	public String getValue() {
		return value;
	}
	
	public Long getClock() {
		return clock;
	}

	
	public String getLastlogsize() {
		return lastlogsize;
	}

	@Override
	public String toString() {
		return getHost() + " " + getKey() + ": " + getValue();
	}

	public IConfigurationElement getConfigurationElement() {
		return configurationElement;
	}

	public void setConfigurationElement(IConfigurationElement configurationElement) {	
		this.configurationElement = configurationElement;
	}


	public int getState() {
		return state;
	}


	public void setState(int state) {
		this.state = state;
	}
	
	
	public void setValue(String value) {
		this.value = value;
	}
}
