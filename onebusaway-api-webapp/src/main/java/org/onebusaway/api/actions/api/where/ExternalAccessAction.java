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
package org.onebusaway.api.actions.api.where;

import java.io.IOException;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.exceptions.ServiceException;

public class ExternalAccessAction extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V1 = 1;

  private static final int V2 = 2;

  public ExternalAccessAction() {
    super(LegacyV1ApiSupport.isDefaultToV1() ? V1 : V2);
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {
      return setOkResponse(null);
  }
}
