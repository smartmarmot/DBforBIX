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

package com.smartmarmot.dbforbix.scheduler;

import com.smartmarmot.dbforbix.config.Config.ZServer;

abstract class AbstractItem implements Item {

	protected String name;
	
	public String getName() {
		return name;
	}
	
	
	private ZServer zServer = null;
	
	public AbstractItem(ZServer zs){
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
	
}
