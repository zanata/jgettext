package org.camouflage.format.po;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public interface POMarshaller {

	public static final String PO_NOWRAP = "po.format.no-wrap";
	public static final String PO_INDENT = "po.format.indent";
	
	public abstract void marshall(POFile file, Writer writer)
			throws IOException;

	public abstract void marshall(POFile file, OutputStream stream)
			throws IOException;

	public abstract void marshall(POEntry entry, OutputStream stream)
			throws IOException;

	public abstract void marshall(POEntry entry, Writer writer)
			throws IOException;

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