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
