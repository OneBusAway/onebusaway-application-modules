<!DOCTYPE html>
<head>
 <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/leaflet.css" />
 <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/jquery.datetimeentry.css" />
 <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/style.css" />
 <script src="<%= request.getContextPath() %>/resources/javascript/leaflet.js"></script>
 <script src="<%= request.getContextPath() %>/resources/javascript/leafletRotatedMarker.js"></script>
 <script src="<%= request.getContextPath() %>/resources/javascript/jquery-2.1.4.min.js"></script>
 <script src="<%= request.getContextPath() %>/resources/javascript/jquery.plugin.min.js"></script>
 <script src="<%= request.getContextPath() %>/resources/javascript/jquery.datetimeentry.min.js"></script>
 <script src="<%= request.getContextPath() %>/resources/javascript/animation.js"></script>
</head>
<body>
<div id="map"></div>

<div id="controls">

<table>
<tr><td>Agencies:</td><td><select id="agencies"></select></td></tr>
<tr><td>Routes:</td><td><select id="routes"></select></td></tr>
<tr><td>Vehicles:</td><td><select id="vehicles"></select></td></tr>
<tr><td>Start time:</td><td><input type="text" id="startTime" class="datetime" disabled/></td></tr>
<tr><td>End time:</td><td><input type="text" id="endTime" class="datetime" disabled /></td></tr>
</table>
  
</div>

<div id="playbackContainer">
  <div id="playback">
    <input type="image" src="<%= request.getContextPath() %>/resources/images/media-seek-backward.svg" id="playbackPrev" />
    <input type="image" src="<%= request.getContextPath() %>/resources/images/media-skip-backward.svg" id="playbackRew" />
    <input type="image" src="<%= request.getContextPath() %>/resources/images/media-playback-start.svg" id="playbackPlay" />
    <input type="image" src="<%= request.getContextPath() %>/resources/images/media-skip-forward.svg" id="playbackFF" /> 
    <input type="image" src="<%= request.getContextPath() %>/resources/images/media-seek-forward.svg" id="playbackNext" /> <br>
    <span id="playbackRate">1X</span> <br>
    <span id="playbackTime">00:00:00</span>
  </div>
</div>

</body>
<script>
var contextPath = "<%= request.getContextPath() %>";
</script>
<script src="<%= request.getContextPath() %>/resources/javascript/map-utils.js"></script>
<script src="<%= request.getContextPath() %>/resources/javascript/map.js"></script>
