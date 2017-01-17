package com.smartmarmot.common;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.smartmarmot.dbforbix.config.Config;
import com.smartmarmot.dbforbix.zabbix.ZabbixItem;
import com.smartmarmot.dbforbix.zabbix.ZabbixSender;

public class StackSingletonPersistent implements Persistence{

	private String DIRECTORY = Config.getInstance().getSPDir();
	private static final Logger	LOG	= Logger.getLogger(ZabbixSender.class);
	private static StackSingletonPersistent instance; 
	private Long size;

	private StackSingletonPersistent() {};

	private void init() {
		size = 0L;
		Config config = Config.getInstance();
		if (DIRECTORY.startsWith("./"))
			DIRECTORY = DIRECTORY.replaceFirst(".", config.getBasedir());
		DIRECTORY.replace("//","/");
		
		File file = new File(DIRECTORY);
		if (!file.exists()) {
			LOG.log(Level.ERROR, "Persistence directory "+DIRECTORY +" does not exists");
			boolean success = (new File(DIRECTORY)).mkdirs();
			if (!success) {
				LOG.log(Level.INFO, "Persistence directory "+DIRECTORY +" created");
			} else{
				LOG.log(Level.ERROR, "Persistence directory "+DIRECTORY +" creation failed");
			}
		}
		File[] files = file.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".ssp");
			}
		});

		for (int i = 0; i < files.length; i++) {
			try {
				read(getFile(i+1L));
				size++;
			} catch (Exception e) {
				LOG.log(Level.ERROR, "Persistence directory "+DIRECTORY +" problem "+e);
				break;
			}
		}
	}

	private File getFile(Long index) {
		return new File(DIRECTORY + "/Item" + index + ".ssp");
	}

	private Object read(File file) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream itemStream = null;
		try {
			itemStream = new ObjectInputStream(new FileInputStream(file));
			return itemStream.readObject();
		} finally {
			if (itemStream != null) {
				itemStream.close();
			}
		}
	}

	private void save(Object item) throws FileNotFoundException, IOException {
		ObjectOutputStream itemStream = null;
		try {
			itemStream = new ObjectOutputStream(new FileOutputStream(getFile(size)));
			itemStream.writeObject(item);
		} finally {
			if (itemStream != null) {
				itemStream.close();
			}
		}
	}

	public static synchronized StackSingletonPersistent getInstance() {
		if (instance == null) {
			instance = new StackSingletonPersistent();
			instance.init();
		}
		return instance;
	}

	public void push(ZabbixItem item) {
		synchronized (StackSingletonPersistent.class) {
			size++;
			try {
				save(item);
			} catch (Exception e) {
				LOG.log(Level.ERROR, "Persistence error:"+ e.getMessage());
			}
		}   
	}

	public ZabbixItem pop() {
		synchronized (StackSingletonPersistent.class) {
			if (size > 0) {
				File file = getFile(size);
				Object result=null;
				try {
					result = read(file);
				} catch (Exception e) {
					e.printStackTrace();
				}
				file.delete();
				size--;
				return (ZabbixItem) result;
			} else {
				return null;
			}
		}
	}

	public Object peek() throws FileNotFoundException, IOException, ClassNotFoundException {
		synchronized (StackSingletonPersistent.class) {
			if (size > 0L) {
				return read(getFile(size));
			} else {
				return null;
			}
		}
	}

	public Long size() {
		synchronized (StackSingletonPersistent.class) {
			return size;
		}
	}

	// public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {

	//  System.out.println(StackSingletonPersistent.getInstance().size());

	//    // Populate

	//   StackSingletonPersistent.getInstance().push(new ZabbixItem("chiave","Valore","host"));
	//  StackSingletonPersistent.getInstance().push(new ZabbixItem("chiave1","Valore1","host1"));
	//  StackSingletonPersistent.getInstance().push(new ZabbixItem("chiave2","Valore2","host2"));
	//  StackSingletonPersistent.getInstance().push(new ZabbixItem("chiave3","Valore3","host3"));

	// StackSingletonPersistent.getInstance().push("dhgfjs jhsf jh fsjd");
	// StackSingletonPersistent.getInstance().push(new BigDecimal(15898604));
	// StackSingletonPersistent.getInstance().push("asdsasdasdas");

	//
	// System.out.println(StackSingletonPersistent.getInstance().size());
	// System.out.println(StackSingletonPersistent.getInstance().peek());
	// while (StackSingletonPersistent.getInstance().size != 0 ){
	// 	ZabbixItem zx = (ZabbixItem) StackSingletonPersistent.getInstance().pop();
	// 	System.out.println("key-->"+zx.getKey()+"Value-->"+zx.getValue()+"Host-->"+zx.getHost());
	// }

	// }
}
