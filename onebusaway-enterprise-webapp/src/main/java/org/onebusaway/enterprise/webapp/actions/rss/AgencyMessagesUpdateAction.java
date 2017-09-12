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
package org.onebusaway.enterprise.webapp.actions.rss;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.onebusaway.enterprise.webapp.actions.status.model.StatusGroup;
import org.onebusaway.enterprise.webapp.actions.status.model.StatusItem;
import org.onebusaway.enterprise.webapp.actions.status.service.StatusProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;

@Results({@Result(type = "rss", params={"feedName", "feed", "feedType", "rss_2.0"})})
public class AgencyMessagesUpdateAction extends OneBusAwayEnterpriseActionSupport {
	  private static Logger _log = LoggerFactory.getLogger(AgencyMessagesUpdateAction.class);
	  private static final long serialVersionUID = 1L;

	  private SyndFeed _feed;

	  @Autowired
	  private StatusProvider _statusProvider;

	  public SyndFeed getFeed() {
	    return _feed;
	  }

	  @Override
	  public String execute() {
	    _feed = new SyndFeedImpl();
	    StringBuilder title = new StringBuilder();
	    title.append("OneBusAway General Notices");

	    HttpServletRequest request = ServletActionContext.getRequest();

	    StringBuilder b = new StringBuilder();
	    b.append("http://");
	    b.append(request.getServerName());
	    if (request.getServerPort() != 80)
	      b.append(":").append(request.getServerPort());
	    if (request.getContextPath() != null)
	      b.append(request.getContextPath());
	    String baseUrl = b.toString();

	    _feed.setTitle(title.toString());
	    _feed.setLink(baseUrl);
	    _feed.setDescription("General Notices");

	    List<SyndEntry> entries = new ArrayList<SyndEntry>();

	    // Add Agency Messages
	    SyndEntry agencyMsgEntry = new SyndEntryImpl();
	    StatusGroup agencyMsgGroup = _statusProvider.getAgencyMetadataStatus();
	    if (agencyMsgGroup.getItems().size() == 0) {
	      agencyMsgEntry = new SyndEntryImpl();
	      agencyMsgEntry.setTitle("No General Notices");
	      entries.add(agencyMsgEntry);
	    } else {
	      for (StatusItem agencyMsgItem : agencyMsgGroup.getItems()) {
	        agencyMsgEntry = new SyndEntryImpl();
	        agencyMsgEntry.setTitle(agencyMsgItem.getTitle());
	        entries.add(agencyMsgEntry);
	      }
	    }

	    _feed.setEntries(entries);
	    return SUCCESS;
	  }
}
