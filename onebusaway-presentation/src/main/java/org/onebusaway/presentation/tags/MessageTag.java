package org.onebusaway.presentation.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ContextBeanTag;

import com.opensymphony.xwork2.util.ValueStack;

public class MessageTag extends ContextBeanTag {

  private static final long serialVersionUID = 1L;

  protected String _key;

  public Component getBean(ValueStack stack, HttpServletRequest req,
      HttpServletResponse res) {
    return new MessageComponent(stack);
  }

  public void setKey(String key) {
    _key = key;
  }

  protected void populateParams() {
    super.populateParams();
    MessageComponent tag = (MessageComponent) getComponent();
    tag.setKey(_key);
  }
}
