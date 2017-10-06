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

package com.smartmarmot.dbforbix.config.element;

import com.smartmarmot.dbforbix.config.ZabbixServer;
import com.smartmarmot.dbforbix.config.item.IConfigurationItem;

abstract class AbstractConfigurationElement implements IConfigurationElement {

	private String id;
	private String prefix;
	private String query;
	private int time=60;
	private String noData=null;
	private IConfigurationItem configurationItem=null;
	
	
	public AbstractConfigurationElement(String _prefix, int _time, String _item_or_items, String _noData, String _query){		
		prefix = _prefix;
		time=_time>0?_time:time;
		id = _prefix+_item_or_items;
		noData = _noData;
		query = _query;
	}
	
	@Override
	public int getTime(){
		return time;
	}
		
	@Override
	public ZabbixServer getZabbixServer(){
		return configurationItem.getZabbixServer();
	}	

	@Override
	public String getElementID() {
		return id;
	}
	
	/**
	 * @return the prefix
	 */
	@Override
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @return the query
	 */
	@Override
	public String getQuery() {
		return query;
	}	
	
	@Override
	public String getNoData(){
		return noData;
	}
	
	@Override
	public void setConfigurationItem(IConfigurationItem _configurationItem){
		configurationItem=_configurationItem;
	}
	
	@Override
	public IConfigurationItem getConfigurationItem() {
		return configurationItem;
	}
	
	@Override
	public	String getConfigurationUID() throws NullPointerException{
		return this.getConfigurationItem().getConfigurationUID();		
	}
}
