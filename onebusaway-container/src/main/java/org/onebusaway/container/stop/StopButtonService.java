/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.container.stop;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Utility class that pops up a simple Swing dialog with a "Stop" button that
 * will cause your program to gracefully exit when pressed. Useful when you need
 * a convenient way during testing for stopping a command-line program that
 * would typically run forever by default.
 * 
 * @author bdferris
 * 
 */
public class StopButtonService {

  private String _name;

  public void setName(String name) {
    _name = name;
  }

  public void start() {
    String title = _name == null ? "Stop Button" : "Stop Button: " + _name;
    JFrame frame = new JFrame(title);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    JButton button = new JButton("Exit");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
    button.setMinimumSize(new Dimension(500, 200));
    frame.getContentPane().add(button);
    frame.pack();
    frame.setVisible(true);
  }
}
