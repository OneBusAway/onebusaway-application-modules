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
package org.onebusaway.users.impl;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet {@link Filter} that pushes a {@link RequestAndResponseContext}
 * context object into thread local storage so that the
 * {@link HttpServletRequest} and {@link HttpServletResponse} can be accessed by
 * code running within the context of the request.
 * 
 * @author bdferris
 * 
 */
public class RequestAndResponseContextFilter implements Filter {

  public void init(FilterConfig filterConfig) {

  }

  public void doFilter(ServletRequest req, ServletResponse res,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    RequestAndResponseContext.setContext(new RequestAndResponseContext(request,
        response));

    try {
      chain.doFilter(request, response);
    } finally {
      RequestAndResponseContext.resetContext();
    }
  }

  public void destroy() {

  }

}
