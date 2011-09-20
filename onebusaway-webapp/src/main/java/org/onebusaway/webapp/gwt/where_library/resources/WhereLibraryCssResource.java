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
package org.onebusaway.webapp.gwt.where_library.resources;

import org.onebusaway.presentation.services.resources.WebappImportedWithPrefix;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;

@ImportedWithPrefix("WhereLibrary")
@WebappImportedWithPrefix("WhereLibrary")
public interface WhereLibraryCssResource extends CssResource {

  public String arrivalStatusOnTime();
  
  public String arrivalStatusNoInfo();

  public String arrivalStatusEarly();

  public String arrivalStatusDelayed();
  
  public String arrivalStatusDepartedOnTime();
  
  public String arrivalStatusDepartedNoInfo();

  public String arrivalStatusDepartedEarly();

  public String arrivalStatusDepartedDelayed();
  
  public String arrivalStatusCancelled();

}
