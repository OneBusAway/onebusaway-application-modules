/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.bundle.task;

import org.apache.commons.io.IOUtils;
import org.onebusaway.admin.model.BundleRequestResponse;
import org.onebusaway.utility.IOLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;


public class StopConsolidationFileTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(StopConsolidationFileTask.class);

  private BundleRequestResponse _requestResponse;

  private String _stopConsolidationUrl;

  @Autowired
  public void setBundleRequestResponse(BundleRequestResponse requestResponse) {
    this._requestResponse = requestResponse;
  }
  
  public void setStopConsolidationUrl(String stopConsolidationUrl) {
    _stopConsolidationUrl = stopConsolidationUrl;
  }

  @Override
  public void run() {
    if (_stopConsolidationUrl == null)
      return;
    try {
      String outputDirectory = _requestResponse.getResponse().getBundleDataDirectory();
      InputStream input = IOLibrary.getPathAsInputStream(_stopConsolidationUrl);
      File outputFile = new File(outputDirectory + "/StopConsolidation.txt");
      OutputStream output = new FileOutputStream(outputFile);
      IOUtils.copy(input, output);
    } catch(IOException e) {
      _log.error("error" + e);
    }
  }
}
