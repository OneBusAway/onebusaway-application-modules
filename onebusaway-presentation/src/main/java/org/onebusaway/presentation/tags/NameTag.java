package org.onebusaway.presentation.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ContextBeanTag;

import com.opensymphony.xwork2.util.ValueStack;

public class NameTag extends ContextBeanTag {

  private static final long serialVersionUID = 1L;

  protected String _value;

  public Component getBean(ValueStack stack, HttpServletRequest req,
      HttpServletResponse res) {
    return new NameComponent(stack);
  }

  public void setValue(String value) {
    _value = value;
  }

  protected void populateParams() {
    super.populateParams();
    NameComponent tag = (NameComponent) getComponent();
    tag.setValue(_value);
  }
}
