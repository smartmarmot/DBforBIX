/*
 * This file is part of DB4bix.
 *
 * DB4bix is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DB4bix is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * DB4bix. If not, see <http://www.gnu.org/licenses/>.
 */

package com.vagabondan.db4bix.scheduler;

import java.util.Map;

import com.vagabondan.db4bix.config.Config.ZServer;

abstract class AbstractItem implements Item {

	protected String name;
	
	public String getName() {
		return name;
	}
	
	
	private ZServer zServer = null;
	private Map<String,String> itemConfig = null;
	
	
	public AbstractItem(Map<String, String> itemConfig, ZServer zs){
		this.setItemConfig(itemConfig);
		zServer=zs;
	}
	
	public boolean setZServer(ZServer zs){
		if(this.zServer!=null) return false;
		else{
			this.zServer=zs;
			return true;
		}
	}
	
	public ZServer getZServer(){
		return zServer;
	}

	public Map<String,String> getItemConfig() {
		return itemConfig;
	}

	public boolean setItemConfig(Map<String,String> itemConfig) {
		if(this.itemConfig!=null) return false;
		else{
			this.itemConfig = itemConfig;
			return true;
		}
		
	}
	
}
