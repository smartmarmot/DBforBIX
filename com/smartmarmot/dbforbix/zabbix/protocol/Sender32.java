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

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

import com.smartmarmot.dbforbix.zabbix.ZabbixItem;

/**
 * Zabbix 3.2 Proxy sender protocol, JSON based
 * 
 * @author Andrey Denisov
 */
public class Sender32 implements SenderProtocol {
	
	

	//Константы:
	private static final String ZBX_PROTO_VALUE_HISTORY_DATA="history data";
	private static final String ZBX_PROTO_VALUE_DISCOVERY_DATA	="discovery data";
	private static final String ZBX_PROTO_VALUE_AUTO_REGISTRATION_DATA="auto registration";

	//DBFORBIX - имя нашего прокси
	
	
	 //private static final String data = "</key><data>";
 	
	 //private static final String time = "</data><timestamp>";

	 //private static final String tail = "</timestamp></req>";

	@Override
	public boolean isMultiValueSupported() {
		return true;
	}

	   
    /**
	 * Encodes data for transmission to the server.
	 * 
	 * This method encodes the data in the ASCII encoding, defaulting to
	 * the platform default encoding if that is somehow unavailable.
	 * 	
	 * @param data
	 * @return byte[] containing the encoded data
	 */
	private byte[] encodeString(String data) {
		try {
			return data.getBytes("ASCII");
		} catch (UnsupportedEncodingException e) {
			return data.getBytes();
		}
	}
	
    private String base64Encode(String data) {
		return new String(Base64.encodeBase64(encodeString(data)));
	}

		
	private String openJson() {
		//{"request":(ZBX_PROTO_VALUE_HISTORY_DATA|ZBX_PROTO_VALUE_DISCOVERY_DATA|ZBX_PROTO_VALUE_AUTO_REGISTRATION_DATA),
		//	"host":DBFORBIX, 
		//	"data":[{"host":HOST_NAME,"key":KEY,"clock":CLOCK,"ns":NS},{},...],
		//	"clock":UNIXTIMESTAMP,
		//	"ns":NS}
		String str=new String("{\"request\":\""+ZBX_PROTO_VALUE_HISTORY_DATA+"\",\"host\":\"DBFORBIX\",\"data\":[");
		return str;
	}
	
	private String buildJsonData(ZabbixItem it) {
		//{"request":(ZBX_PROTO_VALUE_HISTORY_DATA|ZBX_PROTO_VALUE_DISCOVERY_DATA|ZBX_PROTO_VALUE_AUTO_REGISTRATION_DATA),
		//	"host":DBFORBIX, 
		//	"data":[{"host":HOST_NAME,"key":KEY,"clock":CLOCK,"ns":NS},{},...],
		//	"clock":UNIXTIMESTAMP,
		//	"ns":NS}
		String str=new String();
		
		str+="{\"host\":\""+it.getHost()+"\",";
		str+="\"key\":\""+it.getKey()+"\",";
		str+="\"value\":\""+it.getValue()+"\",";
		str+="\"clock\":\""+it.getClock()+"\",";
		str+="\"ns\":\"0\"}";		
		
		return str;
	}
	
	private String closeJson() {
		//{"request":(ZBX_PROTO_VALUE_HISTORY_DATA|ZBX_PROTO_VALUE_DISCOVERY_DATA|ZBX_PROTO_VALUE_AUTO_REGISTRATION_DATA),
		//	"host":DBFORBIX, 
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
	public String encodeItem(ZabbixItem[] items) {
		String result=new String();
		result+=openJson();
		for(ZabbixItem it:items){
			result+=buildJsonData(it)+",";
		}
		result=result.substring(0,result.length()-1);//delete last comma
		result+=closeJson();
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
		ZabbixItem[] items=new ZabbixItem[1];
		items[0]=item;
		String result = encodeItem(items);
		return result;
	}

}
