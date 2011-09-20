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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestAndResponseContext {

  private static final ThreadLocal<RequestAndResponseContext> _context = new ThreadLocal<RequestAndResponseContext>();

  public static RequestAndResponseContext getContext() {
    return _context.get();
  }

  public static void setContext(RequestAndResponseContext context) {
    _context.set(context);
  }

  public static void resetContext() {
    _context.remove();
  }

  private final HttpServletRequest _request;

  private final HttpServletResponse _response;

  public RequestAndResponseContext(HttpServletRequest request,
      HttpServletResponse response) {
    _request = request;
    _response = response;
  }

  public HttpServletRequest getRequest() {
    return _request;
  }

  public HttpServletResponse getResponse() {
    return _response;
  }
}
