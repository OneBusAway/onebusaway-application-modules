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
package org.onebusaway.webapp.gwt.viewkit.resources;

import org.onebusaway.webapp.gwt.viewkit.ListViewController;
import org.onebusaway.webapp.gwt.viewkit.TabBarController;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;

@ImportedWithPrefix("ViewKit")
public interface ViewKitCssResource extends CssResource {

  /****
   * {@link ViewController}
   ****/
  
  public String ViewController();
  
  /****
   * {@link NavigationController}
   ****/
  
  public String NavigationController();
  
  public String NavigationControllerNavigationBar();

  /****
   * {@link TabBarController} Interface
   ****/

  public String TabBarController();

  public String TabBarControllerContent();
  
  public String TabBarControllerTabBar();

  public String TabBar();

  public String TabBarItem();

  public String TabBarItemNoImage();

  public String TabBarItemNoName();

  /****
   * {@link ListViewController} Methods
   ****/

  public String ListViewController();

  public String ListViewSection();

  public String ListViewRow();

  public String ListViewRowFirst();

  public String ListViewRowLast();

  public String ListViewRowText();

  public String ListViewRowDetailText();

  /****
   * {@link NavigationBar}
   ****/

  public String NavigationBar();

  public String NavigationBarLeftItem();

  public String NavigationBarCenterItem();

  public String NavigationBarRightItem();
  
  public String NavigationBarButton();

  public String NavigationBarButtonLeftArrow();
  
  public String NavigationBarButtonLeftRounded();
  
  public String NavigationBarButtonCenter();

  public String NavigationBarButtonRightRounded();
}
