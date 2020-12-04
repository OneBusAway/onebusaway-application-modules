/**
 * Copyright (C) 2020 Cambridge Systematics, Inc.
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
package org.onebusaway.container;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A place to add security filters as requested by client security
 * policies.  Inspired by org.apache.catalina.filters.HttpHeaderSecurityFilter
 * which sadly doesn't do enough.
 */
public class ObaSecurityHeaderFilter implements Filter {

  // Content-Security-Policy: frame-ancestors 'self' -- DEFAULT if enabled
  public static final String CONTENT_SECURITY_POLICY_OPTION = "contentSecurityPolicyOption";
  public static final String CSP_FRAME_ANCESTORS_HOST_SUB_OPTION = "cspFrameHostSubOption";
  public static final String CSP_NONE_SUB_OPTION = "cspNoneSubOption";
  public static final String CSP_SELF_SUB_OPTION = "cspSelfSubOption";

  // Content-Security-Policy strings
  private static final String CSPS_HEADER = "Content-Security-Policy";
  private static final String CSPS_PRE_AMBLE = "frame-ancestors ";
  private static final String CSPS_SELF = "'self'";
  private static final String CSPS_NONE = "'none'";


  private Map<String, String> headerMap = new HashMap<>();

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    if (filterConfig.getInitParameter(CONTENT_SECURITY_POLICY_OPTION) != null) {
      if (filterConfig.getInitParameter(CSP_FRAME_ANCESTORS_HOST_SUB_OPTION) != null) {
        headerMap.put(CSPS_HEADER,
                CSPS_PRE_AMBLE + "'" + filterConfig.getInitParameter(CSP_FRAME_ANCESTORS_HOST_SUB_OPTION) + "'");
      } else if (filterConfig.getInitParameter(CSPS_NONE) != null) {
        headerMap.put(CSPS_HEADER,
                CSPS_PRE_AMBLE + CSPS_NONE);
      } else {
        // the default
        headerMap.put(CSPS_HEADER,
                CSPS_PRE_AMBLE + CSPS_SELF);
      }
    }
  }

  @Override
  public void doFilter(ServletRequest request,
                       ServletResponse response,
                       FilterChain chain)
                        throws IOException,
                        ServletException {
    if (response.isCommitted() && !headerMap.isEmpty()) {
      throw new ServletException("Response is committed when adding headers " + headerMap.toString());
    }

    if (!headerMap.isEmpty() && response instanceof HttpServletResponse) {
      for (Map.Entry<String, String> entry : headerMap.entrySet()) {
        ((HttpServletResponse) response).setHeader(entry.getKey(), entry.getValue());
      }
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    // NOOP
  }
}
