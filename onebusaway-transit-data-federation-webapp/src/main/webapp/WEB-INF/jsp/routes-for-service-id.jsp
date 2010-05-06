<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>

<body>
<h1>Routes:</h1>
<ul>
	<c:forEach var="route" items="${routes}">
		<li><a href="trips-for-service-id-and-route-id.action?serviceId=${serviceId}&routeId=${route.id}">${route.shortName} - ${route.longName}</a></li>
	</c:forEach>
</ul>
<h1>Dates:</h1>
<ul>
	<c:forEach var="d" items="${dates}">
		<li><fmt:formatDate type="date" value="${d}" /></li>
	</c:forEach>
</ul>

</body>
</html>
