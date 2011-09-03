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
package org.asteriskjava.fastagi.command;

import java.util.Map;
import java.util.TreeMap;

public class AgiRequestCommand extends AbstractAgiCommand {

  private static final long serialVersionUID = 1L;

  private Map<String, String> _parameters = new TreeMap<String, String>();
  
  public Map<String,String> getParameters() {
    return _parameters;
  }

  @Override
  public String buildCommand() {
    StringBuilder b = new StringBuilder();
    for (Map.Entry<String, String> entry : _parameters.entrySet()) {
      b.append(entry.getKey());
      b.append(": ");
      b.append(entry.getValue());
      b.append("\n");
    }
    return b.toString();
  }
}
