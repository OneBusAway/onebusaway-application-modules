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

import java.io.File;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.admin.bundle.model.BundleMetadata;
import org.onebusaway.admin.model.BundleRequestResponse;
import org.onebusaway.admin.service.bundle.impl.BundleBuildingUtil;
import org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MetadataTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(MetadataTask.class);
  @Autowired
  private MultiCSVLogger logger;
  @Autowired
  private BundleRequestResponse requestResponse;
  
  private ObjectMapper mapper = new ObjectMapper();
  
  public void setLogger(MultiCSVLogger logger) {
    this.logger = logger;
  }
  
  public void setBundleRequestResponse(BundleRequestResponse requestResponse) {
    this.requestResponse = requestResponse;
  }
  
  @Override
  public void run() {
	BundleBuildingUtil util = new BundleBuildingUtil();
    BundleMetadata data = new BundleMetadata(); 
    try {
      String outputDirectory = requestResponse.getResponse().getBundleDataDirectory();
      String sourceDirectory = requestResponse.getResponse().getBundleOutputDirectory();
      String rootDirectory = requestResponse.getResponse().getBundleRootDirectory();
      data.setId(generateId());
      requestResponse.getResponse().setBundleId(data.getId());
      data.setName(requestResponse.getRequest().getBundleName());
      data.setServiceDateFrom(requestResponse.getRequest().getBundleStartDate().toDate());
      data.setServiceDateTo(requestResponse.getRequest().getBundleEndDate().toDate());

      data.setOutputFiles(util.getBundleFilesWithSumsForDirectory(new File(outputDirectory), new File(outputDirectory), new File(rootDirectory)));
      data.setSourceData(util.getSourceFilesWithSumsForDirectory(new File(sourceDirectory), new File(sourceDirectory), new File(rootDirectory)));
      data.setChangeLogUri(util.getUri(new File(rootDirectory), "change_log.csv"));
      data.setStatisticsUri(util.getUri(new File(rootDirectory), "gtfs_stats.csv"));
      data.setValidationUri(util.getUri(new File(rootDirectory), "gtfs_validation_post.csv"));
      logger.changelog("generated metadata for bundle " + data.getName());
    
      String outputFile = outputDirectory + File.separator + "metadata.json";
      mapper.writeValue(new File(outputFile), data);
      outputFile = sourceDirectory + File.separator + "metadata.json";
      mapper.writeValue(new File(outputFile), data);
    } catch (Exception e) {
      _log.error("json serialization failed:", e);
    }
  }

  private String generateId() {
    /*
    * this is not guaranteed to be unique but is good enough for this
    * occasional usage.  Purists can follow Publisher.java's pattern
    * in queue-subscriber.
    */ 
    return UUID.randomUUID().toString();
  }
}
