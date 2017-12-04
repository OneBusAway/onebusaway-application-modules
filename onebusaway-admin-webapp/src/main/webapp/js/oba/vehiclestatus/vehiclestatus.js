/*
 * Copyright (C) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function showVehiclePopup(vehicleId) {
	//alert("showVehiclePopup(" + vehicleId + ")");
	if (vehicleId == undefined || vehicleId == "") {
		var id = jQuery("#vehicleGrid").jqGrid('getGridParam', 'selrow');
		vehicleId = jQuery("#vehicleGrid").jqGrid('getRowData', id).vehicleId;
		if (vehicleId == undefined || vehicleId == "") { 
			//alert("vehicleId=" + vehicleId);
			return;
		}
	}
	
	//Change these values to style your modal popup
	var align = 'center';										//Valid values; left, right, center
	var top = 100; 												//Use an integer (in pixels)
	var padding = 10;											//Use an integer (in pixels)
	var backgroundColor = '#FFFFFF'; 							//Use any hex code
	var borderColor = '#000000'; 								//Use any hex code
	var borderWeight = 4; 										//Use an integer (in pixels)
	var borderRadius = 5; 										//Use an integer (in pixels)
	var fadeOutTime = 300; 										//Use any integer, 0 = no fade
	var disableColor = '#666666'; 								//Use any hex code
	var disableOpacity = 40; 									//Valid range 0-100
	var loadingImage = '../../css/img/loading.gif';	//Use relative path from this page	
	
	var source = './popup!input.action?vehicleId=' + vehicleId;	//Refer to any page on your server, external pages are not valid
	var width = 500; 					//Use an integer (in pixels)
	modalPopup(align, top, width, padding, disableColor, disableOpacity, backgroundColor, borderColor, borderWeight, borderRadius, fadeOutTime, source, loadingImage, createMaps);

};

var VehicleStatus = Ember.Application.create({
		
});

VehicleStatus.VehicleView = Ember.View.extend({
	tagName: "table",
	didInsertElement: function() {
		var controller = this.get('controller');
		controller.loadGridData();
	},
	controllerBinding: "VehicleStatus.VehiclesController"
});

VehicleStatus.FilterView = Ember.View.extend({
	tagName: "ul",
	didInsertElement: function() {
		var controller = this.get('controller');
		controller.loadFiltersData();
	},
	applyFilters: function() {
		var controller = this.get('controller');
		controller.search();
	},
	resetFilters: function() {
		var controller = this.get('controller');
		controller.reset();
	},
	toggleFilters : function() {
		//toggle filters
		$("#filterBox").animate({width:'toggle'},350);
		var imageSrc = $("#collapseBox #collapse").attr("src");
		if(imageSrc.indexOf("right") != -1) {
			$("#collapseBox #collapse").attr("src","../../css/img/arrow-left_12x12.png");
			$("#collapseBox #collapse").attr("title","Collapse");
			$("#vehicleGrid").jqGrid("setGridWidth", 710, true);
		} else {
			$("#collapseBox #collapse").attr("src","../../css/img/arrow-right_12x12.png");
			$("#collapseBox #collapse").attr("title","Expand");
			$("#vehicleGrid").jqGrid("setGridWidth", 865, true);
		}
	},
	controllerBinding: "VehicleStatus.FiltersController"
});

VehicleStatus.TopBarView = Ember.View.extend({
	refreshDialog: {},
	didInsertElement: function() {
		refreshDialog = $("<div id='refreshDialog'>" +
				"<input type='text' id='refreshRate'/>" +
		"seconds<input type='button' id='set' value='Set'/></div>").dialog({
		autoOpen: false,
		title: "Edit Refresh-rate",
		height: 100
		});
	},
	autoRefreshClick: function(event) {
		var controller = this.get('controller');
		controller.autoRefreshGrid(event.target.checked);
	},
	refreshClick: function() {
		var controller = this.get('controller');
		VehicleStatus.FiltersController.reset();
		controller.refreshGrid();
	},
	refreshLabelClick: function() {
		var controller = this.get('controller');
		//Clear previous autorefresh interval if set
		if($("#enableAutoRefresh").is(":checked")) {
			$("#enableAutoRefresh").removeAttr("checked");
			controller.autoRefreshGrid($("#enableAutoRefresh").checked);
		}
		//open dialog
		refreshDialog.dialog('open');
		var set = refreshDialog.find('#set');
		set.bind({'click' : function() {
			 $("#autoRefreshBox #autoRefresh").text($("#refreshRate").val() + " sec");
			 refreshDialog.dialog('close');
		}});
	},
	controllerBinding: "VehicleStatus.TopBarController"
});

VehicleStatus.SummaryView = Ember.View.extend({
	controllerBinding: "VehicleStatus.SummaryController"
});


/******************* Controllers ************************************/
VehicleStatus.ParametersController = Ember.ArrayController.create({
	content: [],
});

