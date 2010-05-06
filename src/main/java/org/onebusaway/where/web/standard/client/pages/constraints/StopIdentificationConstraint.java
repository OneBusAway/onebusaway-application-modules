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
/**
 * 
 */
package org.onebusaway.where.web.standard.client.pages.constraints;

import com.google.gwt.libideas.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

import org.onebusaway.where.web.common.client.Context;
import org.onebusaway.where.web.common.client.widgets.DivWidget;
import org.onebusaway.where.web.standard.client.pages.ESearchType;
import org.onebusaway.where.web.standard.client.resources.OneBusAwayStandardResources;

public class StopIdentificationConstraint extends AbstractConstraint {

  public void update(Context context) {

    FlowPanel panel = new FlowPanel();
    panel.addStyleName("stopIdentificationPanel");
    _resultsPanel.add(panel);

    DivWidget welcome = new DivWidget(
        _msgs.standardIndexPageStopIdentification());
    panel.add(welcome);

    Image img = new Image();
    ImageResource src = OneBusAwayStandardResources.INSTANCE.getImageStopIdentification();
    img.setUrlAndVisibleRect(src.getURL(), src.getLeft(), src.getTop(),
        src.getWidth(), src.getHeight());
    panel.add(img);

    _wrapper.setSearchText(ESearchType.NUMBER, "");
  }
}