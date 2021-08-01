package com.flex.versatileapi.exceptions;

public class ODataParseException extends RuntimeException {
	public ODataParseException(String message) {
		super("ODataParseException:" + message);
	}

}