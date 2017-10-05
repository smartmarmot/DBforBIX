package com.smartmarmot.dbforbix.config.item;

import java.util.Set;

import org.dom4j.DocumentException;

import com.smartmarmot.dbforbix.config.element.IConfigurationElement;

public interface IConfigurationItemParser {

	Set<IConfigurationElement> buildConfigurationElements() throws DocumentException;

}
