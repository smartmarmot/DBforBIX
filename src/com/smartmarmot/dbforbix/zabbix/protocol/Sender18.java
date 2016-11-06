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
 * Zabbix 1.8 sender protocol, JSON based
 * 
 * @author Andrea Dalle Vacche
 */
public class Sender18 implements SenderProtocol {
	 private static final String data = "</key><data>";
 	
	 private static final String time = "</data><timestamp>";

	 private static final String tail = "</timestamp></req>";

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

	private String buildJSonString(String host, String item, String value, String clock) {
		 String head = "<req><host>" + base64Encode(host) + "</host><key>";
		 final StringBuilder message = new StringBuilder(head);
	       
	        message.append(base64Encode(item));
	        message.append(data);
	       
	        message.append(base64Encode(value == null ? "" : value));
	        message.append(time);
	        message.append(base64Encode(clock));
	        message.append(tail);

		return message.toString();
	}

	@Override
	public String encodeItem(ZabbixItem item) {
		String payload = buildJSonString(item.getHost(), item.getKey(), item.getValue(),Long.toString(item.getClock()));
		return payload;
	}

	@Override
	public String encodeItem(ZabbixItem[] item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isResponeOK(int readed, byte[] response) {
		if (readed != 2 || response[0] != 'O' || response[1] != 'K')
			return false;
		return true;
	}

}
