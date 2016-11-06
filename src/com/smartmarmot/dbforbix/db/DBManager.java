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

package com.smartmarmot.dbforbix.db;

import java.util.ArrayList;
import java.util.List;

import com.smartmarmot.dbforbix.config.Config;
import com.smartmarmot.dbforbix.db.adapter.ALLBASE;
import com.smartmarmot.dbforbix.db.adapter.Adapter;
import com.smartmarmot.dbforbix.db.adapter.DB2;
import com.smartmarmot.dbforbix.db.adapter.MSSQL;
import com.smartmarmot.dbforbix.db.adapter.MySQL;
import com.smartmarmot.dbforbix.db.adapter.Oracle;
import com.smartmarmot.dbforbix.db.adapter.PGSQL;
import com.smartmarmot.dbforbix.db.adapter.SQLANY;
import com.smartmarmot.dbforbix.db.adapter.SYBASE;

public class DBManager {

	private static DBManager	instance;

	protected DBManager() {}

	private List<Adapter>	databases	= new ArrayList<Adapter>(8);

	public static DBManager getInstance() {
		if (instance == null)
			instance = new DBManager();
		return instance;
	}
//DB2, ORACLE, MSSQL, MYSQL, PGSQL, ALLBASE, SYBASE, SQLANY;
	public void addDatabase(Config.Database cfg) {
		switch (cfg.getType()) {
			case DB2:
				databases.add(new DB2(cfg.getName(), cfg.getURL(), cfg.getUser(), cfg.getPassword(), cfg.getMaxActive(),cfg.getMaxIdle()
						,cfg.getMaxWaitMillis(),cfg.getItemFile(),cfg.getPersistence()));
			break;
			case ORACLE:
				databases.add(new Oracle(cfg.getName(), cfg.getURL(), cfg.getUser(), cfg.getPassword(),cfg.getMaxActive(),cfg.getMaxIdle()
						,cfg.getMaxWaitMillis(),cfg.getItemFile(),cfg.getPersistence()));
			break;
			case MSSQL:
				databases.add(new MSSQL(cfg.getName(), cfg.getURL(), cfg.getUser(), cfg.getPassword(),cfg.getMaxActive(),cfg.getMaxIdle()
						,cfg.getMaxWaitMillis(),cfg.getItemFile(),cfg.getPersistence()));
			break;
			case MYSQL:
				databases.add(new MySQL(cfg.getName(), cfg.getURL(), cfg.getUser(), cfg.getPassword(),cfg.getMaxActive(),cfg.getMaxIdle()
						,cfg.getMaxWaitMillis(),cfg.getItemFile(),cfg.getPersistence()));
			break;
			case PGSQL:
				databases.add(new PGSQL(cfg.getName(), cfg.getURL(), cfg.getUser(), cfg.getPassword(),cfg.getMaxActive(),cfg.getMaxIdle()
						,cfg.getMaxWaitMillis(),cfg.getItemFile(),cfg.getPersistence()));
			break;
			case ALLBASE:
				databases.add(new ALLBASE(cfg.getName(), cfg.getURL(), cfg.getUser(), cfg.getPassword(),cfg.getMaxActive(),cfg.getMaxIdle()
						,cfg.getMaxWaitMillis(),cfg.getItemFile(),cfg.getPersistence()));
			break;
			case SYBASE:
				databases.add(new SYBASE(cfg.getName(), cfg.getURL(), cfg.getUser(), cfg.getPassword(),cfg.getMaxActive(),cfg.getMaxIdle()
						,cfg.getMaxWaitMillis(),cfg.getItemFile(),cfg.getPersistence()));
			break;
			case SQLANY:
				databases.add(new SQLANY(cfg.getName(), cfg.getURL(), cfg.getUser(), cfg.getPassword(),cfg.getMaxActive(),cfg.getMaxIdle()
						,cfg.getMaxWaitMillis(), cfg.getItemFile(),cfg.getPersistence()));
			break;
		}
	}

	public Adapter[] getDatabases(String itemFile) {
		ArrayList<Adapter> result = new ArrayList<Adapter>(databases.size());
		for (Adapter db : databases) {
			if (db.getItemFile().equals(itemFile))
				result.add(db);
		}
		return result.toArray(new Adapter[result.size()]);
	}

	public Adapter[] getDatabases() {
		return databases.toArray(new Adapter[databases.size()]);
	}
}
