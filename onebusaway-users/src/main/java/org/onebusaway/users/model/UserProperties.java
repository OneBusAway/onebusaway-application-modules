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
