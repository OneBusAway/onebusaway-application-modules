/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.nextbus.impl.rest.xstream;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class FalseConverter implements SingleValueConverter {

  public boolean canConvert(Class type) {
      return type.equals(boolean.class);
  }

  @Override
  public String toString(Object obj) {
    Boolean booleanField = (Boolean) obj;
    if(booleanField == null || !booleanField){
      return null;
    }
    return Boolean.toString(booleanField);
  }

  @Override
  public Object fromString(String str) {
    return str;
  }
}