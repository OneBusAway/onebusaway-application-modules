package org.onebusaway.presentation.tags;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import org.apache.commons.lang.xwork.StringEscapeUtils;
import org.apache.struts2.components.ContextBean;
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

    try {
      Collection<Pattern> empty = Collections.emptyList();
      json = JSONUtil.serialize(value, empty, empty, _ignoreHierarchy, _excludeNullProperties);
    } catch (JSONException ex) {
      LOG.error("Could not generate json from value", ex);
    }

    if (json != null) {

      if (_escapeJavaScript) {
        json = StringEscapeUtils.escapeJavaScript(json);
      }

      if (getVar() != null) {
        /**
         * We either write the url out to a variable
         */
        putInContext(json);
      } else {
        /**
         * Or otherwise print out the url directly
         */
        try {
          writer.write(json);
        } catch (IOException e) {
          LOG.error("Could not write out json value", e);
        }
      }
    }

    return super.end(writer, "");
  }
}
