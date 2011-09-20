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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UploadDataAction extends ApiActionSupport {

  private static final int V1 = 1;
  
  public UploadDataAction() {
    super(V1);
  }

  private static final long serialVersionUID = 1L;

  @Autowired
  private DataCollectionService _data;

  private String _id;

  private File _file;

  public void setId(String id) {
    _id = id;
  }

  public void setUpload(File file) {
    _file = file;
  }

  public DefaultHttpHeaders update() throws IOException {

    File dataDirectory = _data.getDataDirectory();
    File target = new File(dataDirectory, _id);

    InputStream from = new ByteArrayInputStream(new byte[0]);
    if( _file.exists() )
      from = new FileInputStream(_file);
    OutputStream to = new FileOutputStream(target);

    byte[] buffer = new byte[4096];
    int bytesRead;

    while ((bytesRead = from.read(buffer)) != -1)
      to.write(buffer, 0, bytesRead); // write

    return setOkResponse(null);
  }
}
