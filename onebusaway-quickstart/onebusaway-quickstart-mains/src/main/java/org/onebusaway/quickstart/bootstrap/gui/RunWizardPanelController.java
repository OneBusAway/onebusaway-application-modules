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

import org.onebusaway.quickstart.GuiQuickstartDataModel;
import org.onebusaway.quickstart.bootstrap.gui.wizard.AbstractWizardPanelController;
import org.onebusaway.quickstart.bootstrap.gui.wizard.WizardController;

public class RunWizardPanelController extends AbstractWizardPanelController {

  private GuiQuickstartDataModel model;

  public RunWizardPanelController(GuiQuickstartDataModel model) {
    this.model = model;
  }

  @Override
  public JPanel getPanel() {
    return new RunPanel(model);
  }

  @Override
  public Object getBackPanelId() {
    return model.isRunEnabled() ? GtfsRealtimePathsWizardPanelController.class
        : GtfsPathWizardPanelController.class;
  }

  @Override
  public Object getNextPanelId() {
    return WizardController.FINISH_PANEL_ID;
  }
}
