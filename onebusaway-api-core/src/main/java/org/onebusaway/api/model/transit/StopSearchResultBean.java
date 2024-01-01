/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.api.model.transit;

import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.StopBean;

import java.io.Serializable;

/**
 * results of searching for a stop.
 */
public class StopSearchResultBean implements Serializable {

  private static final long serialVersionUID = 1L;
  ListBean<StopBean> stopSuggestions;
  public void setStopSuggestions(ListBean<StopBean> stopSuggestions) {
    this.stopSuggestions = stopSuggestions;
  }

  public ListBean<StopBean> getStopSuggestions() {
    return stopSuggestions;
  }
}
