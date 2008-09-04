package org.camouflage.format.po;

public class PropertyException extends Exception {

	private static final long serialVersionUID = 2932234066833781519L;
	
	public PropertyException(String message) {
    	super(message);
    }
	
    /**
     * Construct a PropertyException whose message field is set based on the 
     * name of the property and value.toString(). 
     * 
     * @param name the name of the property related to this exception
     * @param value the value of the property related to this exception
     */
    public PropertyException(String name, Object value) {
    	super("Invalid property '"+name+"': "+ value.toString() );
    }
	
}
