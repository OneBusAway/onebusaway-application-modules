/*
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
var oba_where_sign_stop = function() {
	
	var getScrollToBottomTimeForHeight = function(height) {
		return height / 200 * 1000;
	};

	var getScrollToTopTimeForHeight = function(height) {
		return height / 2000 * 1000;
	};

	var registerScrollToBottom = function() {
		
		var body = jQuery('body');
		var scroll = body.height() - jQuery(window).height();
		var time = 29 * 1000 - getScrollToBottomTimeForHeight(scroll);
		
		window.setTimeout(scrollToBottom, 20*1000);
	};

	var scrollToBottom = function() {
		
		var body = jQuery('body');
		var scrollTop = body.scrollTop();
		
		var top = body.height() - jQuery(window).height();
		var time = getScrollToBottomTimeForHeight(top - scrollTop);
		
		jQuery('html, body').stop().animate({
			scrollTop: top 
		}, time, 'linear', registerScrollToTop);
	};

	var registerScrollToTop = function() {
		window.setTimeout(scrollToTop, 1*1000);
	};

	var scrollToTop = function() {
		
		var root = jQuery('body'); 
		var scrollTop = root.scrollTop();
		
		var time = getScrollToTopTimeForHeight(scrollTop);
		
		jQuery('html, body').stop().animate({
			scrollTop: 0 
		}, time, 'linear', registerScrollToBottom);
	};

	registerScrollToBottom();
};