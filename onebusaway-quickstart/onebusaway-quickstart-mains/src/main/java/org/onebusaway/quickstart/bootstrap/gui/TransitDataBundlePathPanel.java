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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.onebusaway.quickstart.GuiQuickstartDataModel;
import org.onebusaway.quickstart.bootstrap.gui.widgets.JCustomTextField;

public class TransitDataBundlePathPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private GuiQuickstartDataModel model;

  private JCustomTextField transitDataBundlePathTextField;

  /**
   * Create the panel.
   * 
   * @param model2
   */
  public TransitDataBundlePathPanel(GuiQuickstartDataModel model) {
    this.model = model;
    setLayout(new MigLayout("", "[450px]", "[][][]"));

    JTextPane transitDataBundlePathDescription = new JTextPane();
    transitDataBundlePathDescription.setEditable(false);
    transitDataBundlePathDescription.setBackground(UIManager.getColor("control"));
    transitDataBundlePathDescription.setText("The Transit Data Bundle directory determines where the optimized bundle of transit data, which powers the OneBusAway application suite, will be stored.");
    add(transitDataBundlePathDescription, "cell 0 0");

    transitDataBundlePathTextField = new JCustomTextField();
    add(transitDataBundlePathTextField, "cell 0 1,growx");
    transitDataBundlePathTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
    transitDataBundlePathTextField.setAlignmentY(Component.TOP_ALIGNMENT);
    transitDataBundlePathTextField.setColumns(20);

    JButton chooseTransitDataBundlePathButton = new JButton(
        Messages.getString("BundleDirectorySelectionPanel.4")); //$NON-NLS-1$
    chooseTransitDataBundlePathButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        handleChooseTransitDataBundlePath();
      }
    });
    add(chooseTransitDataBundlePathButton, "cell 0 2");
    initDataBindings();
    
    transitDataBundlePathTextField.addTextPropertyChangeEvent();
  }

  protected void initDataBindings() {
    BeanProperty<GuiQuickstartDataModel, String> bootstrapDataModelBeanProperty = BeanProperty.create("transitDataBundlePath");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<GuiQuickstartDataModel, String, JTextField, String> autoBinding = Bindings.createAutoBinding(
        UpdateStrategy.READ_WRITE, model, bootstrapDataModelBeanProperty,
        transitDataBundlePathTextField, jTextFieldBeanProperty);
    autoBinding.bind();
  }

  private void handleChooseTransitDataBundlePath() {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int rc = chooser.showOpenDialog(this);
    if (rc == JFileChooser.APPROVE_OPTION) {
      File path = chooser.getSelectedFile();
      model.setTransitDataBundlePath(path.getAbsolutePath());
    }
  }

}
