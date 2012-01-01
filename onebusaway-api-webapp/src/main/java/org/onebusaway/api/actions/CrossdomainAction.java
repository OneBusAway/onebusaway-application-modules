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
package org.onebusaway.api.actions;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.api.actions.api.ApiKeyAuthorization;

@ApiKeyAuthorization(enabled = false)
@Results({
    @Result(name = "success", location = "crossdomain.xml", type = "xml"),
    @Result(name = "success-xml", location = "crossdomain.xml", type = "xml")})
public class CrossdomainAction extends OneBusAwayApiActionSupport {

  private static final long serialVersionUID = 1L;

  public String index() {
    return SUCCESS;
  }
}
