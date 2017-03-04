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

abstract class AbstractMultiItem extends AbstractItem {

	protected String[] items;
	protected String query;
	protected String noData = "";
	
	AbstractMultiItem(String prefix, String[] items, String query, Map<String, String> itemConfig, ZServer zs) {
		super(itemConfig, zs);
		this.name = prefix;
		this.items = items;
		this.query = query;
	}
	
}
