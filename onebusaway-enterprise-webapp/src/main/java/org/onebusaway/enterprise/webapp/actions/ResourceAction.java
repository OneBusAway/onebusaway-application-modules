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
package org.onebusaway.enterprise.webapp.actions;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.onebusaway.presentation.impl.users.SetupAction;
import org.onebusaway.presentation.services.cachecontrol.CacheControl;

@ParentPackage("onebusaway-webapp-default")
@SetupAction
@CacheControl(maxAge = 365 * 24 * 60 * 60, lastModifiedMethod = "getLastModified")
public class ResourceAction extends
        org.onebusaway.presentation.impl.resources.ResourceAction {

    private static final long serialVersionUID = 1L;

}
