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

package com.smartmarmot.dbforbix.zabbix.protocol;

import com.smartmarmot.dbforbix.zabbix.ZabbixItem;

/**
 * Zabbix 3.2 Proxy sender protocol, JSON based
 * 
 * @author Vagabondan
 */
public class Sender32 implements ISenderProtocol {
	
	

	/**
	 * 
	 * discovery data
	 */
	private static final String ZBX_PROTO_VALUE_HISTORY_DATA="history data";
	//private static final String ZBX_PROTO_VALUE_DISCOVERY_DATA	="discovery data";
	//private static final String ZBX_PROTO_VALUE_AUTO_REGISTRATION_DATA="auto registration";

	//DBforBix
	
	
	 //private static final String data = "</key><data>";
 	
	 //private static final String time = "</data><timestamp>";

	 //private static final String tail = "</timestamp></req>";

	@Override
	public boolean isMultiValueSupported() {
		return true;
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
	   
    private String openJson(String type, String proxyName) {
		//{"request":(ZBX_PROTO_VALUE_HISTORY_DATA|ZBX_PROTO_VALUE_DISCOVERY_DATA|ZBX_PROTO_VALUE_AUTO_REGISTRATION_DATA),
		//	"host":DBforBix, 
		//	"data":[{"host":HOST_NAME,"key":KEY,"clock":CLOCK,"ns":NS},{},...],
		//	"clock":UNIXTIMESTAMP,
		//	"ns":NS}
		String str=new String("{\"request\":\""+type+"\",\"host\":\""+proxyName+"\",\"data\":[");
		return str;
	}
	
	private String buildJsonData(ZabbixItem it) {
		//{"request":(ZBX_PROTO_VALUE_HISTORY_DATA|ZBX_PROTO_VALUE_DISCOVERY_DATA|ZBX_PROTO_VALUE_AUTO_REGISTRATION_DATA),
		//	"host":DBforBix, 
		//	"data":[{"host":HOST_NAME,"key":KEY,"clock":CLOCK,"ns":NS},{},...],
		//	"clock":UNIXTIMESTAMP,
		//	"ns":NS}
		String str=new String();
		str+="{\"host\":\""+it.getHost()+"\",";
		str+="\"key\":\""+escapeSpecialChars(it.getKey())+"\",";			
		str+="\"value\":\""+escapeSpecialChars(it.getValue())+"\",";
		/**
		 * Error are passed in value while state is set to "not supported"
		 */
		if(ZabbixItem.ZBX_STATE_NOTSUPPORTED == it.getState())
			str+="\"state\":\""+it.getState()+"\",";
		str+="\"clock\":"+it.getClock()+",";
		str+="\"ns\":0}";		
		str+=",";
		return str;
	}
	
	private String closeJson(String type) {
		//{"request":(ZBX_PROTO_VALUE_HISTORY_DATA|ZBX_PROTO_VALUE_DISCOVERY_DATA|ZBX_PROTO_VALUE_AUTO_REGISTRATION_DATA),
		//	"host":DBforBix, 
		//	"data":[{"host":HOST_NAME,"key":KEY,"clock":CLOCK,"ns":NS},{},...],
		//	"clock":UNIXTIMESTAMP,
		//	"ns":NS}
		String str=new String();
		str+="]";
		Long zbxPrxclock = new Long(System.currentTimeMillis() / 1000L);
		str+=",\"clock\":\""+zbxPrxclock+"\",\"ns\":\"0\"";
		str+="}";		
		return str;
	}

	@Override
	public String encodeItems(ZabbixItem[] items) {		
		return encodeItems(items, ZBX_PROTO_VALUE_HISTORY_DATA);
	}
	
	private String encodeItems(ZabbixItem[] items, String type) {
		String result=new String();		
		String proxyName=items.length>0?items[0].getConfigurationElement().getZabbixServer().getProxy():"none";
		result+=openJson(type,proxyName);
		for(ZabbixItem it:items){
			result+=buildJsonData(it);
		}
		result=result.substring(0,result.length()-1);//delete last comma
		result+=closeJson(type);
		return result;
	}

	@Override
	public boolean isResponeOK(int readed, byte[] response) {
		if (readed < 0 )
			return false;
		return true;
	}


	@Override
	public String encodeItem(ZabbixItem item) {
		String result=null;		
		ZabbixItem[] items=new ZabbixItem[1];
		items[0]=item;
		result = encodeItems(items,ZBX_PROTO_VALUE_HISTORY_DATA);		
		return result;
	}

}
