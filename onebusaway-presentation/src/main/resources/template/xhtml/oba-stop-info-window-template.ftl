<#--

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

-->
<div class="StopInfoWindowTemplate" style="display:none;">
  <h3 class="stopName">Stop Name</h3>
  <h4 class="stopDescription"><span class="stopCode"><@s.text name="StopNum"/></span><span class="stopDirection"> - <@s.text name="bound"/></span></h4>
  <div class="stopContent"><@s.property value="parameters.content" escapeHtml="false"/></div>
  <div class="routesSection">
  	<div><@s.text name="Routes"/>:</div>
  </div>
  <div class="clearBottom"></div>
</div>