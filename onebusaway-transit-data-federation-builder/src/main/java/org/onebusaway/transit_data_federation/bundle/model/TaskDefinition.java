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
package org.onebusaway.transit_data_federation.bundle.model;

import java.util.ArrayList;
import java.util.List;

public class TaskDefinition {

  private String taskName;

  private String beforeTaskName;

  private List<String> afterTaskNames = new ArrayList<String>();

  private Runnable task;

  private String taskBeanName;

  private Runnable taskWhenSkipped;

  private String taskWhenSkippedBeanName;

  private boolean enabled = true;

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

  public void setAfterTaskName(String afterTaskName) {
    this.afterTaskNames.add(afterTaskName);
  }

  public List<String> getAfterTaskNames() {
    return afterTaskNames;
  }
  
  public void setAfterTaskNames(List<String> afterTaskNames) {
    this.afterTaskNames.addAll(afterTaskNames);
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

  public Runnable getTaskWhenSkipped() {
    return taskWhenSkipped;
  }

  public void setTaskWhenSkipped(Runnable taskWhenSkipped) {
    this.taskWhenSkipped = taskWhenSkipped;
  }

  public String getTaskWhenSkippedBeanName() {
    return taskWhenSkippedBeanName;
  }

  public void setTaskWhenSkippedBeanName(String taskWhenSkippedBeanName) {
    this.taskWhenSkippedBeanName = taskWhenSkippedBeanName;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String toString() {
    return "TaskDef(taskName=" + taskName + " beforeTaskName=" + beforeTaskName
        + " afterTaskNames=" + afterTaskNames + " task=" + task
        + " taskBeanName=" + taskBeanName + " enabled=" + enabled + ")";
  }
}
