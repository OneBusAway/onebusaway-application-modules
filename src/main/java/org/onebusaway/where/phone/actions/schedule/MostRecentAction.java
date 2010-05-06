/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.where.phone.actions.schedule;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.where.web.common.client.model.StopWithArrivalsBean;
import org.springframework.stereotype.Component;

@Component
public class MostRecentAction extends AbstractPredictedScheduleAction {

  private static final long serialVersionUID = 1L;

  @Override
  public String execute() throws Exception {

    Stop lastSelection = _bookmarkService.getLastSelection(_userId);

    if (lastSelection == null)
      return INPUT;

    StopWithArrivalsBean bean = _obaService.getArrivalsByStopId(lastSelection.getId());
    _predictions = bean.getPredictedArrivals();
    return SUCCESS;
  }
}
