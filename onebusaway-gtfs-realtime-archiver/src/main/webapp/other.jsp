<!DOCTYPE html>
<head>
 <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/leaflet.css" />
 <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/jquery-ui.css" />
 <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/jquery-ui-timepicker-addon.css" />
 <script src="<%= request.getContextPath() %>/resources/javascript/leaflet.js"></script>
 <script src="<%= request.getContextPath() %>/resources/javascript/leafletRotatedMarker.js"></script>
 <script src="<%= request.getContextPath() %>/resources/javascript/jquery-2.1.4.min.js"></script>
 <script src="<%= request.getContextPath() %>/resources/javascript/jquery-ui.min.js"></script>
 <script src="<%= request.getContextPath() %>/resources/javascript/jquery-ui-timepicker-addon.js"></script>
  <style>
  body {
    padding: 0;
    margin: 0;
  }
  html, body, #map {
	height: 100%;
    width: 100%;
  }
  /* For the params menu */
  #controls {
    background: lightgrey;
	position:absolute;
	top:10px;
	right:10px;
	border-radius: 25px;
	padding: 2%;
	border: 2px solid black;
  }
  
  .datetime {
    width: 60px;
  }
  
  // rectangular markers. these can be deleted after triangles.
  div.avlMarker {
    background-color: #ff7800;
    border-color: black;
    border-radius: 4px;
    border-style: solid;
    border-width: 1px;
    width:7px;
    height:7px;
  }
  
  div.avlTriangle {
    width: 0px;
    height: 0px;
    border-bottom: 10px solid #ff7800;
    border-left: 5px solid transparent;
    border-right: 5px solid transparent
  }
 
  </style>
</head>
<body>
<div id="map"></div>
<div id="controls">

<table>
<tr><td>Agencies:</td><td><select id="agencies"></select></td></tr>
<tr><td>Routes:</td><td><select id="routes"></select></td></tr>
<tr><td>Vehicles:</td><td><select id="vehicles"></select></td></tr>
<tr><td>Start time:</td><td><input type="text" id="startTime" class="datetime "/></td></tr>
<tr><td>End time:</td><td><input type="text" id="endTime" class="datetime" /></td></tr>
</table>
  
</div>
</body>
<script>
var contextPath = "<%= request.getContextPath() %>";
</script>
<script src="<%= request.getContextPath() %>/resources/javascript/map-utils.js"></script>
<script src="<%= request.getContextPath() %>/resources/javascript/map.js"></script>
