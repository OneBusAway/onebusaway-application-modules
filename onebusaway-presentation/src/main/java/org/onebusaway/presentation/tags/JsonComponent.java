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

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.struts2.components.ContextBean;
import org.apache.struts2.json.DefaultJSONWriter;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

public class JsonComponent extends ContextBean {

  private static final Logger LOG = LoggerFactory.getLogger(JsonComponent.class);

  private boolean _escapeJavaScript = false;
  
  private boolean _ignoreHierarchy = true;
  
  private boolean _excludeNullProperties = true;

  private String _value;

  public JsonComponent(ValueStack stack) {
    super(stack);
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

  @Override
  public boolean end(Writer writer, String body) {

    if (_value == null)
      _value = "top";

    Object value = findValue(_value);

    String json = null;


    if (json != null) {

      if (_escapeJavaScript) {
        json = StringEscapeUtils.escapeEcmaScript(json);
      }

      if (getVar() != null) {
        /**
         * We either write the url out to a variable
         */
        Collection<Pattern> empty = Collections.emptyList();
        try {
          json = new DefaultJSONWriter().write(value, empty, empty, _excludeNullProperties);
        } catch (JSONException e) {
          LOG.error("Could not write out json value", e);
        }
        putInContext(json);
      } else {
        /**
         * Or otherwise print out the url directly
         */
        try {
          Collection<Pattern> empty = Collections.emptyList();
          new JSONUtil().serialize(writer, value, empty, empty,_ignoreHierarchy, _excludeNullProperties);

        } catch (IOException | JSONException e) {
          LOG.error("Could not write out json value", e);
        }
      }
    }

    return super.end(writer, "");
  }
}
