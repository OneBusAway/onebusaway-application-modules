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
package org.onebusaway.api.actions.api;

import java.util.List;
import java.util.Map;

public class ValidationErrorBean {
  private List<String> actionErrors;
  private Map<String, List<String>> fieldErrors;

  public ValidationErrorBean(List<String> actionErrors, Map<String,List<String>> fieldErrors) {
    this.actionErrors = actionErrors;
    this.fieldErrors = fieldErrors;
  }
  public List<String> getActionErrors() {
    return actionErrors;
  }

  public Map<String, List<String>> getFieldErrors() {
    return fieldErrors;
  }

}
