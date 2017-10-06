package com.smartmarmot.common.utils;

import org.apache.log4j.Logger;

import com.smartmarmot.dbforbix.config.ZabbixServer;

public final class DBforBIXHelper {
	
	private static final Logger		LOG					= Logger.getLogger(DBforBIXHelper.class);
	
	public static boolean isMacro(String macro) {
		String macroMask="^\\{\\$[a-zA-Z0-9_-]+\\}$";		
		return macro.matches(macroMask);
	}
	
	public static String substituteMacros(String inStr, ZabbixServer zabbixServer, String hostid) {
		String result=new String(inStr);
		try{// substitute macro
			int iStart=0;
			int iEnd=0;
			iStart=result.indexOf("{$");
			while (-1!=iStart){
				iEnd=result.indexOf('}', iStart);
				if(-1!=iEnd){							
					String macro=result.substring(iStart, ++iEnd);
					if(isMacro(macro)){
						String macroValue=zabbixServer.getMacroValue(hostid,macro);
						if(null!=macroValue){
							result=result.replace(macro, macroValue);
							iEnd=iEnd-macro.length()+macroValue.length();
						}
					}							
				} else	break;						
				iStart=result.indexOf("{$",iEnd);
			}
		}
		catch (Exception ex){
			LOG.error("Error substituting macros for Zabbix Server "+zabbixServer+",hostid = "+hostid+". String: "+ inStr + "\nException:\n" + ex.getLocalizedMessage());
		}
		return result;
	}

}
