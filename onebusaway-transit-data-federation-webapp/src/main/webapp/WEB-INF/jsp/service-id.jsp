<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>

<body>
<h1>Service Id: <c:out value="${serviceId}"/></h1>
<ul>
	<c:forEach var="d" items="${dates}">
		<li><fmt:formatDate type="date" value="${d}" /></li>
	</c:forEach>
</ul>

</body>
</html>
