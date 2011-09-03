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
package org.onebusaway.webapp.gwt.common.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.webapp.gwt.common.resources.CommonCssResources;
import org.onebusaway.webapp.gwt.common.resources.CommonResources;

/**
 * This Widget displays a list of items, each with a checkbox
 * next to it in order to allow the user to deselect ("delete") 
 * items.
 * 
 * Also, items in the list can be given a disabled styling.
 * 
 * A given ID is automatically de-duped so it cannot appear more than once.
 * 
 * NOTE: The user should define CSS for the following style classes:
 *   CheckBoxListWidget
 *   CheckBoxListWidgetItem
 *   CheckBoxListWidgetItemDisabled (required to support disabled items)
 */
public class CheckBoxListWidget extends Composite implements HasClickHandlers {
  
  private static CommonCssResources _css = CommonResources.INSTANCE.getCss();
  
  private VerticalMultiColumnPanel _panel;
  private List<String> _labels = new ArrayList<String>();
  private Comparator<String> _sort = null;
  private Map<String,Item> _idsToItems = new HashMap<String, Item>();
  private CheckBoxListWidget _this = this;
  
  /** In case of no list items, we put this. */
  private Label _emptyItem;
  
  /** Construct with specifier sorter (may be null) */
  public CheckBoxListWidget(Comparator<String> sorter, int maxColumnSize) {
    _panel = new VerticalMultiColumnPanel(maxColumnSize);
    initWidget(_panel); //Composite requires calling this
    setStyleName(_css.CheckBoxListWidget());
    _sort = sorter;
    
    _emptyItem = new Label("(none)");
    _emptyItem.setStyleName(_css.CheckBoxListWidgetItem());
    _panel.insert(_emptyItem, 0);
  }
  
  /** 
   * So that you can add handlers that do additional action after 
   * someone has clicked a checkbox.
   */
  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addDomHandler(handler, ClickEvent.getType());
  }
  
  /** Add an item to the list */
  public void addItem(final String label, final String id) {
    Item existing = _idsToItems.get(id);
    if (existing != null) {
      existing.setEnabled(true);
      return; //avoid duplicate IDs, instead enabled existing one
    }
    
    _panel.remove(_emptyItem);
        
    final Item item = new Item(id, label);
    
    _idsToItems.put(id, item);

    
    //Where to insert in sorted list
    int insertAt;
    if (_sort == null) {
      insertAt = _labels.size();
    } else {
      insertAt = 0;
      while (insertAt < _labels.size() &&
          _sort.compare(_labels.get(insertAt), label) < 0) {
        ++insertAt;
      }
    }
    
    _panel.insert(item.panel, insertAt);
    _labels.add(insertAt, label);
  }
  
  /** Returns checked, enabled IDs */
  public List<String> getIds() {
    List<String> ids = new ArrayList<String>();
    for (Item item : _idsToItems.values()) {
      if (!item.isDeleted() && item.isEnabled()) {
        ids.add(item.id);
      }
    }
    return ids;
  }
  
  /** Returns checked (enabled and disabled) IDs */
  public List<String> getNonDeletedIds() {
    List<String> ids = new ArrayList<String>();
    for (Item item : _idsToItems.values()) {
      if (!item.isDeleted()) {
        ids.add(item.id);
      }
    }
    return ids;
  }
  
  /** Get all IDs regardless of state. */
  public Collection<String> getAllIds() {
    return _idsToItems.keySet();
  }
  
  public void setEnabled(String id, boolean enabled) {
    _idsToItems.get(id).setEnabled(enabled);
  }  
  
  public void setDeleted(String id, boolean deleted) {
    _idsToItems.get(id).setDeleted(deleted);
  }
  
  /** A single item in the list */
  private class Item {
    String id;
    String label;
    HorizontalPanel panel;
    CheckBox checkBox;
    private boolean enabled = true;
    private boolean deleted = false;
    
    Item(String id, String label) {
      this.id = id;
      this.label = label;
      
      panel = new HorizontalPanel();
      panel.setStyleName(_css.CheckBoxListWidgetItem());
      panel.setWidth("100%");
      panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
      panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
                
      checkBox = new CheckBox(this.label);
      checkBox.setName(id);
      checkBox.setValue(true);
      checkBox.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          boolean checked = checkBox.getValue();
          setDeleted(!checked);
          
          _this.fireEvent(event); //is this needed?
        }
      });
      /*checkBox.addClickListener(new ClickListener() { //deprecated
        public void onClick(Widget w) {
          boolean checked = checkBox.isChecked();
          setDeleted(!checked);
          
          _listeners.fireClick(_this);
        }
      });*/
      panel.add(checkBox);
    }
    
    /** Update display to match state */
    void refresh() {
      //Yes, you can check/uncheck an item even if not enabled
      checkBox.setValue(!deleted); 
      
      //Items are gray if they are disabled or if merely unchecked.
      if (deleted || !enabled) {
        checkBox.addStyleName(_css.CheckBoxListWidgetItemDisabled());
      } else {
        checkBox.removeStyleName(_css.CheckBoxListWidgetItemDisabled());
      }
    }

    boolean isEnabled() {
      return enabled;
    }

    void setEnabled(boolean enabled) {
      this.enabled = enabled;
      refresh();
    }

    /** "Deleted" means the user has unchecked it. */
    boolean isDeleted() {
      return deleted;
    }

    void setDeleted(boolean deleted) {
      this.deleted = deleted;
      refresh();
    }
  }

}

