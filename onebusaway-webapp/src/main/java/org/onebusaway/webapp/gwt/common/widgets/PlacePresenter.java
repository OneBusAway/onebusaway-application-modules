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
