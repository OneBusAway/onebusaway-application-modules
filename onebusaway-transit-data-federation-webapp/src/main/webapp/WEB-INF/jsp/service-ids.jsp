<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>

<body>
<ul>
	<c:forEach var="id" items="${ids}">
		<li><a href="service-id.action?serviceId=${id}">${id}</a></li>
	</c:forEach>
</ul>
</body>
</html>
