package org.onebusaway.webapp.tags;

import java.io.IOException;
import java.io.Writer;

import org.apache.struts2.components.ContextBean;
import org.onebusaway.users.impl.PhoneNumberLibrary;

import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

public class PhoneNumberComponent extends ContextBean {

  private static final Logger LOG = LoggerFactory.getLogger(PhoneNumberComponent.class);

  private String _value;

  public PhoneNumberComponent(ValueStack stack) {
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

    if (obj instanceof String) {

      String number = formatPhoneNumber((String) obj);

      try {
        writer.write(number);
      } catch (IOException e) {
        LOG.error("Could not write out Text tag", e);
      }
    }

    return super.end(writer, "");
  }

  private String formatPhoneNumber(String number) {
    String[] segments = PhoneNumberLibrary.segmentPhoneNumber(number);
    StringBuilder b = new StringBuilder();
    for (String segment : segments) {
      if (b.length() > 0)
        b.append('-');
      b.append(segment);
    }
    return b.toString();
  }
}
