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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import org.onebusaway.quickstart.GuiQuickstartDataModel;
import org.onebusaway.quickstart.bootstrap.gui.wizard.AbstractWizardPanelController;
import org.onebusaway.quickstart.bootstrap.gui.wizard.WizardController;

public class GtfsPathWizardPanelController extends
    AbstractWizardPanelController {

  private PropertyChangeHandler handler = new PropertyChangeHandler();

  private GuiQuickstartDataModel model;

  private WizardController controller;

  public GtfsPathWizardPanelController(GuiQuickstartDataModel model,
      WizardController controller) {
    this.model = model;
    this.controller = controller;
  }

  @Override
  public JPanel getPanel() {
    return new GtfsPathPanel(model);
  }

  @Override
  public Object getBackPanelId() {
    return QuickStartTypeWizardPanelController.class;
  }

  @Override
  public Object getNextPanelId() {
    String path = model.getGtfsPath();
    if (path == null || path.isEmpty())
      return null;
    return model.isRunEnabled() ? GtfsRealtimePathsWizardPanelController.class
        : RunWizardPanelController.class;
  }

  @Override
  public void willSetVisible(WizardController controller, boolean visible) {
    super.willSetVisible(controller, visible);
    if (visible) {
      model.addPropertyChangeListener(handler);
    } else {
      model.addPropertyChangeListener(handler);
    }
  }

  private class PropertyChangeHandler implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals("transitDataBundlePath")) {
        String path = (String) evt.getNewValue();
        controller.setNextButtonEnabled(!(path == null || path.isEmpty()));
      }
    }
  }

}
