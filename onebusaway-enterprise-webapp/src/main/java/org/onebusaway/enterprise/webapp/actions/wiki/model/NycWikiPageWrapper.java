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
package org.onebusaway.enterprise.webapp.actions.wiki.model;

import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opensymphony.xwork2.ActionContext;
import org.onebusaway.wiki.api.WikiPage;

/*
 * This class wraps an WikiPageImpl and fixes up the lack of page namespace handling in the 
 * existing WikiPageImpl class. 
 */
public class NycWikiPageWrapper implements WikiPage {

	private static final Pattern externalLinkPattern = 
			Pattern.compile("\\b(https?|ftp|file)://[-A-Z0-9+&@#/%?=~_|!:,.;]*[-A-Z0-9+&@#/%=~_|]");

	private static final Pattern linkPattern = Pattern.compile("([^\\[|\\>]*)]]");
	
	private WikiPage page;
	
	public NycWikiPageWrapper(WikiPage realPage) {
		this.page = realPage;
	}

	public boolean pageExists() {
		return (this.page != null);
	}
	
	public String getNamespace() {
		return this.page.getNamespace();
	}

	public String getName() {
		return this.page.getName();
	}

	@Override
	public Locale getLocale() {
		ActionContext context = ActionContext.getContext();
		Locale locale = context.getLocale();
		return locale;
	}

	public String getTitle() {
		return this.page.getTitle();
	}
	
	public Date getLastModified() {
		return this.page.getLastModified();
	}

	public String getContent() {
		String content = this.page.getContent();

		if(content == null) 
			return content;
		
		// replace a "." with a "/" in a Wiki markup link
		Matcher m = linkPattern.matcher(content);
		while (m.find()) {
			String match = m.group();

			Matcher e = externalLinkPattern.matcher(match);

			if(e.find()) 
				continue;
			
			content = content.replace(match, match.replace(".", "/"));
		}

		return content;
	}
}