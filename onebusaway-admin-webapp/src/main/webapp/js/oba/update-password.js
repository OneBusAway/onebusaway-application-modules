/*
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
$("#messageSuccess, #messageFailure").hide();
$("#updatePassword").submit(function(evt) {
	evt.preventDefault();
	var csrfParameter = $("meta[name='_csrf_parameter']").attr("content");
	var csrfHeader = $("meta[name='_csrf_header']").attr("content");
	var csrfToken = $("meta[name='_csrf']").attr("content");
	var data = {
		"newPassword": $("#newPassword").val(),
		"confirmPassword": $("#confirmPassword").val()
	}
	data[csrfParameter] = csrfToken;

	$.post("update-password!updatePassword.action", data, function(resp) {
		$("#messageSuccess, #messageFailure").hide();
		if (resp == "success")
			$("#messageSuccess").show()
		else
			$("#messageFailure").show()
	})
});