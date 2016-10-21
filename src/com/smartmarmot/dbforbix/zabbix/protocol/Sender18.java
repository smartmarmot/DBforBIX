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

import org.apache.commons.lang.NotImplementedException;

import com.smartmarmot.dbforbix.zabbix.ZabbixItem;

/**
 * Zabbix 1.8 sender protocol, JSON based
 * 
 * @author Andrea Dalle Vacche
 */
public class Sender18 implements SenderProtocol {

	public Sender18() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isMultiValueSupported() {
		return true;
	}

	private String buildJSonString(String host, String item, String value) {
		return "{"
				+ "\"request\":\"sender data\",\n"
				+ "\"data\":[\n"
				+ "{\n" + "\"host\":\""	+ host + "\",\n"
				+ "\"key\":\"" + item+ "\",\n" 
				+ "\"value\":\"" + value.replace("\\", "\\\\") 
				+ "\"}]}\n";
	}

	@Override
	public String encodeItem(ZabbixItem item) {
		String payload = buildJSonString(item.getHost(), item.getKey(), item.getValue());
		byte[] data = payload.getBytes();
		int length = data.length;
		
		byte[] header = new byte[] {
				'Z', 'B', 'X', 'D', 
				'\1',
				(byte)(length & 0xFF), 
				(byte)((length >> 8) & 0x00FF), 
				(byte)((length >> 16) & 0x0000FF), 
				(byte)((length >> 24) & 0x000000FF),
				'\0','\0','\0','\0'};
		
		return new String(header).concat(new String(data));
	}

	@Override
	public String encodeItem(ZabbixItem[] item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isResponeOK(int readed, byte[] response) {
		// TODO Auto-generated method stub
		return false;
	}

}
