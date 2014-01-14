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
import javax.swing.JTextPane;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

public class WelcomePanel extends JPanel {

  private static final long serialVersionUID = 1L;

  /**
   * Create the panel.
   */
  public WelcomePanel() {
    setLayout(new MigLayout("", "[450px]", "[grow]"));
    
    JTextPane welcomeText = new JTextPane();
    welcomeText.setEditable(false);
    welcomeText.setBackground(UIManager.getColor("control"));
    welcomeText.setText("Welcome to the OneBusAway Quick-Start wizard.  This wizard will walk you through configuring the OneBusAway application with your transit data and starting the application.");
    add(welcomeText, "cell 0 0,grow");

  }

}
