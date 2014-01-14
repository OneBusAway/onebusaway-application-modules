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
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.onebusaway.quickstart.GuiQuickstartDataModel;
import org.onebusaway.quickstart.bootstrap.gui.widgets.JCustomTextField;

public class GtfsRealtimePathsPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private GuiQuickstartDataModel model;

  private JCustomTextField tripUpdatesUrlTextField;
  private JCustomTextField vehiclePositionsUrlTextField;
  private JCustomTextField alertsUrlTextField;

  /**
   * Create the panel.
   */
  public GtfsRealtimePathsPanel(GuiQuickstartDataModel model) {
    this.model = model;

    setLayout(new MigLayout("", "[450px,grow]", "[][][][][][][]"));

    JTextPane txtpnIfYouHave = new JTextPane();
    txtpnIfYouHave.setEditable(false);
    txtpnIfYouHave.setBackground(UIManager.getColor("control"));
    txtpnIfYouHave.setText("If you have access to real-time transit information in the GTFS-realtime format for your agency, specify the source URLs here.");
    txtpnIfYouHave.setToolTipText("");
    add(txtpnIfYouHave, "cell 0 0,grow");

    JLabel lblTripUpdatesUrl = new JLabel("Trip Updates URL:");
    add(lblTripUpdatesUrl, "cell 0 1");

    tripUpdatesUrlTextField = new JCustomTextField();
    add(tripUpdatesUrlTextField, "cell 0 2,growx");
    tripUpdatesUrlTextField.setColumns(10);

    JLabel lblVehiclePositionsUrl = new JLabel("Vehicle Positions URL:");
    add(lblVehiclePositionsUrl, "cell 0 3");

    vehiclePositionsUrlTextField = new JCustomTextField();
    add(vehiclePositionsUrlTextField, "cell 0 4,growx");
    vehiclePositionsUrlTextField.setColumns(10);

    JLabel lblAlertsUrl = new JLabel("Alerts URL:");
    add(lblAlertsUrl, "cell 0 5");

    alertsUrlTextField = new JCustomTextField();
    add(alertsUrlTextField, "cell 0 6,growx");
    alertsUrlTextField.setColumns(10);
    initDataBindings();

    tripUpdatesUrlTextField.addTextPropertyChangeEvent();
    vehiclePositionsUrlTextField.addTextPropertyChangeEvent();
    alertsUrlTextField.addTextPropertyChangeEvent();
  }
  protected void initDataBindings() {
    BeanProperty<GuiQuickstartDataModel, String> bootstrapDataModelBeanProperty = BeanProperty.create("tripUpdatesUrl");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<GuiQuickstartDataModel, String, JTextField, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, model, bootstrapDataModelBeanProperty, tripUpdatesUrlTextField, jTextFieldBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<GuiQuickstartDataModel, String> bootstrapDataModelBeanProperty_1 = BeanProperty.create("vehiclePositionsUrl");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<GuiQuickstartDataModel, String, JTextField, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, model, bootstrapDataModelBeanProperty_1, vehiclePositionsUrlTextField, jTextFieldBeanProperty_1);
    autoBinding_1.bind();
    //
    BeanProperty<GuiQuickstartDataModel, String> bootstrapDataModelBeanProperty_2 = BeanProperty.create("alertsUrl");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_2 = BeanProperty.create("text");
    AutoBinding<GuiQuickstartDataModel, String, JTextField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, model, bootstrapDataModelBeanProperty_2, alertsUrlTextField, jTextFieldBeanProperty_2);
    autoBinding_2.bind();
  }
}
