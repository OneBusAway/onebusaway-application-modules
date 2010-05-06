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
