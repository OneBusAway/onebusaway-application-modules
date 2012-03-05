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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class JConsoleDialog extends JDialog {

  private static final long serialVersionUID = 1L;

  private final JPanel contentPanel = new JPanel();

  private final JTextPane _consoleTextPane = new JTextPane();

  /**
   * Create the dialog.
   */
  public JConsoleDialog(String consoleLogPath) {
    setBounds(100, 100, 900, 300);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new BorderLayout(0, 0));
    {
      JScrollPane scrollPane = new JScrollPane();
      contentPanel.add(scrollPane, BorderLayout.CENTER);
      {
        _consoleTextPane.setEditable(false);
        scrollPane.setViewportView(_consoleTextPane);
      }
    }
    {
      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            // TODO: Maybe we should do a bit more cleanup?
            System.exit(0);
          }
        });
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton);
      }
    }

    OutputStream additionalOutput = null;
    if (consoleLogPath != null) {
      try {
        additionalOutput = new BufferedOutputStream(new FileOutputStream(
            consoleLogPath));
      } catch (IOException ex) {
        throw new IllegalStateException(ex);
      }
    }
    System.setOut(new PrintStream(new ConsoleOutputStream(removeNulls(
        System.out, additionalOutput))));
    System.setErr(new PrintStream(new ConsoleOutputStream(removeNulls(
        System.err, additionalOutput))));
  }

  private OutputStream[] removeNulls(OutputStream... values) {
    List<OutputStream> nonNull = new ArrayList<OutputStream>();
    for (OutputStream out : values) {
      if (out != null)
        nonNull.add(out);
    }
    return nonNull.toArray(new OutputStream[nonNull.size()]);
  }

  private void updateTextPane(final String value) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        Document doc = _consoleTextPane.getDocument();

        try {
          doc.insertString(doc.getLength(), value, null);
        } catch (BadLocationException ex) {
          throw new IllegalStateException(ex);
        }
      }
    });
  }

  private class ConsoleOutputStream extends OutputStream {

    private final OutputStream[] _outputs;

    public ConsoleOutputStream(OutputStream... outputs) {
      _outputs = outputs;
    }

    @Override
    public void write(int b) throws IOException {
      for (OutputStream out : _outputs)
        out.write(b);
      updateTextPane(String.valueOf((char) b));
    }

    @Override
    public void write(byte[] b) throws IOException {
      for (OutputStream out : _outputs)
        out.write(b);
      updateTextPane(new String(b));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      for (OutputStream out : _outputs)
        out.write(b, off, len);
      updateTextPane(new String(b, off, len));
    }
  }
}