VehicleStatus.VehiclesController = Ember.ArrayController.create({
	content: [],
	loadGridData : function() {
		var grid = $("#vehicleGrid");
		grid.jqGrid({
			url: "vehicle-status!getVehicleData.action?ts=" + new Date().getTime(),
			datatype: "json",
			mType: "GET",
			colNames: ["Health","Vehicle Id", "Last Update", "Inferred Phase", "Inferred DSC, Route + Direction", "Observed DSC", "Pull-out", "Pull-in", "Details"],
			colModel:[ {name:'status',index:'status', width:70, sortable:false,
						formatter: function(cellValue, options) {
							var cellImg="error";
							
							if (!cellValue.indexOf("circle_green")){
								cellImg = "<a class=\"tooltip\" href=\"#\"><img src='../../css/img/" +cellValue +"' alt='Not Found' />"+
									"<span class=\"boxData\">GREEN: This bus is currently generating revenue: It is either in progress, deadhead, or experiencing a layover less than 2 minutes</span></a>";
							}
							if (!cellValue.indexOf("circle_red")){
								cellImg = "<a class=\"tooltip\" href=\"#\"><img src='../../css/img/" +cellValue +"' alt='Not Found' />"+
									"<span class=\"boxData\">RED: This bus is off track and has no inferred direction or route</span></a>";
							}							
							if (!cellValue.indexOf("circle_orange")){
								cellImg = "<a class=\"tooltip\" href=\"#\"><img src='../../css/img/" +cellValue +"' alt='Not Found' />"+
									"<span class=\"\">ORANGE: This bus is off track, but has an inferred direction or route</span></a>";
							}

							return cellImg;
						}}, 
			           {name:'vehicleId',index:'vehicleId', width:80}, 
			           {name:'lastUpdate',index:'lastUpdate', width:80}, 
			           {name:'inferredPhase',index:'inferredPhase', width:110}, 
			           {name:'inferredDestination',index:'inferredDestination', width:170, sortable:false}, 
			           {name:'observedDSC',index:'observedDSC', width:80}, 
			           {name:'formattedPulloutTime',index:'pulloutTime', width:80},
			           {name:'formattedPullinTime',index:'pullinTime', width:80},
			           {name:'details',index:'details', width:75, 
			        	formatter: function(cellValue, options) {
			        	   var linkHtml = "<a href='javascript:showVehiclePopup(" + cellValue + ");' style='color:blue'>" + "Details" + "</a>";
			        	   return linkHtml;
			           },
			            sortable:false}
			         ],
			height: "532",
			width: "auto",
			viewrecords: true,
			loadonce:false,
			jsonReader: {
				root: "rows",
			    page: "page",
			    total: "total",
			    records: "records",
				repeatitems: false
			},
			pager: "#pager",
			loadComplete: function(data) {
				//Update the time
				var lastUpdateTime = new Date();
				var time = function() {
					var hours = lastUpdateTime.getHours();
					var meridian;
					if(hours > 12) {
						hours = hours - 12;
						meridian = "PM";
					} else {
						if(hours == 12) {
							meridian = "PM";
						} else {
							meridian = "AM";
						}
					}
					var minutes = lastUpdateTime.getMinutes();
					if(minutes < 10) {
						minutes = "0" + minutes;
					}
					return  hours + ":" +  minutes + " " +meridian + " , " +lastUpdateTime.toDateString();
				}
				
				$("#lastUpdateBox #lastUpdate").text(time);
				
				//Adjust height of filter box according to height of the grid
				var gridHeight = $("#gbox_vehicleGrid").height();
				$("#mainBox #filterBox").height(gridHeight);
				$("#mainBox #collapseBox").height(gridHeight);
				
				//Add zebra stripes to the grid
			    $("tr.jqgrow:odd").css("background", "#DDDDDC");

				$.each(data.rows, function(i) {
					//Highlight inferred and observed DSC columns if they are different
					if(data.rows[i].inferredDSC != null && 
							(data.rows[i].observedDSC != data.rows[i].inferredDSC)) {
						//Change observed DSC font color if it is not one of the known out of
						//service DSCs
						if(!(data.rows[i].observedDSC == 6 || data.rows[i].observedDSC == 11 ||
								data.rows[i].observedDSC == 12 || data.rows[i].observedDSC == 22)) {
							grid.jqGrid('setCell', i+1, "observedDSC", "", {'color':'red'});
							grid.jqGrid('setCell', i+1, "observedDSC", "", {'font-weight':'bold'});
						}
						grid.jqGrid('setCell', i+1, "observedDSC", "", {'background-color':'#FFCCCC'});
						grid.jqGrid('setCell', i+1, "inferredDestination", "", {'background-color':'#FFCCCC'});
					}
				});
				
				//load statistics data once grid is refreshed 
				VehicleStatus.SummaryController.getStatistics();
			},
			postData: {
				vehicleId: function() {return $("#filters #vehicleId").val();},
				route: function() {return $("#filters #route").val();},
				depot: function() {return $("#filters #depot option:selected").val();},
				dsc: function() {return $("#filters #dsc").val();},
				inferredPhase: function() {return $("#filters #inferredPhase option:selected").val();},
				pulloutStatus: function() {return $("#filters #pulloutStatus option:selected").val();},
				emergencyStatus: function() {return $("#checkFilters #emergencyCheck").is(':checked');},
				formalInferrence: function() {return $("#checkFilters #formalInferrenceCheck").is(':checked');}
			},
		}).navGrid("#pager", {edit:false,add:false,del:false,search:false,refresh:false });
	}
});

