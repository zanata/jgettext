<%@page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@page import="java.util.Locale"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%
Map locSelect = new HashMap();
String reqLocale = request.getLocale().toString();
locSelect.put(reqLocale, "selected"); //$NON-NLS-1$
pageContext.setAttribute("selected", locSelect); //$NON-NLS-1$
String[] locales = 
{
	"en", "en_AA", "xxx", "ja", "es", "pt_BR", "de", "zh_CN" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
//	"el",	"fi",	"fr",	"it",	"nl",	"pl",	"pt",	"sk",	"sr",	"sr_LATN",	"sv"
};
%>
<%@page import="org.fedorahosted.tennera.webgettext.I18nUtil"%>
<form action="locale.jsp" method="get">
	<input type="hidden" name="from" value="${param.from}"/>
	<label for="locale">Locale:</label>
	<select name="locale" id="locale" onchange="this.form.submit()">
	<% 
	for(int i=0; i<locales.length; i++)
	{
		String lName = locales[i];
		Locale loc = I18nUtil.toLocale(lName);
	%>	<option <%= reqLocale.equals(lName) ? "selected" : "" //$NON-NLS-1$ //$NON-NLS-2$
			%> value="<%= lName %>"><%= loc.getDisplayName(loc) %></option> 
	<%
	}
	%>
	</select>
	<input type="submit" value="OK" />
</form>
