/**
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
package org.onebusaway.enterprise.webapp.actions.m.wiki;

import java.text.DateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.onebusaway.enterprise.webapp.actions.wiki.model.NycWikiPageWrapper;
import org.onebusaway.wiki.api.WikiDocumentService;
import org.onebusaway.wiki.api.WikiRenderingService;
import org.springframework.beans.factory.annotation.Autowired;

public class IndexAction extends OneBusAwayEnterpriseActionSupport implements
		ServletRequestAware {

	private static final long serialVersionUID = 1L;

	private HttpServletRequest httpServletRequest;

	@Autowired
	private WikiDocumentService _wikiDocumentService;

	@Autowired
	private WikiRenderingService _wikiRenderingService;

	protected String namespace;
	protected String name;
	private String title;
	private String content;
	private String editLink;
	private Date lastModifiedTimestamp;

	@Override
	public String execute() throws Exception {
		if (namespace == null || namespace.isEmpty()) {
			namespace = "MainMobile";
		}

		if (name == null || name.isEmpty()) {
			name = "Index";
		}

		if (namespace != null && name != null) {

			// content for page
			try {
				NycWikiPageWrapper page = new NycWikiPageWrapper(
						_wikiDocumentService
								.getWikiPage(namespace, name, getLocale(),false));

				if (page.pageExists()) {
					content = _wikiRenderingService.renderPage(page);
					String contextPath = this.httpServletRequest
							.getContextPath();
					content = content.replaceAll(
							"href=\"/wiki/(.*?)Mobile/(.*?)", "href=\""
									+ contextPath + "/m/wiki/$1/$2");
					editLink = _wikiRenderingService.getEditLink(page);
					title = page.getTitle();
					lastModifiedTimestamp = page.getLastModified();
				} else {
					content = null;
					editLink = null;
					title = null;
					lastModifiedTimestamp = null;

					return "NotFound";
				}
			} catch (Exception ex) {
				throw new JspException(ex);
			}
		}

		return SUCCESS;
	}

	@Override
	public void setServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		if (title == null || title.isEmpty()) {
			return "";
		}
		return ": " + title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	// FIXME: should replace namespace at the service level?
	public String getContent() {
		return content.replace("%{namespace}", namespace);
	}

	// FIXME: should replace namespace at the service level?
	public String getEditLink() {
		return editLink.replace("%{namespace}", namespace);
	}

	public String getLastModifiedTimestamp() {
		if (lastModifiedTimestamp == null)
			return "Unknown";

		return DateFormat.getDateInstance().format(lastModifiedTimestamp)
				+ " at "
				+ DateFormat.getTimeInstance().format(lastModifiedTimestamp);
	}
}
