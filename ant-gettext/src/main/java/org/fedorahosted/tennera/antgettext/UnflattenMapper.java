package org.fedorahosted.tennera.antgettext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.util.FileNameMapper;

public class UnflattenMapper implements FileNameMapper
{
	// (module)/(plugin)-(resource)/(locale).po
	private String from = "^([^/\\\\]+)[/\\\\]([^-]+)-([^/\\\\]+)[/\\\\](.*)[.]po$"; //$NON-NLS-1$
	
	private Pattern pattern;
	
	public UnflattenMapper() 
	{
		recompile();
	}

	private void recompile() 
	{
		pattern = Pattern.compile(from);
	}
	
	/**
	 * Changing the from pattern isn't really supported...
	 */
	public void setFrom(String from) 
	{
		this.from = from;
		recompile();
	}

	/**
	 * Changing the to pattern is definitely not supported...
	 */
	public void setTo(String to) 
	{
	}

	
	public String[] mapFileName(String sourceFileName) 
	{
		Matcher m = pattern.matcher(sourceFileName);
		if (m.matches())
		{
			StringBuilder sb = new StringBuilder();
			String module = m.group(1);
			String bundle = m.group(2);
			String resource = m.group(3);
			String locale = m.group(4);
			sb.append(module).append('/');
			sb.append(bundle).append('/');
			sb.append(resource.replace('.', '/'));
			sb.append('_').append(locale);
			sb.append(".properties"); //$NON-NLS-1$
			// m.replaceAll("$1/$2/$3_$4.properties") would be similar, 
			// but we want to replace "." in $3 with "/"
			return new String[]{sb.toString()};
		}
		else
		{
			return null;
		}
	}
}