VehicleStatus.FiltersController = Ember.ArrayController.create({
	content: [],
	loadFiltersData: function() {
		$.ajax({
			type: "GET",
			url: "../../filters/vehicle-filters.xml",
			dataType: "xml",
			success: function(xml) {
				//Add depot options
				$(xml).find("Depot").each(function(){
					$("#depot").append("<option value=\"" +$(this).text() + "\"" + ">" +$(this).text() + "</option>");
				});
				//Add inferred state options
				$(xml).find("InferredPhase").each(function(){
					$("#inferredPhase").append("<option value=\"" +$(this).text() + "\"" + ">" +$(this).text() + "</option>");
				});
				//Add pullout options
				$(xml).find("PulloutStatus").each(function(){
					$("#pulloutStatus").append("<option value=\"" +$(this).text() + "\"" + ">" +$(this).text() + "</option>");
				});
			},
			error: function(request) {
				alert("Error: " + request.statusText);
			}
		});
	},
	search: function() {
		$("#vehicleGrid").jqGrid('setGridParam', {search:true, page:1});
		$("#vehicleGrid").trigger("reloadGrid");
	},
	reset: function() {
		var filters = $("#filters");
		filters.find("input:text").val("");
		filters.find("select").val("all");
		$("#checkFilters #emergencyCheck").removeAttr("checked");
		$("#checkFilters #formalInferrenceCheck").removeAttr("checked");
	}
});

VehicleStatus.TopBarController = Ember.ArrayController.create({
	content: [],
	interval: 0,
	autoRefreshGrid: function(checked) {
		if(checked) {
			$("#vehicleGrid").jqGrid('setGridParam', {search:false, page:1});
			var refreshInterval = $("#autoRefreshBox #autoRefresh").text().split(/ +/)[0];
			interval = setInterval(function(){$("#vehicleGrid").trigger("reloadGrid");},refreshInterval * 1000);
		} else {
			window.clearInterval(interval);
		}
	},
	refreshGrid: function() {
		$("#vehicleGrid").jqGrid('setGridParam', {search:false, page:1});
		$("#vehicleGrid").trigger("reloadGrid");
	}
});

VehicleStatus.SummaryController = Ember.ArrayController.create({
	content: [],
	getStatistics: function() {
		$.ajax({
			type: "GET",
			url: "vehicle-status!getStatistics.action?ts=" + new Date().getTime(),
			dataType: "json",
			success: function(response) {
				$("#emergencyVehiclesBox #emergencyCount").text(response.vehiclesInEmergency);
				$("#inferrenceBox #revenueServiceCount").text(response.vehiclesInRevenueService);
				$("#busBox #vehiclesTrackedCount").text(response.vehiclesTracked);
				/*this.set('content', []);
				var statistics = VehicleStatus.Statistics.create({
					vehiclesTracked: response.vehiclesTracked,
					revenueServiceVehicleCount: response.vehiclesInRevenueService,
					emergencyVehicleCount: response.vehiclesInEmergency
				});
				this.pushObject(statistics);*/
			},
			error: function(request) {
				alert("Error: " + request.statusText);
			}
		});
	}
});

/******************* Model ************************************/
VehicleStatus.Statistics = Ember.Object.extend({
	vehiclesTracked: 0,
	revenueServiceVehicleCount: 0,
	emergencyVehicleCount : 0
});


