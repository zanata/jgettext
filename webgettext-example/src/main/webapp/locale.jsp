<%@page import="org.fedorahosted.tennera.webgettext.I18nUtil"%>
<%
String name = request.getParameter("locale"); //$NON-NLS-1$
session.setAttribute("locale", I18nUtil.toLocale(name)); //$NON-NLS-1$
Cookie cookie = new Cookie("locale", name); //$NON-NLS-1$
cookie.setMaxAge(60*60*24*365);
cookie.setPath("/"); //$NON-NLS-1$
response.addCookie(cookie);
String from = request.getParameter("from"); //$NON-NLS-1$
if (from == null)
   from = "ServerInfo.jsp"; //$NON-NLS-1$
response.sendRedirect(from);
%>
