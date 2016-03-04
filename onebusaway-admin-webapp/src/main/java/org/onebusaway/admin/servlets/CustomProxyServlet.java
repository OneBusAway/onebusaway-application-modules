/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.servlets;

import javax.servlet.ServletException;

import org.mitre.dsmiley.httpproxy.URITemplateProxyServlet;

public class CustomProxyServlet extends URITemplateProxyServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void initTarget() throws ServletException {
		String key = CustomProxyServlet.class.getSimpleName() + ".targetUri";
		targetUriTemplate = getServletContext().getInitParameter(key);
		if (targetUriTemplate == null)
			throw new ServletException(P_TARGET_URI + " is required.");
	}

}
