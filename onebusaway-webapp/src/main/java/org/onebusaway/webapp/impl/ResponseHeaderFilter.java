/**
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
package org.onebusaway.webapp.impl;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResponseHeaderFilter implements Filter {

    private FilterConfig _fc;

    private Map<String, String> _headers = new HashMap<String, String>();

    private Set<String> _matches = new HashSet<String>();

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        boolean isMatch = false;

        if (_matches.isEmpty()) {
            isMatch = true;
        } else {
            String uri = request.getRequestURI();
            for (String match : _matches) {
                if (uri.matches(match)) {
                    isMatch = true;
                    break;
                }
            }
        }

        if (isMatch) {

            // set the provided HTTP response parameters
            for (Map.Entry<String, String> entry : _headers.entrySet()) {
                String headerName = entry.getKey();
                String headerValue = entry.getValue();
                response.addHeader(headerName, headerValue);
            }
        }

        // pass the request/response on
        chain.doFilter(req, response);
    }

    public void init(FilterConfig filterConfig) {
        _fc = filterConfig;
        for (Enumeration<?> e = _fc.getInitParameterNames(); e.hasMoreElements();) {
            String headerName = (String) e.nextElement();
            String headerValue = _fc.getInitParameter(headerName);
            if (headerName.startsWith(":")) {
                headerName = headerName.substring(1);
                if (headerName.equals("match")) {
                    _matches.add(headerValue);
                }
            } else {
                _headers.put(headerName, headerValue);
            }
        }
    }

    public void destroy() {
        _fc = null;
    }

}
