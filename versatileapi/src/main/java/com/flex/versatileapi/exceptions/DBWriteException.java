package com.flex.versatileapi.exceptions;

public class DBWriteException extends RuntimeException {
	public DBWriteException(String message) {
		super("DBWriteException:" + message);
	}
}
