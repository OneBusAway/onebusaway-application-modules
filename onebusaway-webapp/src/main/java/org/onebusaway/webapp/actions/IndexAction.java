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
package org.onebusaway.webapp.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionSupport;

@Results({@Result(name = "404", type = "httpheader", params = {
    "error", "404", "errorMessage", "resource not found"})})
public class IndexAction extends ActionSupport {

  private static final long serialVersionUID = 1L;
  
  private Properties _gitProperties;

  public Properties getGitProperties(){
	return _gitProperties;
  }
  
  @Override
  public String execute() throws Exception {

    ActionContext context = ActionContext.getContext();
    ActionInvocation invocation = context.getActionInvocation();
    ActionProxy proxy = invocation.getProxy();

    String namespace = proxy.getNamespace();
    String name = proxy.getActionName();

    if (namespace.equals("/") && (name.equals("index") || name.equals(""))) {
      return super.execute();
    }

	_gitProperties = new Properties();
	try {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("git.properties");
		if (inputStream != null) {
			_gitProperties.load(inputStream);
		}
	} catch (IOException ioe) {}
	  
    return "404";
  }
}
