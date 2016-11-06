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
 * Old zabbix 1.1 sender protocol, XML based
 * 
 * @author Andrea Dalle Vacche
 */
public class Sender14 implements SenderProtocol { 

	@Override
	public boolean isMultiValueSupported() {
		return false;
	}

	@Override
	public String encodeItem(ZabbixItem item) {
		String result = "<req><host>" + encode64(item.getHost()) + "</host><key>";
		result += encode64(item.getKey());
		result += "</key><data>";
		result += encode64(item.getValue());
		result += "</data></req>";
		return result;
	}

	private String encode64(String input) {
		return new String(org.apache.commons.codec.binary.Base64.encodeBase64(input.getBytes()));
	}

	@Override
	public String encodeItem(ZabbixItem[] item) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isResponeOK(int readed, byte[] response) {
		if (readed != 2 || response[0] != 'O' || response[1] != 'K')
			return false;
		return true;
	}

}
