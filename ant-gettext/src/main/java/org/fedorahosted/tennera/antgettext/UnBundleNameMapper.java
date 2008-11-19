package org.fedorahosted.tennera.antgettext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.util.FileNameMapper;

public class UnBundleNameMapper implements FileNameMapper
{
	// module/(locale)/(plugin)-(resource).po
//	private String from = "^[^/\\\\]+[/\\\\]([^/\\\\]+)[/\\\\]([^-]+)-(.*)[.]po$"; //$NON-NLS-1$
	// (module)/(plugin)-(resource)/(locale).po
	private String from = "^([^/\\\\]+)[/\\\\]([^-]+)-([^/\\\\]+)[/\\\\](.*)[.]po$"; //$NON-NLS-1$
	private String to;
	
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

	
	public String[] mapFileName(String sourceFileName) 
	{
		Matcher m = pattern.matcher(sourceFileName);
		if (m.matches())
		{
			StringBuilder sb = new StringBuilder();
//			String locale = m.group(1);
//			String bundle = m.group(2);
//			String resource = m.group(3);
			String module = m.group(1);
			String bundle = m.group(2);
			String resource = m.group(3);
			String locale = m.group(4);
			sb.append(module).append('/');
			sb.append(bundle).append('/');
			sb.append(resource.replace('.', '/'));
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
