package org.onebusaway.transit_data_federation.bundle.model;

public class TaskDefinition {

  private String taskName;

  private String beforeTaskName;

  private String afterTaskName;

  private Runnable task;

  private String taskBeanName;

  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  public String getBeforeTaskName() {
    return beforeTaskName;
  }

  public void setBeforeTaskName(String beforeTaskName) {
    this.beforeTaskName = beforeTaskName;
  }

  public String getAfterTaskName() {
    return afterTaskName;
  }

  public void setAfterTaskName(String afterTaskName) {
    this.afterTaskName = afterTaskName;
  }

  public Runnable getTask() {
    return task;
  }

  public void setTask(Runnable task) {
    this.task = task;
  }

  public String getTaskBeanName() {
    return taskBeanName;
  }

  public void setTaskBeanName(String taskBeanName) {
    this.taskBeanName = taskBeanName;
  }
}
