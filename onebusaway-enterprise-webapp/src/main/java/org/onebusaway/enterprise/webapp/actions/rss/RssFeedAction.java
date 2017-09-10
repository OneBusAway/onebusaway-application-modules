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

import com.rometools.rome.feed.synd.SyndFeed;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.onebusaway.enterprise.webapp.actions.status.service.StatusProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * Base class for Actions that do RSS work.
 */
public class RssFeedAction extends OneBusAwayEnterpriseActionSupport {

    private static final long serialVersionUID = 1L;

    protected SyndFeed _feed;

    @Autowired
    protected StatusProvider _statusProvider;

    public SyndFeed getFeed() {
        return _feed;
    }

    protected String createBaseUrl(HttpServletRequest request) {

        StringBuilder b = new StringBuilder();
        b.append(request.getProtocol());
        b.append(request.getServerName());
        if (request.getServerPort() != 80)
            b.append(":").append(request.getServerPort());
        if (request.getContextPath() != null)
            b.append(request.getContextPath());
        return b.toString();
    }

}
