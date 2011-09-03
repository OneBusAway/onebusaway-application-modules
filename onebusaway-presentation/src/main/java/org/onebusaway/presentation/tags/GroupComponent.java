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

import com.opensymphony.xwork2.util.ValueStack;

import org.apache.struts2.components.ContextBean;
import org.apache.struts2.util.MakeIterator;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupComponent extends ContextBean {

  private String _value;

  private Iterator<?> _iterator;

  private List<Object> _groups = new ArrayList<Object>();

  private List<Object> _currentGroup = null;

  private String _lastKey = null;

  public GroupComponent(ValueStack stack) {
    super(stack);
  }

  public void setValue(String value) {
    _value = value;
  }

  @Override
  public boolean usesBody() {
    return true;
  }

  @Override
  public boolean start(Writer writer) {

    ValueStack stack = getStack();

    if (_value == null)
      _value = "top";

    _iterator = MakeIterator.convert(findValue(_value));

    // get the first
    if ((_iterator != null) && _iterator.hasNext()) {
      Object currentValue = _iterator.next();
      stack.push(currentValue);
      return true;
    } else {
      super.end(writer, "");
      return false;
    }
  }

  @Override
  public boolean end(Writer writer, String body) {

    ValueStack stack = getStack();

    if (_iterator != null) {
      Object value = stack.pop();
      if (_lastKey == null || !_lastKey.equals(body)) {
        _currentGroup = new ArrayList<Object>();
        _groups.add(_currentGroup);
      }
      _currentGroup.add(value);
      _lastKey = body;
    }

    if (_iterator != null && _iterator.hasNext()) {
      Object currentValue = _iterator.next();
      stack.push(currentValue);
      return true;
    } else {
      putInContext(_groups);
      return super.end(writer, "");
    }
  }
}
