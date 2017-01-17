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

package com.smartmarmot.dbforbix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collection;
import java.util.Date;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.SimpleLayout;
import com.smartmarmot.common.Constants;
import com.smartmarmot.dbforbix.config.Config;
import com.smartmarmot.dbforbix.config.Config.ZServer;
import com.smartmarmot.dbforbix.db.DBManager;
import com.smartmarmot.dbforbix.zabbix.PersistentDBSender;
import com.smartmarmot.dbforbix.zabbix.ZabbixSender;

/**
 * DBforBix daemon main class
 */
public class DBforBix implements Daemon {
	
	private static final Logger				LOG				= Logger.getLogger(DBforBix.class);
	
	private static ZabbixSender				zbxSender;
	private static PersistentDBSender	persSender;           
	private static boolean debug = false;
	
	private static Options					options;
	
	private static void reinit(){
		//get config instance
		Config config = Config.getInstance();
		
		// read config file
		try {
			config.readFileConfig();
		}
		catch (IOException e) {
			System.err.println("Error in config: " + e.getLocalizedMessage());
			System.exit(-1);
		}
		catch (NullPointerException e) {
			System.err.println("Error while getting config hash file: " + e.getLocalizedMessage());
			System.exit(-1);
		}
		
		// init logging
		try {
			String logfile = config.getLogFile();
			
			if (logfile.startsWith("./"))
				logfile = logfile.replaceFirst(".", config.getBasedir());
			
			PatternLayout layout = new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss}] [%-5p] [%t[%M(%F:%L)]]: %m%n");
			RollingFileAppender rfa = new RollingFileAppender(layout, logfile, true);
			rfa.setMaxFileSize(config.getLogFileSize());
			rfa.setMaxBackupIndex(1);
			
			Logger.getRootLogger().addAppender(rfa);
			if (!debug)
				Logger.getRootLogger().setLevel(config.getLogLevel());
		}
		catch (IOException ex) {
			System.err.println("Error while configuring logging: " + ex.getLocalizedMessage());
			LOG.error(ex.getLocalizedMessage(), ex);
		}
		
		
		LOG.info("### executing " + Constants.BANNER + ": " + new Date().toString());
		LOG.info("using config file " + config.getConfigFile());
		LOG.debug(config);							
		
		// read config from Zabbix Server
		config.getItemConfigFromZabbix();// fill itemConfigs collection
		config.buildItems();
	} 
	
	
	public static void main(final String[] args) {
		
		Config config = Config.getInstance();
		String action = "";
		
		options = new Options();
		options.addOption("v", false, "display version and exit");
		options.addOption("t", false, "test configuration");
		options.addOption("h", false, "display help");
		
		options.addOption("b", true, "base directory");
		options.addOption("c", true, "config file location");
		
		options.addOption("d", false, "enable debugging");
		options.addOption("C", false, "enable console output");
		
		options.addOption("a", true, "action (start/stop/status)");
		
		// handle command line parameters
		try {
			CommandLineParser cmdparser = new DefaultParser();
			CommandLine cmd = cmdparser.parse(options, args);
			
			if (cmd.hasOption("v")) {
				System.out.println(Constants.BANNER);
				System.exit(0);
			}
			
			if (cmd.hasOption("t")) {
				System.out.println("not implemented");
				System.exit(0);
			}
			
			if (args.length == 0 || cmd.hasOption("h")) {
				displayUsage();
				System.exit(0);
			}
			
			if (cmd.hasOption("d")) {
				debug = true;
				Logger.getRootLogger().setLevel(Level.ALL);
			}
			
			if (cmd.hasOption("C"))
				Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout()));
			
			action = cmd.getOptionValue("a", "help");
			
			/**
			 * set config file path
			 */			
			config.setBasedir(cmd.getOptionValue("b", "."));
			config.setConfigFile(cmd.getOptionValue("c", config.getBasedir() + File.separator + "conf" + File.separator + "config.properties"));
		}
		catch (ParseException e) {
			System.err.println("Error in parameters: " + e.getLocalizedMessage());
			System.exit(-1);
		}
		
		//autoupdate cycle
		while(true){
			try {
				switch (action.toLowerCase()) {
					case "start": {						
						reinit();
						
						LOG.info("Starting "+ Constants.BANNER);
						// writePid(_pid, _pidfile);
						
						zbxSender = new ZabbixSender(ZabbixSender.PROTOCOL.V32);
						zbxSender.updateServerList(config.getZabbixServers().toArray(new ZServer[0]));
						zbxSender.start();
										
						persSender = new PersistentDBSender(PersistentDBSender.PROTOCOL.V18);
						persSender.updateServerList(config.getZabbixServers().toArray(new ZServer[0]));
						persSender.start();

						
						config.startChecks();
						action="update";
					}
					break;
					case "update": {
						LOG.info("Sleeping before configuration update...");
						Thread.sleep(config.getUpdateConfigTimeout()*1000);
						LOG.info("Updating DBforBix configuration...");
						if(config.checkConfigChanges()) action="stop";				
					}
					break;
					case "stop": {
						LOG.info("Stopping DBforBix...");
						config=config.reset();
						if (zbxSender != null) {
							zbxSender.terminate();
							while (zbxSender.isAlive())
								Thread.yield();
						}
						//workTimers=new HashMap<String, Timer>();
						zbxSender=null;
						
						if (persSender != null) {
							persSender.terminate();
							while (persSender.isAlive())
								Thread.yield();
						}
						
						DBManager dbman=DBManager.getInstance();
						dbman=dbman.cleanAll();
						
						action="start";
					}
					break;
					default: {
						LOG.error("Unknown action " + action);
						System.exit(-5);
					}
					break;
				}
			}
			catch (Throwable th) {
				LOG.fatal("DBforBix crashed!", th);
			}
		}
	}
	
	private static void displayUsage() {
		System.out.println(Constants.BANNER);
		for (Option o: ((Collection<Option>) options.getOptions()))
			System.out.println("\t-" + o.getOpt() + "\t" + o.getDescription());
	}
	
