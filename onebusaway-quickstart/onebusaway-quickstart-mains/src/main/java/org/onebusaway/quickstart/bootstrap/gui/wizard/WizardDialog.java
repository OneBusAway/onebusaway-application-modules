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
package org.onebusaway.quickstart.bootstrap.gui.wizard;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;

public class WizardDialog extends JDialog {

  private static final long serialVersionUID = 1L;

  private WizardController wizardController;

  private final JPanel contentPanel = new JPanel();
  private JButton nextButton;
  private JButton backButton;

  /**
   * Create the dialog.
   * 
   * @param controller
   */
  public WizardDialog(WizardController controller) {
    this.wizardController = controller;
    controller.addPropertyChangeListener(new WizardControllerPropertyChangeHandler());
    controller.addCompletionListener(new WizardCompletionHandler());

    setBounds(100, 100, 450, 300);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new BorderLayout(0, 0));
    {
      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            wizardController.handleCancelButton();
          }
        });
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton);
      }
      {
        backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            wizardController.handleBackButton();
          }
        });
        buttonPane.add(backButton);
      }
      {
        nextButton = new JButton("Next");
        nextButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            wizardController.handleNextButton();
          }
        });
        nextButton.setActionCommand("OK");
        buttonPane.add(nextButton);
        getRootPane().setDefaultButton(nextButton);
      }
    }
    initDataBindings();
    updateControlPanel();
  }

  protected void initDataBindings() {
    BeanProperty<WizardController, Boolean> wizardControllerBeanProperty = BeanProperty.create("nextButtonEnabled");
    BeanProperty<JButton, Boolean> jButtonBeanProperty = BeanProperty.create("enabled");
    AutoBinding<WizardController, Boolean, JButton, Boolean> autoBinding = Bindings.createAutoBinding(
        UpdateStrategy.READ, wizardController, wizardControllerBeanProperty,
        nextButton, jButtonBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<WizardController, Boolean> wizardControllerBeanProperty_1 = BeanProperty.create("backButtonEnabled");
    AutoBinding<WizardController, Boolean, JButton, Boolean> autoBinding_1 = Bindings.createAutoBinding(
        UpdateStrategy.READ, wizardController, wizardControllerBeanProperty_1,
        backButton, jButtonBeanProperty);
    autoBinding_1.bind();
  }

  private void updateControlPanel() {
    contentPanel.removeAll();
    WizardPanelController controller = wizardController.getCurrentController();
    if (controller != null) {
      JPanel panel = controller.getPanel();
      if (panel != null) {
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
      }
    }
  }

  private class WizardControllerPropertyChangeHandler implements
      PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      String name = evt.getPropertyName();
      if (name.equals(WizardController.PROPERTY_CURRENT_PANEL)) {
        updateControlPanel();
      }
    }
  }

  private class WizardCompletionHandler implements WizardCompletionListener {

    @Override
    public void handleCanceled() {
      handleFinished();
    }

    @Override
    public void handleFinished() {
      WizardDialog.this.setVisible(false);
    }
  }
}
