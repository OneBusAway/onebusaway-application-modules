function handleGenerateApiKeyClick() {
	jQuery.ajax({
		url: "../api/config/generate-api-key",
		type: "GET",
		success: function(response) {
			jQuery("#key").val(response);
		},
		error: function(request) {
			alert("There was an error processing your request. Please try again.");
		}
	});
}