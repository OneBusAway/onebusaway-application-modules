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
package org.onebusaway.quickstart.bootstrap.gui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.onebusaway.quickstart.GuiQuickstartDataModel;
import org.onebusaway.quickstart.bootstrap.gui.widgets.PropertyEventHelpers;

public class QuickStartTypePanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private GuiQuickstartDataModel model;
  private JRadioButton buildOnlyRadioButton;
  private JRadioButton runOnlyRadioButton;
  private JRadioButton buildAndRunRadioButton;

  /**
   * Create the panel.
   */
  public QuickStartTypePanel(GuiQuickstartDataModel model) {
    this.model = model;

    setLayout(new MigLayout("", "[450px,grow]",
        "[][][][]"));

    JTextPane txtpnYouNeedTo = new JTextPane();
    txtpnYouNeedTo.setEditable(false);
    txtpnYouNeedTo.setBackground(UIManager.getColor("control"));
    txtpnYouNeedTo.setText("You need to build a transit data bundle from raw input data (GTFS, etc) at least once before you run the webapp, or any time your raw input data changes. ");
    add(txtpnYouNeedTo, "cell 0 0");

    buildOnlyRadioButton = new JRadioButton(
        "Build transit data bundle only");
    add(buildOnlyRadioButton, "cell 0 1");

    runOnlyRadioButton = new JRadioButton("Run webapp only");
    add(runOnlyRadioButton, "cell 0 2");

    buildAndRunRadioButton = new JRadioButton(
        "Build transit data bundle and run webapp");
    buildAndRunRadioButton.setSelected(true);
    add(buildAndRunRadioButton, "cell 0 3");

    ButtonGroup group = new ButtonGroup();
    group.add(buildOnlyRadioButton);
    group.add(runOnlyRadioButton);
    group.add(buildAndRunRadioButton);
    initDataBindings();
    
    PropertyEventHelpers.addSelectedPropertyChangeEventForJRadioButton(buildOnlyRadioButton);
    PropertyEventHelpers.addSelectedPropertyChangeEventForJRadioButton(runOnlyRadioButton);
    PropertyEventHelpers.addSelectedPropertyChangeEventForJRadioButton(buildAndRunRadioButton);
    
  }
  protected void initDataBindings() {
    BeanProperty<GuiQuickstartDataModel, Boolean> bootstrapDataModelBeanProperty = BeanProperty.create("buildOnly");
    BeanProperty<JRadioButton, Boolean> jRadioButtonBeanProperty = BeanProperty.create("selected");
    AutoBinding<GuiQuickstartDataModel, Boolean, JRadioButton, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, model, bootstrapDataModelBeanProperty, buildOnlyRadioButton, jRadioButtonBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<GuiQuickstartDataModel, Boolean> bootstrapDataModelBeanProperty_1 = BeanProperty.create("runOnly");
    AutoBinding<GuiQuickstartDataModel, Boolean, JRadioButton, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, model, bootstrapDataModelBeanProperty_1, runOnlyRadioButton, jRadioButtonBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<GuiQuickstartDataModel, Boolean> bootstrapDataModelBeanProperty_2 = BeanProperty.create("buildAndRun");
    AutoBinding<GuiQuickstartDataModel, Boolean, JRadioButton, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, model, bootstrapDataModelBeanProperty_2, buildAndRunRadioButton, jRadioButtonBeanProperty);
    autoBinding_2.bind();
  }
}
