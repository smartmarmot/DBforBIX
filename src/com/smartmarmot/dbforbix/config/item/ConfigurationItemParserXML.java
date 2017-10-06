package com.smartmarmot.dbforbix.config.item;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.smartmarmot.common.utils.SAXParserDBforBIX;
import com.smartmarmot.dbforbix.config.element.ConfigurationElementFactory;
import com.smartmarmot.dbforbix.config.element.IConfigurationElement;

public class ConfigurationItemParserXML implements IConfigurationItemParser {
	
	private static final Logger		LOG					= Logger.getLogger(ConfigurationItemParserXML.class);
	
	private String config;

	public ConfigurationItemParserXML(String _config) {		
		config=_config;
	}

	@Override
	public Set<IConfigurationElement> buildConfigurationElements() throws DocumentException {
		Set<IConfigurationElement> configurationElements=new HashSet<>();
		try{			
			//add constant header - DTD
			config="<!DOCTYPE parms SYSTEM \"./items/param.dtd\">"+config;
			//substitute special chars >, < for XML DOM parser
			config=preprocessZabbixConfig(config);
		}catch(Exception e){
			LOG.error("Exception while preprocessing XML configuration: "+e.getLocalizedMessage()+"\nBut we are still trying to construct XML DOM...");
		}
		
		Document doc = DocumentHelper.parseText(config);
		Element root = doc.getRootElement();
		String prefix = root.attributeValue("prefix","");
		for (Object xmlServerElement: root.elements("server")) {
			if (xmlServerElement instanceof Element)
				configurationElements.addAll(buildConfigurationElementsByXMLServer(prefix,(Element) xmlServerElement));
		}
		return configurationElements;
	}
	
	/**
	 * Substitute > and < characters on & gt; and & lt; if they are not XML tags. Used to prepare statement for standard JSON parser.
	 * XML should contain DTD file because we need to know the template and your XML language keywords!
	 * @param inputString - configuration from Zabbix configuration item
	 * @return String - preprocessed configuration
	 */
	private String preprocessZabbixConfig(String inputString) {		
		return SAXParserDBforBIX.replaceSpecialChars(inputString);
	}
	
	private Collection<IConfigurationElement> buildConfigurationElementsByXMLServer(String prefix, Element xmlServer) {
		Set<IConfigurationElement> configurationElements = new HashSet<>();
		for (Object xmlConfigurationObject: xmlServer.elements()) {
			if (xmlConfigurationObject instanceof Element) {
				Element xmlConfigurationElement = (Element) xmlConfigurationObject;
				configurationElements.add(buildConfigurationElementFromXML(prefix,xmlConfigurationElement));
			}
		}
		return configurationElements;
	}
	
	/**
	 * Builds configuration element from its XML representation	
	 * @param prefix 
	 * @param xmlConfigurationElement
	 * @return
	 * @throws InvalidParameterException
	 */
	private IConfigurationElement buildConfigurationElementFromXML(String prefix, Element xmlConfigurationElement) throws InvalidParameterException {
		String configurationElementType=xmlConfigurationElement.getName();
		int time = 60;
		try {
			time = Integer.parseInt(xmlConfigurationElement.attributeValue("time"));
		}
		catch (NumberFormatException ex) {
			LOG.warn("Invalid time value " + xmlConfigurationElement.attributeValue("time") + 
					" for configuration element "+xmlConfigurationElement.attributeValue("item")+
					"\nSet time to default value 60 seconds");
		}		
		//can be item or items
		String item_or_items=xmlConfigurationElement.attributeValue("item","")+
				xmlConfigurationElement.attributeValue("items","");
		String names=xmlConfigurationElement.attributeValue("names","");
		String noData=xmlConfigurationElement.attributeValue("nodata","");
		String type=xmlConfigurationElement.attributeValue("type","column");
		String query=xmlConfigurationElement.getTextTrim();
		
		IConfigurationElement configurationElement=ConfigurationElementFactory.buildConfigurationElement(
				configurationElementType, prefix, time, item_or_items, names, noData, type, query
				);
		return configurationElement;
	}
}
