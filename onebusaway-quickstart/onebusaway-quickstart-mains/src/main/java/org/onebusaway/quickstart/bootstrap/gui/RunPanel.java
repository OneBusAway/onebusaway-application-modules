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
import javax.swing.UIManager;

import org.onebusaway.quickstart.GuiQuickstartDataModel;

public class RunPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  /**
   * Create the panel.
   */
  public RunPanel(GuiQuickstartDataModel model) {
    setLayout(new MigLayout("", "[grow]", "[][][]"));

    JTextPane txtReadyToGo = new JTextPane();
    txtReadyToGo.setEditable(false);
    txtReadyToGo.setBackground(UIManager.getColor("control"));
    txtReadyToGo.setText("We're ready to go!  Here's what to expect:");
    add(txtReadyToGo, "cell 0 0");

    JTextPane txtOptions = new JTextPane();
    txtOptions.setText("");
    txtOptions.setBackground(UIManager.getColor("control"));
    txtOptions.setEditable(false);
    add(txtOptions, "cell 0 1");

    StringBuilder text = new StringBuilder();
    int index = 1;
    if (model.isBuildEnabled()) {
      text.append(index++);
      text.append(") ");
      text.append(Messages.getString("RunPanel.BuildMessage"));
    }
    if (model.isRunEnabled()) {
      if (text.length() > 0)
        text.append("\n\n");
      text.append(index++);
      text.append(") ");
      text.append(Messages.getString("RunPanel.RunMessage"));
    }
    txtOptions.setText(text.toString());

    JTextPane txtConsole = new JTextPane();
    txtConsole.setEditable(false);
    txtConsole.setBackground(UIManager.getColor("control"));
    txtConsole.setText(Messages.getString("RunPanel.ConsoleMessage")); //$NON-NLS-1$
    add(txtConsole, "cell 0 2");

  }

}
