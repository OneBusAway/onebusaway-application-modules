/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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

import org.onebusaway.admin.model.BundleRequestResponse;
import org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ChangeLogTask implements Runnable {

  private Logger _log = LoggerFactory.getLogger(ChangeLogTask.class);
  protected MultiCSVLogger logger;
  protected BundleRequestResponse requestResponse;
  
  @Autowired
  public void setBundleRequestResponse(BundleRequestResponse requestResponse) {
    this.requestResponse = requestResponse;
  }
  
  @Autowired
  public void setLogger(MultiCSVLogger logger) {
    this.logger = logger;
  }
  
  @Override
  public void run() {
    _log.info("creating change log and comment");
    logger.changelogHeader(requestResponse.getRequest().getBundleComment());
    _log.info("done");
  }
}
