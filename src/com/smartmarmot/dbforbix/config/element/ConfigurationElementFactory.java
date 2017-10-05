package com.smartmarmot.dbforbix.config.element;

import java.security.InvalidParameterException;

public class ConfigurationElementFactory {

	public static IConfigurationElement buildConfigurationElement(String _configurationElementType, 
			String _prefix, int _time, String _item_or_items, String _names, String _noData, String _type, String _query) 
					throws InvalidParameterException {
		
		IConfigurationElement configurationElement = null;		
		switch (_configurationElementType) {
		case "discovery": {
			configurationElement = new DiscoveryConfigurationElement(_prefix, _time, _item_or_items, _names, _query);
		}
		break;
		case "query": {
			configurationElement = new SimpleConfigurationElement(_prefix,_time,_item_or_items,_noData,_query);
		}
		break;					
		case "multiquery": {							
			if (_type.equalsIgnoreCase("column"))
				configurationElement = new MultiColumnConfigurationElement(_prefix, _time, _item_or_items, _noData, _query);
			else
				configurationElement = new MultiRowConfigurationElement(_prefix, _time, _item_or_items, _noData, _query);
		}
		break;
		default: throw new InvalidParameterException("Unknown configuration element type: "+_configurationElementType);				
	}
		return configurationElement;
	}

}
