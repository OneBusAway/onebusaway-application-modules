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
package org.onebusaway.webapp.gwt.common.widgets;

import org.onebusaway.webapp.gwt.common.control.Place;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;

public class PlacePresenter {
  
  public DivPanel getPlaceAsPanel(Place place) {
    return getPlaceAsPanel(place,null);
  }
  
  public DivPanel getPlaceAsPanel(Place place, ClickHandler handler) {

    DivPanel panel = new DivPanel();

    DivPanel pName = new DivPanel();
    pName.addStyleName("Place-Name");

    if (handler == null) {
      pName.add(new SpanWidget(place.getName()));
    } else {
      Anchor anchor = new Anchor(place.getName());
      anchor.addClickHandler(handler);
      pName.add(anchor);
    }

    panel.add(pName);

    for (String description : place.getDescription()) {
      DivPanel pDescription = new DivPanel();
      pDescription.addStyleName("Place-Description");

      if (handler == null) {
        pDescription.add(new SpanWidget(description));
      } else {
        Anchor descriptionAnchor = new Anchor(description);
        descriptionAnchor.addClickHandler(handler);
        pDescription.add(descriptionAnchor);
      }
      panel.add(pDescription);
    }

    return panel;
  }
}
