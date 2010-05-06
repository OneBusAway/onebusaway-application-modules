package org.onebusaway.webapp.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ContextBeanTag;

import com.opensymphony.xwork2.util.ValueStack;

public class RouteDescriptionTag extends ContextBeanTag {

  private static final long serialVersionUID = 1L;

  protected String _value;

  public Component getBean(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
    return new RouteDescriptionComponent(stack);
  }

  public void setValue(String value) {
    _value = value;
  }

  protected void populateParams() {
    super.populateParams();

    RouteDescriptionComponent tag = (RouteDescriptionComponent) getComponent();
    tag.setValue(_value);
  }
}
