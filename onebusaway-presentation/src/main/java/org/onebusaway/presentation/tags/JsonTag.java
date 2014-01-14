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
