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

  /**
   * TODO(bdferris): This method of determining the context path is pretty
   * hacky. See if we can refactor existing users of this method to do something
   * different.
   * 
   * @param context
   * @return
   * @deprecated avoid using this if you can help it.
   */
  @Deprecated
  public static String getContextPath(ServletContext context) {

    // Get the context path without the request.
	String contextPath = context.getContextPath();
    try {     
      String path = context.getResource("/").getPath();
      if (path.contains("jetty")
          && path.contains("onebusaway-quickstart-assembly")) {
        return "";
      }
      String localPath = path.substring(0, path.lastIndexOf("/"));
      if (contextPath == null || contextPath.indexOf("/") == -1)
        return "";
      localPath = contextPath.substring(contextPath.lastIndexOf("/"));
      if (localPath.equals("/localhost"))
        return "";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return contextPath;
  }
}
