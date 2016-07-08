<?xml version="1.0" encoding="UTF-8"?>
<%--

    Copyright (C) 2011 Metropolitan Transportation Authority

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

<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:directive.page contentType="text/html" />
	<div id="topBox">
		<script type=text/x-handlebars>
			{{#view VehicleStatus.TopBarView}}
				<!--<input type="text" id="search" />-->
				<div id="lastUpdateBox">
					<label class="vehicleLabel">Last Update:</label>
					<label id="lastUpdate" class="vehicleLabel"></label>
				</div>
				<input type="button" id="refresh" value="Refresh" class="inlineFormButton"
					{{action "refreshClick" on="click" }}/>
				<div id="autoRefreshBox">
					<input type="checkbox" id="enableAutoRefresh" class="vehicleLabel"
						{{action "autoRefreshClick" on="click" }}>Auto Refresh:
					<label id="autoRefresh" class="vehicleLabel" {{action "refreshLabelClick" on="click"}}>30 sec</label>
				</div>
			{{/view}}
		</script>
	</div>
	<div id="mainBox">
			<script type=text/x-handlebars>
			{{#view VehicleStatus.FilterView}}
		<div id="filterBox">
			<label id="filterLabel">Filter by:</label>
			<ul id="filters">
				<li>
					<label class="vehicleLabel">Vehicle ID:</label>
					<input type="text" id="vehicleId" />
				</li>
				<li>
					<label class="vehicleLabel">Route:</label>
					<input type="text" id="route" />
				</li>
				<li>
					<label class="vehicleLabel">Depot:</label>
					<select name="depot" id="depot">
						<option selected="selected" value="all">All</option>
					</select>
				</li>
				<li>
					<label class="vehicleLabel">DSC:</label>
					<input type="text" id="dsc" />
				</li>
				<li>
					<label class="vehicleLabel">Inferred Phase:</label>
					<select name="inferredPhase" id="inferredPhase">
						<option selected="selected" value="all">All</option>
					</select>
				</li>
				<li>
					<label class="vehicleLabel">Pullout Status:</label>
					<select name="pulloutStatus" id="pulloutStatus">
						<option selected="selected" value="all">All</option>
					</select>
				</li>
			</ul>
			<div id="checkFilters">
				<div>
					<input type="checkbox" id="emergencyCheck" />
					<label class="vehicleLabel">Emergency Status</label>
				</div>
				<div>
					<input type="checkbox" id="formalInferrenceCheck" />
					<label class="vehicleLabel">Formal Inferrence</label>
				</div>
			</div>
			<div id="filterButtons">
				<input type="button" id="reset" value="Reset" {{action "resetFilters" on="click" }}/>
				<input type="button" id="apply" value="Apply" {{action "applyFilters" on="click" }}/>
			</div>
		</div>
		<div id="collapseBox">
			<s:url var="url" value="/css/img/arrow-right_12x12.png"/>
			<img src="${url}" alt="Not Found" id="collapse" title="Expand" {{action "toggleFilters" on="click"}}/>
		</div>
		{{/view}}
			</script>
		<div id="vehiclesBox">
			<script type=text/x-handlebars>
				{{#view VehicleStatus.VehicleView}}
					<table id="vehicleGrid" />
					<div id="pager" />	
				{{/view}}
			</script>
		</div>
	</div>
	<div id="bottomBox">
	<script type=text/x-handlebars>
		{{#view VehicleStatus.SummaryView}}
		<!-- This box has been deferred by MTA at this time
		<div id="scheduleBox" class="infoBox">
			<label class="vehicleLabel">Run/blocks scheduled to be active</label>
			<div id="scheduleInfo" class="boxData">
				<s:url var="url" value="/css/img/view-calendar-day.ico" />
				<img src="${url}" alt="Not found" />
				<label><s:property value=""/></label>
			</div>	
		</div>
		-->
		<div id="busBox" class="infoBox">
			<label class="vehicleLabel">Buses tracked in past 5 minutes</label>
			<div id="busInfo" class="boxData">
				<s:url var="url" value="/css/img/user-away-2.png" />
				<img src="${url}" alt="Not found" />
				<label id="vehiclesTrackedCount">{{vehiclesTracked}}</label>
			</div>	
		</div>
		<div id="inferrenceBox" class="infoBox">
			<label class="vehicleLabel">Buses inferred in revenue service</label>
			<div id="inferrenceInfo" class="boxData">
				<s:url var="url" value="/css/img/sign_dollar_icon.jpg" />
				<img src="${url}" alt="Not found" />
				<label id="revenueServiceCount"><s:property value=""/></label>
			</div>	
		</div>
		<div id="emergencyVehiclesBox" class="infoBox">
			<label class="vehicleLabel">Buses reporting emergency status</label>
			<div id="emergencyInfo" class="boxData">
				<s:url var="url" value="/css/img/dialog-warning-4.png" />
				<img src="${url}" alt="Not found" />
				<label id="emergencyCount"><s:property value=""/></label>
			</div>	
		</div>
		{{/view}}
		</script>
	</div>
