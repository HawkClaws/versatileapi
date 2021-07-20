package com.flex.versatileapi.exceptions;

public class ODataParseException extends Exception {
	public ODataParseException(String message) {
		super("ODataParseException:" + message);
	}

}