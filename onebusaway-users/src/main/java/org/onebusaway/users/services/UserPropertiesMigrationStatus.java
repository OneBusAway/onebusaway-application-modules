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
package org.onebusaway.users.services;

public class UserPropertiesMigrationStatus {

  private int numberOfUsers;

  private int numberOfUsersProcessed;

  private int numberOfUsersConverted;

  private boolean complete;

  private boolean canceled;

  public int getNumberOfUsers() {
    return numberOfUsers;
  }

  public void setNumberOfUsers(int numberOfUsers) {
    this.numberOfUsers = numberOfUsers;
  }

  public int getNumberOfUsersProcessed() {
    return numberOfUsersProcessed;
  }

  public void setNumberOfUsersProcessed(int numberOfUsersProcessed) {
    this.numberOfUsersProcessed = numberOfUsersProcessed;
  }

  public int getNumberOfUsersConverted() {
    return numberOfUsersConverted;
  }

  public void setNumberOfUsersConverted(int numberOfUsersConverted) {
    this.numberOfUsersConverted = numberOfUsersConverted;
  }

  public boolean isComplete() {
    return complete;
  }

  public void setComplete(boolean complete) {
    this.complete = complete;
  }

  public boolean isCanceled() {
    return canceled;
  }

  public void setCanceled(boolean canceled) {
    this.canceled = canceled;
  }
}
