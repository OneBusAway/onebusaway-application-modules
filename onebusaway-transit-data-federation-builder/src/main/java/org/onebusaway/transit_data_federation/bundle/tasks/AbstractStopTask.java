/**
 * Copyright (C) 2012 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.utility.IOLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractStopTask {

  protected static Logger _log = LoggerFactory.getLogger(StopVerificationTask.class);
  @Autowired
  protected MultiCSVLogger _logger;
  protected GtfsDao _dao;
  private String path = null;

  public AbstractStopTask() {
    super();
  }

  @Autowired
  public void setGtfsDao(GtfsDao dao) {
    _dao = dao;
  }

  public void setLogger(MultiCSVLogger logger) {
    this._logger = logger;
  }

  public void setPath(String path) {
    this.path = path;
  }

  protected abstract void insertHeader();
  
  protected abstract void verifyStops(String rootId, List<String> consolidatedStops);
  
  public void run() {
    if (path == null || path.length() == 0) {
      _log.info("path not configured, exiting");
      return;
    }
    
    insertHeader();
    _log.info("running verification task from path=" + path);
    try {
      InputStream in = IOLibrary.getPathAsInputStream(path);
    
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String line = null;
    
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.length() == 0 || line.startsWith("#")
            || line.startsWith("{{{") || line.startsWith("}}}"))
          continue;
        String[] tokens = line.split("\\s+");
        if (tokens.length < 1)
          continue;
        String rootStop = tokens[0];
        List<String> consolidatedStops = new ArrayList<String>(Arrays.asList(tokens));
        consolidatedStops.remove(0); // remove the root stop
        
        verifyStops(rootStop, consolidatedStops);
      }
    } catch (Exception e) {
      _log.error("run failed", e);
    }
    _log.info("exiting verification task");
  
  }

}
