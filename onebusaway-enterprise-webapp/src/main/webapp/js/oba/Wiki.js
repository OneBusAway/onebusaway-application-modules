/*
 * Copyright (c) 2011 Metropolitan Transportation Authority
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

var OBA = window.OBA || {};

OBA.Wiki = function() {	
	
	var theWindow = jQuery(window),
		menuBar = jQuery("#cssmenu1"),
		mainbox = jQuery("#mainbox");
	
	var resize = function() {
		var w = theWindow.width();
		
		if (w <= 1024) {
			mainbox.css("width", "960px");
		} else {
			mainbox.css("width", w - 150); // 75px margin on each 
										   // side for dropdown menus
		}

		// size set so we can have MTA menu items calculate their widths properly
		menuBar.width(mainbox.width());
	};

	function addResizeBehavior() {
		resize();

		// call when the window is resized
		theWindow.resize(resize);
	}
	
	function googleTranslateElementInit() {
		
		var translate_element = jQuery("#google_translate_element");		
		translate_element.click(function (e) {
			e.preventDefault();
			translate_element.html(' ')
							 .attr('src','//translate.google.com/translate_a/element.js?cb=googleTranslateElementInit');
			new google.translate.TranslateElement({pageLanguage: 'en', 
				layout: google.translate.TranslateElement.InlineLayout.SIMPLE}, 'google_translate_element');
			translate_element.unbind('click');
		});						
	}
		
	return {
		initialize: function() {
			addResizeBehavior();
			googleTranslateElementInit();
		}
	};
};

//for IE: only start using google maps when the VML/SVG namespace is ready
if(jQuery.browser.msie) {
	window.onload = function() { OBA.Wiki().initialize(); };
} else {
	jQuery(document).ready(function() { OBA.Wiki().initialize(); });
}