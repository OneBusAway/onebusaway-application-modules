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
package org.onebusaway.gtfs_realtime.library;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs_realtime.interfaces.FeedEntityModel;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

public abstract class FeedEntityConvertor<T extends FeedEntityModel> {

  public abstract T readFeedEntity(FeedEntity entity, long timestamp);
  
  public List<T> readAll(FeedMessage feedMessage) {
    List<T> updates = new ArrayList<T>();
    if (feedMessage == null) {
      return updates;
    }
    List<FeedEntity> entityList = feedMessage.getEntityList();
    long timestamp = feedMessage.getHeader().getTimestamp() * 1000;
    for (FeedEntity entity : entityList) {
      T model = readFeedEntity(entity, timestamp);
      if (model != null) {
        updates.add(model);
      }
    }
    return updates;
  }
  
}
