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
package org.onebusaway.enterprise.webapp.actions.where;

import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

@ParentPackage("onebusaway-webapp-default")
@InterceptorRefs( {@InterceptorRef("onebusaway-webapp-stack")})
@Results( {
    @Result(type = "chain", name = "query-default-search-location", location = "query-default-search-location"),
    @Result(type = "chain", name = "set-default-search-location", location = "set-default-search-location"),
    @Result(type = "chain", name = "stops", location = "stops"),
    @Result(type = "chain", name = "routes", location = "routes")})
public abstract class AbstractWhereAction extends AbstractAction {

  private static final long serialVersionUID = 1L;
}
