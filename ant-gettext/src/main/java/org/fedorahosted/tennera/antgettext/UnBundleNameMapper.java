package org.fedorahosted.tennera.antgettext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.util.FileNameMapper;

public class UnBundleNameMapper implements FileNameMapper
{
	private String from = "^([^/\\\\]+)[/\\\\]([^-]+)-(.*)[.]po$"; //$NON-NLS-1$
	private String to;
	private String locale;
	private boolean directoryPerLocale;
	
	private Pattern pattern;
	
	public UnBundleNameMapper() 
	{
		recompile();
	}

	private void recompile() 
	{
		pattern = Pattern.compile(from);
	}
	
	public void setFrom(String from) 
	{
		this.from = from;
		recompile();
	}

	public void setTo(String to) 
	{
		this.to = to;
	}
	
	public void setLocale(String locale) 
	{
		this.locale = locale;
	}
	
	public void setDirectoryPerLocale(boolean directoryPerLocale) 
	{
		this.directoryPerLocale = directoryPerLocale;
	}
	
	public String[] mapFileName(String sourceFileName) 
	{
		Matcher m = pattern.matcher(sourceFileName);
		if (m.matches())
		{
			String bundle, resource;
			StringBuilder sb = new StringBuilder();
			switch (m.groupCount())
			{
			case 0: case 1: default: return null;
			case 2:
				bundle = m.group(1);
				resource = m.group(2);
				break;
			case 3:
				sb.append(m.group(1)).append('/');
				bundle = m.group(2);
				resource = m.group(3);
				break;
			}
			if (directoryPerLocale)
				sb.append(bundle).append('/');
			sb.append(resource.replace('.', '/'));
			if (locale != null)
				sb.append('_').append(locale);
			sb.append(".properties"); //$NON-NLS-1$
			return new String[]{sb.toString()};
		}
		else
		{
			return null;
		}
	}
}
