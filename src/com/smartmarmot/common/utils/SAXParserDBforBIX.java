package com.smartmarmot.common.utils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;



public class SAXParserDBforBIX {
	//max iterations
	private static final int maxIterations=1000;
	
	//special characters/strings to replace	
	private static final Map<String,String> mStringToReplace =new HashMap<String, String>() {{
	    put(">", "&gt;");
	    put("<", "&lt;");
	}};

	/**
	 * Parses input XML string trying to fix "<" or ">" signs. Iterates maximum 1000 times and then exits.
	 * Throws XMLSyntaxUnrecoverableException
	 * @param inputXMLString
	 * @return
	 * @throws XMLDBforBIXUnrecoverableException
	 */
	public static String replaceSpecialChars(String inputXMLString)throws XMLDBforBIXUnrecoverableException{
		String result=inputXMLString;
		SAXParser parser = null;
		InputStream inputStream = null;
		SAXParserFactory parserFactor = SAXParserFactory.newInstance();
		SAXHandler handler = new SAXHandler();

		try {
			parser = parserFactor.newSAXParser();

		} catch (ParserConfigurationException|SAXException e) {
			e.printStackTrace();
			throw new XMLDBforBIXUnrecoverableException("Unhandled exception came from SAX XML parser.");
		}
		
		int i=0;
		for(;i++<maxIterations;){		
			try {
				inputStream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8.name()));
				parser.parse(inputStream,handler);
				break;
			} catch (SAXException e) {				
				if(handler.isXMLSyntaxError()){
					result = correctSymbols(result,handler.getLineNumber(),handler.getColumnNumber());
					handler.reset();
					parser.reset();
				}else{
					e.printStackTrace();
					throw new XMLDBforBIXUnrecoverableException("Unhandled exception came from SAX XML parser.");					
				}
			} catch (UnsupportedEncodingException e1) {				
				e1.printStackTrace();
				throw new XMLDBforBIXUnrecoverableException("Input XML string encoding is wrong.");
			} catch (IOException e) {
				e.printStackTrace();
				throw new XMLDBforBIXUnrecoverableException("Unhandled exception came from SAX XML parser.");
			}
		}
		
		if (i>maxIterations) 
			throw new XMLDBforBIXUnrecoverableException("Found more than "+maxIterations+" '>', '<' signs (outside of tags!) in XML. Please change them to &gt; or &lt; manually or contact developer.");

		return result;
	}
	
	private static String correctSymbols(String inputString, int lineNumber, int columnNumber) throws XMLDBforBIXUnrecoverableException{
		String result=inputString;
		boolean specialCharsNotFound=true;
		
		//find starting position of lineNumber
		int fromIndex=0;
		for(int i=0; i++<lineNumber-1; fromIndex=result.indexOf('\n', ++fromIndex));
		//getting exact bad character position
		fromIndex+=columnNumber-1;
		//If special char found then replace it and get out
		for(String s:mStringToReplace.keySet()){
			if(result.regionMatches(fromIndex, s, 0, s.length())){
				specialCharsNotFound=false;
				result=result.substring(0, fromIndex)+mStringToReplace.get(s)+result.substring(fromIndex+s.length());
				break;
			}
		}
		
		if(specialCharsNotFound)
			throw new XMLDBforBIXUnrecoverableException("Couldn't identified any of special char that I'm able to replace. Please check XML correctness manually!");
		return result;
	}
	
}
/**
 * The Handler for SAX Events.
 */
class SAXHandler extends DefaultHandler {

	//XML correction
	private boolean isXMLSyntaxError=false;
	private int lineNumber=0;
	private int columnNumber=0;
	

	/**
	 * Resets internal errors indicators.
	 */
	public void reset() {
		setXMLSyntaxError(false);
		setLineNumber(0);
		setColumnNumber(0);		
	}

	
	@Override
	public void error(SAXParseException e){
		setXMLSyntaxError(true);
		setLineNumber(e.getLineNumber());
		setColumnNumber(e.getColumnNumber());
	}
	
	@Override
	public void fatalError(SAXParseException e){		
		error(e);		
	}

	public boolean isXMLSyntaxError() {
		return isXMLSyntaxError;
	}

	private void setXMLSyntaxError(boolean isXMLSyntaxError) {
		this.isXMLSyntaxError = isXMLSyntaxError;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	private void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	private void setColumnNumber(int columnNumber) {
		this.columnNumber = columnNumber;
	}
}
