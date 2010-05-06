package org.onebusaway.webapp.gwt.oba_application.view;

import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class PlaceCollection extends FlowPanel {

  private boolean _expanded = false;

  private FlowPanel _elements = new FlowPanel();

  private Label _label;

  public PlaceCollection(String name, Image image) {
    FlowPanel header = new FlowPanel();
    add(header);
    header.add(image);
    SpanWidget label = new SpanWidget(name);
    header.add(label);

    add(_elements);
    _elements.setVisible(_expanded);

    _label = new Label("0 elements");
    _label.setVisible(!_expanded);
    add(_label);

    ClickHandlerImpl handler = new ClickHandlerImpl();
    image.addClickHandler(handler);
    label.addClickHandler(handler);
  }

  public void addEntry(LocalSearchResult result, TimedPlaceBean bean) {
    DivWidget widget = new DivWidget(result.getName() + " "
        + (bean.getTime() / 60) + " mins");
    _elements.add(widget);
    int count = _elements.getWidgetCount();
    String label = count == 1 ? "1 element" : (count + " elements");
    _label.setText(label);
  }

  private void toggle() {
    _expanded = !_expanded;
    _elements.setVisible(_expanded);
    _label.setVisible(!_expanded);
  }

  private class ClickHandlerImpl implements ClickHandler {
    public void onClick(ClickEvent arg0) {
      toggle();
    }
  }

}
