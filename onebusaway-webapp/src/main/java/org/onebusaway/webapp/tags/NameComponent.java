package org.onebusaway.webapp.tags;

import java.io.IOException;
import java.io.Writer;

import org.apache.struts2.components.ContextBean;
import org.onebusaway.transit_data.model.NameBean;

import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

public class NameComponent extends ContextBean {

  private static final Logger LOG = LoggerFactory.getLogger(NameComponent.class);

  private String _value;

  public NameComponent(ValueStack stack) {
    super(stack);
  }

  public void setValue(String value) {
    _value = value;
  }

  @Override
  public boolean usesBody() {
    return false;
  }

  @Override
  public boolean end(Writer writer, String body) {

    if (_value == null)
      _value = "top";

    Object obj = findValue(_value);

    if (obj instanceof NameBean) {
      NameBean name = (NameBean) obj;
      String value = name.getName();
      try {
        if (value != null)
          writer.write(value);
      } catch (IOException e) {
        LOG.error("Could not write out Text tag", e);
      }
    }

    return super.end(writer, "");
  }
}
