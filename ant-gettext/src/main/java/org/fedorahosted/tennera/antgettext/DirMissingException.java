package org.fedorahosted.tennera.antgettext;

public class DirMissingException extends RuntimeException {

	public DirMissingException() {
	}

	public DirMissingException(String message) {
		super(message);
	}

	public DirMissingException(Throwable cause) {
		super(cause);
	}

	public DirMissingException(String message, Throwable cause) {
		super(message, cause);
	}

}
