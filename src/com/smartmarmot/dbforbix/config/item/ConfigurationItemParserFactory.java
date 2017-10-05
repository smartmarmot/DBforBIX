package com.smartmarmot.dbforbix.config.item;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory for Zabbix configuration parser
 * @author Vagabondan
 *
 */
public class ConfigurationItemParserFactory {

	public static IConfigurationItemParser getConfigurationItemParser(String config) throws TypeNotPresentException {
		IConfigurationItemParser configurationItemParser=null;
		ConfigurationItemType zabbixConfigurationType;
		Pattern p = Pattern.compile("^\\s*<\\s*parms\\s+.*");
		Matcher m = p.matcher(config);
		if (m.find()){
			zabbixConfigurationType=ConfigurationItemType.XML;
		}else{
			zabbixConfigurationType=ConfigurationItemType.Native;
		}
		switch(zabbixConfigurationType){
		case XML:
			configurationItemParser=new ConfigurationItemParserXML(config);break;
		case Native:
			configurationItemParser=new ConfigurationItemParserNative(config);break;
		default:
			throw new TypeNotPresentException("Couldn't recognize configuration item type! :(",null); 
		}
		return configurationItemParser;
	}
	

}
