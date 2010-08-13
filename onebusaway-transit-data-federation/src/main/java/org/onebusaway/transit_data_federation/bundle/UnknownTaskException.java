package org.onebusaway.transit_data_federation.bundle;

public class UnknownTaskException extends Exception {

  private static final long serialVersionUID = 1L;

  private String _taskName;

  public UnknownTaskException(String taskName) {
    super("unknown task: name=" + taskName);
    _taskName = taskName;
  }

  public String getTaskName() {
    return _taskName;
  }

}
