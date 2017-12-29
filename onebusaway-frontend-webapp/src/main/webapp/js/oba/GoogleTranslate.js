function googleTranslateElementInit() {

	var translate_element_id = jQuery("#google_translate_element_bottom").is(
			":visible") ? "google_translate_element_bottom"
			: "google_translate_element_side";
	var translate_element = jQuery("#" + translate_element_id);
	translate_element
			.click(function(e) {
				e.preventDefault();
				translate_element
						.html(' ')
						.attr('src',
								'//translate.google.com/translate_a/element.js?cb=googleTranslateElementInit');
				new google.translate.TranslateElement(
						{
							pageLanguage : 'en',
							layout : google.translate.TranslateElement.InlineLayout.SIMPLE
						}, translate_element_id);
				translate_element.unbind('click');
			});
}