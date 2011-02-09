package org.onebusaway.users.services;

import org.onebusaway.users.model.UserProperties;

public interface UserPropertiesMigration {

  public <T extends UserProperties> T migrate(UserProperties properties,
      Class<T> target);

  public boolean needsMigration(UserProperties properties, Class<?> target);

  public <T extends UserProperties> void startUserPropertiesBulkMigration(
      Class<T> target);

  public UserPropertiesMigrationStatus getUserPropertiesBulkMigrationStatus();
}
