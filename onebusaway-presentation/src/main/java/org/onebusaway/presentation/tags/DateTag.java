package org.onebusaway.presentation.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ContextBeanTag;

import com.opensymphony.xwork2.util.ValueStack;

public class DateTag extends ContextBeanTag {

  private static final long serialVersionUID = 1L;

  private String _format;

  private String _dateStyle;

  private String _timeStyle;

  protected String _value;

  private String _timeZone;

  public Component getBean(ValueStack stack, HttpServletRequest req,
      HttpServletResponse res) {
    return new DateComponent(stack);
  }

  public void setFormat(String format) {
    _format = format;
  }

  public void setDateStyle(String dateStyle) {
    _dateStyle = dateStyle;
  }

  public void setTimeStyle(String timeStyle) {
    _timeStyle = timeStyle;
  }

  public void setValue(String value) {
    _value = value;
  }

  public void setTimeZone(String timeZone) {
    _timeZone = timeZone;
  }

  protected void populateParams() {
    super.populateParams();
    DateComponent tag = (DateComponent) getComponent();
    tag.setFormat(_format);
    tag.setDateStyle(_dateStyle);
    tag.setTimeStyle(_timeStyle);
    tag.setValue(_value);
    tag.setTimeZone(_timeZone);
  }
}
