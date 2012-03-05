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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.onebusaway.quickstart.GuiQuickstartDataModel;
import org.onebusaway.quickstart.bootstrap.gui.widgets.JCustomTextField;
import javax.swing.UIManager;

public class GtfsPathPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private GuiQuickstartDataModel _model;

  private JCustomTextField gtfsPathTextField;

  /**
   * Create the panel.
   * 
   * @param model
   */
  public GtfsPathPanel(GuiQuickstartDataModel model) {
    _model = model;

    setLayout(new MigLayout("", "[450px]", "[][][]"));

    JTextPane txtpnWhat = new JTextPane();
    txtpnWhat.setEditable(false);
    txtpnWhat.setBackground(UIManager.getColor("control"));
    txtpnWhat.setText("OneBusAway reads transit schedule data in the GTFS format.  Please specify the path to your GTFS feed so that we can use it to build the OneBusAway transit data bundle.");
    add(txtpnWhat, "cell 0 0");

    gtfsPathTextField = new JCustomTextField();
    add(gtfsPathTextField, "cell 0 1,growx");
    gtfsPathTextField.setColumns(10);

    JButton btnNewButton = new JButton(
        Messages.getString("BundleDirectorySelectionPanel.4")); //$NON-NLS-1$
    btnNewButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        handleChooseGtfsPath();
      }
    });
    add(btnNewButton, "cell 0 2");
    initDataBindings();

    gtfsPathTextField.addTextPropertyChangeEvent();
  }

  private void handleChooseGtfsPath() {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    int rc = chooser.showOpenDialog(this);
    if (rc == JFileChooser.APPROVE_OPTION) {
      File path = chooser.getSelectedFile();
      _model.setGtfsPath(path.getAbsolutePath());
    }
  }

  protected void initDataBindings() {
    BeanProperty<GuiQuickstartDataModel, String> bootstrapDataModelBeanProperty = BeanProperty.create("gtfsPath");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<GuiQuickstartDataModel, String, JTextField, String> autoBinding = Bindings.createAutoBinding(
        UpdateStrategy.READ_WRITE, _model, bootstrapDataModelBeanProperty,
        gtfsPathTextField, jTextFieldBeanProperty);
    autoBinding.bind();
  }
}
