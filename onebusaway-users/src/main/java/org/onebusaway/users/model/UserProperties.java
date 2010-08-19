package org.onebusaway.users.model;

import java.io.Serializable;

import org.onebusaway.users.services.UserPropertiesMigration;

/**
 * Remember: you can add and remove fields from a user properties implementation
 * object and previously serialized objects will leave new fields uninitialized
 * and old fields will be ignored. However, if you need to change the definition
 * of a field, then you need to create a new user properties implementation and
 * deal with migration using {@link UserPropertiesMigration}.
 * 
 * @author bdferris
 * @see UserPropertiesMigration
 */
public interface UserProperties extends Serializable {

}
