/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.bundle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeTripLibrary;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class RealtimeSourceServiceImpl {

  private static Logger _log = LoggerFactory.getLogger(RealtimeSourceServiceImpl.class);
  
  @Autowired
  private ListableBeanFactory beanFactory;

  public String[] getSourceNames() {
    return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, GtfsRealtimeSource.class);
  }
  
  public List<GtfsRealtimeSource> getSources() {
    ArrayList<GtfsRealtimeSource> sources = new ArrayList<GtfsRealtimeSource>();
    for (String sourceName : getSourceNames()) {
      sources.add((GtfsRealtimeSource) beanFactory.getBean(sourceName));
    }
    _log.debug("returning " + sources.size() + " sources");
    return sources;
  }

  public boolean getPlaybackEnabled() {
    return SystemTime.getAdjustment() != 0;
  }

  public boolean getSystemTimeAdjustmentEnabled() { return SystemTime.isEnabled(); }

  public String getCurrentTime() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(SystemTime.currentTimeMillis()));
  }

}
