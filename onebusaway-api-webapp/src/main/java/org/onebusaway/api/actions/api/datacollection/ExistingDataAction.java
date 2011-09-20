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
package org.onebusaway.api.actions.api.datacollection;

import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.services.DataCollectionService;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExistingDataAction extends ApiActionSupport {
  
  private static final int V1 = 1;

  @Autowired
  private DataCollectionService _data;
  
  public ExistingDataAction() {
    super(V1);
  }

  private static final long serialVersionUID = 1L;
  
  public DefaultHttpHeaders index() {
    List<String> values = new ArrayList<String>();
    File dataDirectory = _data.getDataDirectory();
    File[] files = dataDirectory.listFiles();
    if( files != null) {
      for( File file : files)
        values.add(file.getName());
    }
    return setOkResponse(values);
  }

}
