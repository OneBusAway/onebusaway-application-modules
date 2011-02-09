package org.onebusaway.presentation.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ContextBeanTag;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.opensymphony.xwork2.util.ValueStack;

public class ResourcesUrlTag extends ContextBeanTag {

  private static final long serialVersionUID = 1L;
  
  private String _id;

  public Component getBean(ValueStack stack, HttpServletRequest req,
      HttpServletResponse res) {
    ResourcesUrlComponent component = new ResourcesUrlComponent(stack);
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
    context.getAutowireCapableBeanFactory().autowireBean(component);
    return component;
  }
  
  public void setId(String id) {
    _id = id;
  }

  protected void populateParams() {
    super.populateParams();
    ResourcesUrlComponent tag = (ResourcesUrlComponent) getComponent();
    tag.setId(_id);
  }
}
