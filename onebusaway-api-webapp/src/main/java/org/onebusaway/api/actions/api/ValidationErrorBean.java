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
