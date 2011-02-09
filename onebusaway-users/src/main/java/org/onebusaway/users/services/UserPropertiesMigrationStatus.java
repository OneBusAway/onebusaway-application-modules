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
