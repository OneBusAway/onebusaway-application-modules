package org.onebusaway.oba.web.standard.client.view;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.onebusaway.common.web.common.client.widgets.DivWidget;
import org.onebusaway.common.web.common.client.widgets.SpanWidget;
import org.onebusaway.oba.web.common.client.model.LocalSearchResult;
import org.onebusaway.oba.web.common.client.model.TimedPlaceBean;

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
    
    ClickHandler handler = new ClickHandler();
    image.addClickListener(handler);
    label.addClickListener(handler);
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

  private class ClickHandler implements ClickListener {
    public void onClick(Widget arg0) {
      toggle();
    }
  }

}
