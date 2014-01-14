/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
