package com.smartmarmot.common.utils;

public class XMLDBforBIXUnrecoverableException extends RuntimeException {	
	/**
	 * 
	 */
	private static final long serialVersionUID = -335428024493806460L;
	//Parameterless Constructor
    public XMLDBforBIXUnrecoverableException() {super("Unrecoverable syntax error has been found. Please check XML correctness!");}
    //Constructors that accept parameters
    public XMLDBforBIXUnrecoverableException(String msg) { super(msg); }  
    public XMLDBforBIXUnrecoverableException(Throwable cause) { super(cause); }  
    public XMLDBforBIXUnrecoverableException(String msg, Throwable cause) { super(msg, cause); } 
}
