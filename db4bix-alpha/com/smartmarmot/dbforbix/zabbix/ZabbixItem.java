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

import com.smartmarmot.dbforbix.scheduler.Item;

public final class ZabbixItem implements Serializable {

	private static final long	serialVersionUID	= 1374520722821228793L;

	private String				key;
	private String				value;
	private String				host;
	private Long				clock;
	private String				lastlogsize;
	private Item				confItem;


	//main case
	public ZabbixItem(String key, String value, Long clock, Item confItem) {
		if (key == null || "".equals(key.trim()))
			throw new IllegalArgumentException("empty key");
		if (value == null)
			throw new IllegalArgumentException("null value for key '" + key + "'");
		if (confItem == null)
			throw new IllegalArgumentException("null configuration item for '" + host + "." + key + "'");

		this.key = escapeSpecialChars(key);
		this.value = escapeSpecialChars(value);
		this.clock = clock;
		this.host=confItem.getItemConfig().get("host");
		this.setConfItem(confItem);		
	}

	
	//For persistence usage
	public ZabbixItem(String host, String key, String value, Long clock) {
		if (key == null || "".equals(key.trim()))
			throw new IllegalArgumentException("empty key");
		if (value == null)
			throw new IllegalArgumentException("null value for key '" + key + "'");
		if (confItem == null)
			throw new IllegalArgumentException("null configuration item for '" + host + "." + key + "'");

		this.key = escapeSpecialChars(key);
		this.value = escapeSpecialChars(value);
		this.clock = clock;
		this.host=host;
		confItem=null;
	}

	private String escapeSpecialChars(String string) {
		String result=string;
		if(null!=result){
			result=result.replace("\\", "\\\\");//has to be the first one
			result=result.replace("\n", "\\n");
			result=result.replace("\"", "\\\"");			
			result=result.replace("\b", "\\b");
			result=result.replace("\f", "\\f");
			result=result.replace("\r", "\\r");
			result=result.replace("\t", "\\t");			
		}
		return result;
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

	public Item getConfItem() {
		return confItem;
	}

	public void setConfItem(Item confItem) {	
		this.confItem = confItem;
	}
}
