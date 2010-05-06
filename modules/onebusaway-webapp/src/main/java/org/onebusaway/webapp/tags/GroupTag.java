package org.onebusaway.webapp.tags;

import com.opensymphony.xwork2.util.ValueStack;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ContextBeanTag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

public class GroupTag extends ContextBeanTag {

  private static final long serialVersionUID = 1L;

  protected String _value;

  public Component getBean(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
    return new GroupComponent(stack);
  }

  public void setValue(String value) {
    _value = value;
  }

  protected void populateParams() {
    super.populateParams();

    GroupComponent tag = (GroupComponent) getComponent();
    tag.setValue(_value);
  }

  public int doEndTag() throws JspException {
    component = null;
    return EVAL_PAGE;
  }

  public int doAfterBody() throws JspException {

    boolean again = component.end(pageContext.getOut(), getBody());
    bodyContent.clearBody();
    
    if (again) {
      return EVAL_BODY_AGAIN;
    } else {
      return SKIP_BODY;
    }
  }

}
