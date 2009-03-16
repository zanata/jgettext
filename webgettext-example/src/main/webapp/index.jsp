<%@page isELIgnored="false" %>
<html>
<head>
   	<title>${messages["Title of page"]}</title>
</head>
<body>
<%--
--%>
<div id="language">

<jsp:include page="localeselect.jsp">
   <jsp:param name="from" value="index.jsp" />	
</jsp:include>

</div>
    <img src="logo.png" alt="${messages["Project Logo"]}" />
    <h1>${messages["ABC {0}{1}{2}"]["1"][2]['3']}</h1>


    <p align="left"><font size="1"><b>${messages["Default locale: "]}</b><%=java.util.Locale.getDefault()%></font></p>
    <p align="left"><font size="1"><b>${messages["User locale: "]}</b><%=org.fedorahosted.tennera.webgettext.I18nUtil.getRequestLocale()%></font></p>

</body>
