package org.camouflage.format.po;

public class ParseException extends Exception {

	private static final long serialVersionUID = -3173727882355158967L;

	private int line = 0;
	
	public ParseException(int line, String message){
		super(message);
		this.line = line;
	}
	
	public ParseException(String message){
		super(message);
	}

	public ParseException(String message, Throwable cause){
		super(message,cause);
	}

	public ParseException(int line, String message, Throwable cause){
		super(message,cause);
		this.line = line;
	}

	
	
	/**
	 * @return the line
	 */
	public int getLine() {
		return line;
	}

	/**
	 * @param line the line to set
	 */
	public void setLine(int line) {
		this.line = line;
	}

	@Override
	public String getMessage() {
		if(line != 0){
			return "Line "+ line+ ": " + super.getMessage();
		}
		else
			return super.getMessage();
	}

}
