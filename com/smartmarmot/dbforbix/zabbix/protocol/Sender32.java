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

import com.smartmarmot.dbforbix.scheduler.Discovery;
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

		
	private String openJson(String type) {
		//{"request":(ZBX_PROTO_VALUE_HISTORY_DATA|ZBX_PROTO_VALUE_DISCOVERY_DATA|ZBX_PROTO_VALUE_AUTO_REGISTRATION_DATA),
		//	"host":DBFORBIX, 
		//	"data":[{"host":HOST_NAME,"key":KEY,"clock":CLOCK,"ns":NS},{},...],
		//	"clock":UNIXTIMESTAMP,
		//	"ns":NS}
		String str=new String("{\"request\":\""+type+"\",\"host\":\"DBFORBIX\",\"data\":[");
		//if(type.contains(ZBX_PROTO_VALUE_HISTORY_DATA)) str+="\"data\":[";
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
		str+="\"clock\":"+it.getClock()+",";
		str+="\"ns\":0}";		
		
//		if(it.getConfItem() instanceof Discovery){
//			
//			 /** {"request":"agent data","data":[
//			 * 		{"host":"zabdomis01","key":"net.if.discovery","value":"{\"data\":[{\"{#IFNAME}\":\"lo\"},{\"{#IFNAME}\":\"eno16780032\"}]}","clock":1481573917,"ns":417446137},
//			 * 		{"host":"zabdomis01","key":"vfs.fs.discovery","value":"{\"data\":[
//			 			{\"{#FSNAME}\":\"/\",\"{#FSTYPE}\":\"rootfs\"},{\"{#FSNAME}\":\"/sys\",\"{#FSTYPE}\":\"sysfs\"},{\"{#FSNAME}\":\"/proc\",\"{#FSTYPE}\":\"proc\"},{\"{#FSNAME}\":\"/dev\",\"{#FSTYPE}\":\"devtmpfs\"},{\"{#FSNAME}\":\"/sys/kernel/security\",\"{#FSTYPE}\":\"securityfs\"},				 * 			{\"{#FSNAME}\":\"/dev/shm\",\"{#FSTYPE}\":\"tmpfs\"},{\"{#FSNAME}\":\"/dev/pts\",\"{#FSTYPE}\":\"devpts\"},{\"{#FSNAME}\":\"/run\",\"{#FSTYPE}\":\"tmpfs\"},{\"{#FSNAME}\":\"/sys/fs/cgroup\",\"{#FSTYPE}\":\"tmpfs\"},
//			 			{\"{#FSNAME}\":\"/sys/fs/cgroup/systemd\",\"{#FSTYPE}\":\"cgroup\"},{\"{#FSNAME}\":\"/sys/fs/pstore\",\"{#FSTYPE}\":\"pstore\"},				 * 			{\"{#FSNAME}\":\"/sys/fs/cgroup/cpu,cpuacct\",\"{#FSTYPE}\":\"cgroup\"},{\"{#FSNAME}\":\"/sys/fs/cgroup/devices\",\"{#FSTYPE}\":\"cgroup\"},
//			            {\"{#FSNAME}\":\"/sys/fs/cgroup/perf_event\",\"{#FSTYPE}\":\"cgroup\"},{\"{#FSNAME}\":\"/sys/fs/cgroup/hugetlb\",\"{#FSTYPE}\":\"cgroup\"},				 * 			{\"{#FSNAME}\":\"/sys/fs/cgroup/cpuset\",\"{#FSTYPE}\":\"cgroup\"},{\"{#FSNAME}\":\"/sys/fs/cgroup/net_cls\",\"{#FSTYPE}\":\"cgroup\"},
//			            {\"{#FSNAME}\":\"/sys/fs/cgroup/memory\",\"{#FSTYPE}\":\"cgroup\"},				 * 			{\"{#FSNAME}\":\"/sys/fs/cgroup/freezer\",\"{#FSTYPE}\":\"cgroup\"},				 * 			{\"{#FSNAME}\":\"/sys/fs/cgroup/blkio\",\"{#FSTYPE}\":\"cgroup\"},				 * 			{\"{#FSNAME}\":\"/sys/kernel/config\",\"{#FSTYPE}\":\"configfs\"},
//			            {\"{#FSNAME}\":\"/\",\"{#FSTYPE}\":\"xfs\"},				 * 			{\"{#FSNAME}\":\"/proc/sys/fs/binfmt_misc\",\"{#FSTYPE}\":\"autofs\"},				 * 			{\"{#FSNAME}\":\"/dev/hugepages\",\"{#FSTYPE}\":\"hugetlbfs\"},				 * 			{\"{#FSNAME}\":\"/sys/kernel/debug\",\"{#FSTYPE}\":\"debugfs\"},
//			            {\"{#FSNAME}\":\"/dev/mqueue\",\"{#FSTYPE}\":\"mqueue\"},				 * 			{\"{#FSNAME}\":\"/proc/sys/fs/binfmt_misc\",\"{#FSTYPE}\":\"binfmt_misc\"},
//			            {\"{#FSNAME}\":\"/proc/fs/nfsd\",\"{#FSTYPE}\":\"nfsd\"},				 * 			{\"{#FSNAME}\":\"/var\",\"{#FSTYPE}\":\"xfs\"},{\"{#FSNAME}\":\"/var/lib/nfs/rpc_pipefs\",\"{#FSTYPE}\":\"rpc_pipefs\"},{\"{#FSNAME}\":\"/home\",\"{#FSTYPE}\":\"xfs\"},{\"{#FSNAME}\":\"/opt\",\"{#FSTYPE}\":\"xfs\"},{\"{#FSNAME}\":\"/tmp\",\"{#FSTYPE}\":\"xfs\"},{\"{#FSNAME}\":\"/boot\",\"{#FSTYPE}\":\"xfs\"},{\"{#FSNAME}\":\"/app\",\"{#FSTYPE}\":\"xfs\"},{\"{#FSNAME}\":\"/misc\",\"{#FSTYPE}\":\"autofs\"},{\"{#FSNAME}\":\"/net\",\"{#FSTYPE}\":\"autofs\"},{\"{#FSNAME}\":\"/run/user/354789\",\"{#FSTYPE}\":\"tmpfs\"},{\"{#FSNAME}\":\"/run/user/0\",\"{#FSTYPE}\":\"tmpfs\"},
//			            {\"{#FSNAME}\":\"/run/user/59425\",\"{#FSTYPE}\":\"tmpfs\"},
//			            {\"{#FSNAME}\":\"/run/user/361033\",\"{#FSTYPE}\":\"tmpfs\"}]
//			       }",
//			       "clock":1481573917,"ns":417779254}],
//			    "clock":1481573922,"ns":418957607}
//			*/
//			
//			//str+="{\"host\":\""+it.getHost()+"\",";
//			//str+="\"key\":\""+it.getKey()+"\",";
//			String data=it.getValue();
//			//str+=data.substring(1,data.length()-1)+",";
//			str+=data.substring(1,data.length()-1);
//			//str+="\"clock\":"+it.getClock();
//			//str+="\"ns\":\"0\"";
//			
//			
//			
//		}else{
//			str+="{\"host\":\""+it.getHost()+"\",";
//			str+="\"key\":\""+it.getKey()+"\",";
//			str+="\"value\":\""+it.getValue()+"\",";
//			str+="\"clock\":\""+it.getClock()+"\",";
//			str+="\"ns\":\"0\"}";			
//		}
		str+=",";
		return str;
	}
	
	private String closeJson(String type) {
		//{"request":(ZBX_PROTO_VALUE_HISTORY_DATA|ZBX_PROTO_VALUE_DISCOVERY_DATA|ZBX_PROTO_VALUE_AUTO_REGISTRATION_DATA),
		//	"host":DBFORBIX, 
		//	"data":[{"host":HOST_NAME,"key":KEY,"clock":CLOCK,"ns":NS},{},...],
		//	"clock":UNIXTIMESTAMP,
		//	"ns":NS}
		String str=new String();
		//if(type.contains(ZBX_PROTO_VALUE_HISTORY_DATA))	str+="]";
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
		result+=openJson(type);
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
		String type=ZBX_PROTO_VALUE_HISTORY_DATA;
		//if(item.getConfItem() instanceof Discovery)	type=ZBX_PROTO_VALUE_DISCOVERY_DATA;
		ZabbixItem[] items=new ZabbixItem[1];
		items[0]=item;
		result = encodeItems(items,type);		
		return result;
	}

}
