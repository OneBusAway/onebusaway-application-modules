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
package org.onebusaway.presentation.impl;

import org.onebusaway.presentation.services.LocationNameSplitStrategy;
import org.onebusaway.presentation.services.SelectionNameTypes;
import org.onebusaway.transit_data.model.NameBean;

import java.util.ArrayList;
import java.util.List;

public class DefaultLocationNameSplitStrategyImpl implements
    LocationNameSplitStrategy {

  public List<NameBean> splitLocationNameIntoParts(String name) {

    if (name.contains("P&R"))
      name = name.replaceAll("P&R", "ParkAndRide");

    String[] tokens = name.split("\\s+&\\s+");
    List<NameBean> names = new ArrayList<NameBean>();

    if (tokens.length == 2) {
      names.add(new NameBean(SelectionNameTypes.MAIN_STREET, tokens[0]));
      names.add(new NameBean(SelectionNameTypes.CROSS_STREET, tokens[1]));

    } else {
      names.add(new NameBean(SelectionNameTypes.STOP_DESCRIPTION, name));
    }

    return names;
  }
}
