var oba_where_sign_stop = function() {
	
	var heightToScrollTime = function(height) {
		return height / 200 * 1000;
	};
	
	var registerScrollToBottom = function() {
		
		var body = jQuery('body');
		var scroll = body.height() - jQuery(window).height();
		var time = 29 * 1000 - heightToScrollTime(scroll);
		
		window.setTimeout(scrollToBottom, 20*1000);
	};
	
	var scrollToBottom = function() {
		
		var body = jQuery('body');
		var scrollTop = body.scrollTop();
		
		var top = body.height() - jQuery(window).height();
		var time = heightToScrollTime(top - scrollTop);
		
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
		
		var time = heightToScrollTime(scrollTop);
		
		jQuery('html, body').stop().animate({
			scrollTop: 0 
		}, time, 'linear', registerScrollToBottom);
	};
	
	registerScrollToBottom();
};