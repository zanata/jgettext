package org.camouflage.format.po;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public interface POUnmarshaller {

	public static final String IGNORE_INVALID_CHARSET = "po.parser.ignore-invalid-charset";
	public static final String DEFAULT_CHARSET = "po.parser.default-charset";
	public static final String OVERRIDE_CHARSET = "po.parser.override-charset";
	
	public abstract POFile unmarshall(InputStream stream) throws ParseException;

	public abstract POFile unmarshall(String filename)
			throws FileNotFoundException, ParseException;

	public abstract POFile unmarshall(File file)
	throws FileNotFoundException, ParseException;
	
	/**
	 * Set the particular property in the underlying implementation of 
	 * <tt>POMarshaller</tt>.  This method can only be used to set one of
	 * the standard defined properties above or a provider specific
	 * property.  Attempting to set an undefined property will result in
	 * a PropertyException being thrown.  See <a href="#supportedProps">
	 * Supported Properties</a>.
	 *
	 * @param name the name of the property to be set. This value can either
	 *              be specified using one of the constant fields or a user 
	 *              supplied string.
	 * @param value the value of the property to be set
	 *
	 * @throws PropertyException when there is an error processing the given
	 *                            property or value
	 * @throws IllegalArgumentException
	 *      If the name parameter is null
	 */
	public abstract void setProperty(String name, Object value)
			throws PropertyException;

	/**
	 * Get the particular property in the underlying implementation of 
	 * <tt>POMarshallerr</tt>.  This method can only be used to get one of
	 * the standard defined properties above or a provider specific
	 * property.  Attempting to get an undefined property will result in
	 * a PropertyException being thrown.  See <a href="#supportedProps">
	 * Supported Properties</a>.
	 *
	 * @param name the name of the property to retrieve
	 * @return the value of the requested property
	 *
	 * @throws PropertyException
	 *      when there is an error retrieving the given property or value
	 *      property name
	 * @throws IllegalArgumentException
	 *      If the name parameter is null
	 */
	public abstract Object getProperty(String name) throws PropertyException;
	
}