//	private static void loadItemConfig(List<String> itemFileList) {
//		File paramDir = new File(Config.getInstance().getBasedir() + File.separator + "items");
//		File[] paramFiles = paramDir.listFiles(new FilenameFilter() {
//			
//			@Override
//			public boolean accept(File dir, String name) {
//				return name.toLowerCase().endsWith(".xml");
//			}
//		});
//		
//		for (File f: paramFiles) {
//			if (itemFileList.contains(f.getName())){
//				LOG.info("reading items from " + f.getAbsolutePath().replace(File.separator+"."+File.separator, File.separator));
//				try {
//					String itemFile = f.getName();
//					SAXReader saxReader = new SAXReader();
//					Document doc = saxReader.read(f);
//					Element root = doc.getRootElement();
//					String prefix = root.attributeValue("prefix");
//	//				DBType dbType = DBType.fromString(root.attributeValue("type").toLowerCase());
//					for (Object srv: root.elements("server")) {
//						if (srv instanceof Element)
//							buildServerElements((Element) srv, itemFile, prefix );
//					}
//					for (Object db: root.elements("database")) {
//						if (db instanceof Element)
//							buildDatabaseElements((Element) db, itemFile, prefix);
//					}
//				}
//				catch (Exception ex) {
//				LOG.error("Error while loading " + f.getName(), ex);
//				}
//			}
//		}
//	}
	
//	private static void buildServerElements(Element e, String itemFile, String prefix) {
//		for (Object itm: e.elements()) {
//			if (itm instanceof Element) {
//				Element itmE = (Element) itm;
//				int time = 60;
//				try {
//					time = Integer.parseInt(itmE.attributeValue("time"));
//				}
//				catch (NumberFormatException ex) {
//					LOG.warn("invalid time value: " + itmE.attributeValue("time"));
//				}
//				if (!scheduler.containsKey(time)) {
//					LOG.debug("creating item scheduler with time " + time);
//					scheduler.put(time, new Scheduler(time));
//				}
//				Scheduler itemSch = scheduler.get(time);
//				switch (itmE.getName()) {
//					case "discovery": {
//						Discovery item = new Discovery(prefix + itmE.attributeValue("item"), itmE.getTextTrim());
//						String nameList = itmE.attributeValue("names", "");
//						String names[] = nameList.split("\\|");
//						if (names != null && names.length > 0)
//							item.setAltNames(names);
//						itemSch.addItem(itemFile, item);
//					}
//					break;
//					
//					case "query": {
//						Item item = new SimpleItem(prefix + itmE.attributeValue("item"), itmE.getTextTrim(),itmE.attributeValue("nodata"));
//						itemSch.addItem(itemFile, item);
//					}
//					break;
//					
//					case "multiquery": {
//						String itemList = itmE.attributeValue("items", "");
//						String items[] = itemList.split("\\|");
//						Item item;
//						if (itmE.attributeValue("type", "column").equalsIgnoreCase("column"))
//							item = new MultiColumnItem(prefix, items, itmE.getTextTrim());
//						else
//							item = new MultiRowItem(prefix, items, itmE.getTextTrim());
//						itemSch.addItem(itemFile, item);
//					}
//					break;
//				}
//			}
//		}
//	}
	
