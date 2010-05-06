<!--
  Copyright 2008 Brian Ferris

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy of
  the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  License for the specific language governing permissions and limitations under
  the License.
-->
<!DOCTYPE html PUBLIC 
	"-//W3C//DTD XHTML 1.1 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator"%>
<%@taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html lang="en">
<head>
<title><decorator:title default="Struts Starter" /></title>
<link rel="stylesheet" type="text/css" href="<c:url value='${resources.common.decoratorCSS.url}'/>"  media="all"/>
<link rel="icon" type="image/png" href="<c:url value='${resources.common.imageBusIcon.url}'/>" />
<decorator:head />
</head>
<body id="page-home">
<div id="page">
<div id="header"><span><a href="<%=request.getContextPath()%>/index.html">Real-Time</a></span> <span><a
    href="mailto:contact@onebusaway.org">Feedback?</a></span> <span><a
    href="http://code.google.com/p/onebusaway/issues/list">Bugs?</a></span> <span><a
    href="<%=request.getContextPath()%>/links.html">Links</a></span> <span><a
    href="<%=request.getContextPath()%>/about.html">About</a></span></div>
<decorator:body /></div>

<!-- Google Analytics -->
<script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
var pageTracker = _gat._getTracker("UA-2423527-7");
pageTracker._initData();
pageTracker._trackPageview();
</script>

</body>
</html>