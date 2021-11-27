package com.pangu.framework.console.exception;

public abstract class CommandException extends Exception {

	private static final long serialVersionUID = 8734255937189347706L;

	public CommandException() {
		super();
	}

	public CommandException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandException(String message) {
		super(message);
	}

	public CommandException(Throwable cause) {
		super(cause);
	}
	
}
