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
package org.onebusaway.webapp.gwt.where_library;

import org.onebusaway.webapp.gwt.common.CommonLibrary;
import org.onebusaway.webapp.gwt.where_library.resources.WhereLibraryResources;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;

public class WhereLibrary implements EntryPoint {

  public static final WhereMessages MESSAGES = GWT.create(WhereMessages.class);
  
  public static final WhereLibraryResources INSTANCE = GWT.create(WhereLibraryResources.class);

  public void onModuleLoad() {

    CommonLibrary.registerService(WebappServiceAsync.SERVICE_PATH,
        WebappServiceAsync.SERVICE);

    UserContext.getContext();

    StyleInjector.inject(WhereLibrary.INSTANCE.getCss().getText());
  }
}
