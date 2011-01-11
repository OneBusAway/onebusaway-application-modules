package org.onebusaway.presentation.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ContextBeanTag;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.opensymphony.xwork2.util.ValueStack;

public class ResourceUrlTag extends ContextBeanTag {

  private static final long serialVersionUID = 1L;

  protected String _value;

  public Component getBean(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
    ResourceUrlComponent component = new ResourceUrlComponent(stack);
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
    context.getAutowireCapableBeanFactory().autowireBean(component);
    return component;
  }

  public void setValue(String value) {
    _value = value;
  }

  protected void populateParams() {
    super.populateParams();
    ResourceUrlComponent tag = (ResourceUrlComponent) getComponent();
    tag.setValue(_value);
  }
}
