<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>

<body>
<ul>
	<c:forEach var="trip" items="${trips}">
		<li>${trip.tripHeadsign} - ${trip.id}</li>
	</c:forEach>
</ul>
</body>
</html>
