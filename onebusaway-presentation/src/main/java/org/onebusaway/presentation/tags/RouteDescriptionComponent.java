package org.onebusaway.presentation.tags;

import java.io.IOException;
import java.io.Writer;

import org.apache.struts2.components.ContextBean;
import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.transit_data.model.RouteBean;

import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

public class RouteDescriptionComponent extends ContextBean {

  private static final Logger LOG = LoggerFactory.getLogger(RouteDescriptionComponent.class);

  private String _value;

  public RouteDescriptionComponent(ValueStack stack) {
    super(stack);
  }

  public void setValue(String value) {
    _value = value;
  }

  @Override
  public boolean usesBody() {
    return false;
  }

  @Override
  public boolean end(Writer writer, String body) {

    if (_value == null)
      _value = "top";

    Object obj = findValue(_value);

    if (obj instanceof RouteBean) {
      RouteBean route = (RouteBean) obj;
      String value = RoutePresenter.getDescriptionForRoute(route);
      try {
        if( value != null)
          writer.write(value);
      } catch (IOException e) {
        LOG.error("Could not write out Text tag", e);
      }
    }

    return super.end(writer, "");
  }
}
