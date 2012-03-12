/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.quickstart.bootstrap.gui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JRadioButton;

import org.jdesktop.beansbinding.AutoBinding;

/**
 * Helper methods to create virtual property change events for particular Swing
 * widgets to help with bi-directional data-binding as enabled with
 * {@link AutoBinding}.
 * 
 * @author bdferris
 */
public class PropertyEventHelpers {
  public static void addSelectedPropertyChangeEventForJRadioButton(
      final JRadioButton button) {
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        boolean selected = button.isSelected();
        button.firePropertyChange("selected", !selected, selected);
      }
    });
  }
}
