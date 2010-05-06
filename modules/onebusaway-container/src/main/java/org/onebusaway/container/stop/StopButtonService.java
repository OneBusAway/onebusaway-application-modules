package org.onebusaway.container.stop;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

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
    button.setMinimumSize(new Dimension(500,200));
    frame.getContentPane().add(button);
    frame.pack();
    frame.setVisible(true);
  }
}