//	private static void buildDatabaseElements(Element e, String itemFile, String prefix) {
//		for (Object itm: e.elements()) {
//			if (itm instanceof Element) {
//				Element itmE = (Element) itm;
//				int time = 60;
//				try {
//					time = Integer.parseInt(itmE.attributeValue("time"));
//				}
//				catch (NumberFormatException ex) {
//					LOG.warn("invalid time value: " + itmE.attributeValue("time"));
//				}
//				if (!scheduler.containsKey(time)) {
//					LOG.debug("creating item scheduler with time " + time);
//					scheduler.put(time, new Scheduler(time));
//				}
//				Scheduler itemSch = scheduler.get(time);
//				switch (itmE.getName()) {
//					case "discovery": {
//						Discovery item = new Discovery(prefix + itmE.attributeValue("item"), itmE.getTextTrim());
//						String nameList = itmE.attributeValue("names", "");
//						String names[] = nameList.split("\\|");
//						if (names != null && names.length > 0)
//							item.setAltNames(names);
//						itemSch.addItem(itemFile, item);
//					}
//					break;
//					
//					case "query": {
//						Item item = new SimpleItem(prefix + itmE.attributeValue("item"), itmE.getTextTrim(), itmE.attributeValue("nodata"));
//						itemSch.addItem(itemFile, item);
//					}
//					break;
//					
//					case "multiquery": {
//						String itemList = itmE.attributeValue("items", "");
//						String items[] = itemList.split("\\|");
//						Item item;
//						if (itmE.attributeValue("type", "column").equalsIgnoreCase("column"))
//							item = new MultiColumnItem(prefix, items, itmE.getTextTrim());
//						else
//							item = new MultiRowItem(prefix, items, itmE.getTextTrim());
//						itemSch.addItem(itemFile, item);
//					}
//					break;
//				}
//			}
//		}
//	}
	
	
	
	public static ZabbixSender getZSender() {
		return zbxSender;
	}
	
	
	public static void writePid(String _pidfile) throws Exception {
		RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
		String pid = rmxb.getName();
		try {
			
			File target = new File(_pidfile);
			
			File newTarget = new File(target.getAbsoluteFile().getCanonicalPath());
			target = null;
			
			if (newTarget.exists()) {
				boolean success = newTarget.delete();
				if (!success) {
					DBforBix.LOG.log(Level.ERROR, "Delete: deletion failed " + newTarget.getAbsolutePath());
				}
			}
			if (!newTarget.exists()) {
				FileOutputStream fout = new FileOutputStream(newTarget);
				new PrintStream(fout).print(pid);
				fout.close();
			}
			
		}
		catch (IOException e) {
			DBforBix.LOG.log(Level.ERROR, "Unable to write to file " + _pidfile + " error:" + e);
		}
	}
	
	@Override
	public void init(DaemonContext dc) throws Exception {
		String[] args = dc.getArguments();
		if (args == null || args.length == 0)
			throw new IllegalStateException("BASEDIR missing");
		Config.getInstance().setBasedir(args[0]);
	}
	
	public static void start(String[] args) {
		main(new String[] { "-a", "start" });
	}

	public static void stop(String[] args) {
		main(new String[] { "-a", "stop" });
	}
	
	@Override
	public void start() throws Exception {
		main(new String[] { "-a", "start", "-b", Config.getInstance().getBasedir() });
	}
	
	@Override
	public void stop() throws Exception {
		main(new String[] { "-a", "stop" });
	}
	
	@Override
	public void destroy() {}
	
}
