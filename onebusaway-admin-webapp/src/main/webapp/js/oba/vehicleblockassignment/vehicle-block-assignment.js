/*
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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

var csrfParameter = "";
var csrfHeader = "";
var csrfToken = "";


jQuery(function() {
    // these are provided by sec:csrfMetaTags
    csrfParameter = $("meta[name='_csrf_parameter']").attr("content");
    csrfHeader = $("meta[name='_csrf_header']").attr("content");
    csrfToken = $("meta[name='_csrf']").attr("content");

    var table = $('#blockSummaryTable').DataTable({
        "paging":false
    });

    $.fn.dataTable.ext.search.push(function(settings, data, dataIndex) {

        var vehicleIdInput =  table.cell(dataIndex,4).nodes().to$().find('input').val();
        var vehicleIdField = $('#findVehicleField').val().trim();
        var regex = new RegExp(vehicleIdField,"g");

        if(vehicleIdInput.match(regex)){
            return true;
        }
        return false;
    });

    // Search By Block
    $('#findBlockField').keyup(function (e) {
        table
            .column( 0 )
            .search( this.value )
            .draw();
    });

    // Search By VehicleID
    $('#findVehicleField').keyup(function (e) {
        table.draw();
    });
})

var delay = (function(){
    var timer = 0;
    return function(callback, ms){
        clearTimeout (timer);
        timer = setTimeout(callback, ms);
    };
})();