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
package org.onebusaway.presentation.impl;

import javax.servlet.ServletContext;

public class ServletLibrary {

  public static String getContextPath(ServletContext context) {

    // Get the context path without the request.
    String contextPath = "";
    try {
      String path = context.getResource("/").getPath();
      contextPath = path.substring(0, path.lastIndexOf("/"));
      contextPath = contextPath.substring(contextPath.lastIndexOf("/"));
      if (contextPath.equals("/localhost"))
        contextPath = "";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return contextPath;
  }
}
