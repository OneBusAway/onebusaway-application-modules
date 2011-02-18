package org.onebusaway.presentation.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ContextBeanTag;

import com.opensymphony.xwork2.util.ValueStack;

public class JsonTag extends ContextBeanTag {

  private static final long serialVersionUID = 1L;

  protected String _value;

  private boolean _escapeJavaScript = false;

  private boolean _ignoreHierarchy = true;

  private boolean _excludeNullProperties = true;

  public Component getBean(ValueStack stack, HttpServletRequest req,
      HttpServletResponse res) {
    return new JsonComponent(stack);
  }

  public void setValue(String value) {
    _value = value;
  }

  public void setEscapeJavaScript(boolean escapeJavaScript) {
    _escapeJavaScript = escapeJavaScript;
  }

  public void setIgnoreHierarchy(boolean ignoreHiearchy) {
    _ignoreHierarchy = ignoreHiearchy;
  }

  public void setExcludeNullProperties(boolean excludeNullProperties) {
    _excludeNullProperties = excludeNullProperties;
  }

  protected void populateParams() {
    super.populateParams();
    JsonComponent tag = (JsonComponent) getComponent();
    tag.setValue(_value);
    tag.setEscapeJavaScript(_escapeJavaScript);
    tag.setIgnoreHierarchy(_ignoreHierarchy);
    tag.setExcludeNullProperties(_excludeNullProperties);
  }
}
