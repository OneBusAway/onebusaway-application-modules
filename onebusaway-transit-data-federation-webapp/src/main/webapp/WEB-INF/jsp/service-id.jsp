<%--

    Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
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
