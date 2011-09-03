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
package org.onebusaway.webapp.gwt.where_library.view;

import com.google.gwt.resources.client.CssResource;

public interface StopFinderCssResource extends CssResource {

  public String resultList();

  public String resultListPrimary();

  public String resultListAdditional();

  public String resultListHeader();

  public String resultListWarning();

  public String resultListEntry();

  public String resultListEntryName();

  public String resultListEntryDescription();

  public String resultListMoreInfoLink();

  public String resultListClear();
  
  public String stopInfoWindowPanel();
  
  public String stopInfoWindowRoutesPanel();
  
  public String stopInfoWindowRoutesSubPanel();

  public String stopInfoWindowRouteShortNameEntry();

  public String stopInfoWindowRouteLongNameEntry();

  public String stopInfoWindowRouteReallyLongNameEntry();
}
