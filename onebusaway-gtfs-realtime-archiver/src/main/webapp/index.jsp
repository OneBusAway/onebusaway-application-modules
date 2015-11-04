<%--
        
    Copyright (C) 2015 Cambridge Systematics, Inc.
    
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
<html>
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
<tr><td>Start time:</td><td><input type="text" id="startTime" class="datetime"/></td></tr>
<tr><td>End time:</td><td><input type="text" id="endTime" class="datetime"/></td></tr>
<tr><td><input type="button" id="submit" value="Submit"></td></tr>
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
</html>
