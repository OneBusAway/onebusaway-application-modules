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
package org.onebusaway.phone.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.asteriskjava.fastagi.AgiClientChannel;
import org.asteriskjava.fastagi.AgiClientScript;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.DefaultAgiClient;
import org.asteriskjava.fastagi.reply.AgiReply;

public class SimplePhoneClient {

  private static final String ARG_HOSTNAME = "hostname";

  private static final String ARG_PORT = "port";

  private static final String ARG_CALLER_ID = "callerId";

  private static final Pattern TEXT_PATTERN = Pattern.compile("^\"(.*)\" \".*\"$");

  public static void main(String[] args) throws IOException, ParseException {

    Parser parser = new GnuParser();
    Options options = buildOptions();
    CommandLine cli = parser.parse(options, args);

    String host = cli.getOptionValue(ARG_HOSTNAME, "localhost");
    int port = Integer.parseInt(cli.getOptionValue(ARG_PORT, "8001"));
    String callerId = cli.getOptionValue(ARG_CALLER_ID, "2000");

    AgiClientScriptImpl script = new AgiClientScriptImpl();
    setupGui(script);

    DefaultAgiClient client = new DefaultAgiClient(host, port, script);
    client.setCallerId(callerId);
    client.setNetworkScript("index.agi");
    client.run();
  }

  private static void setupGui(AgiClientScriptImpl script) {

    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    
    KeyPressHandler handler = new KeyPressHandler(script);

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.addKeyListener(handler);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(4, 3));
    panel.add(buttonPanel, BorderLayout.CENTER);

    String buttons = "123456789*0#";

    for (int i = 0; i < buttons.length(); i++)
      addButton(buttonPanel, script, handler, buttons.charAt(i));

    Document document = script.getDocument();

    final JTextArea textArea = new JTextArea(document);
    textArea.setEditable(false);
    textArea.addKeyListener(handler);
    document.addDocumentListener(new ScrollDocumentToEnd(textArea));

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setPreferredSize(new Dimension(300, 100));
    scrollPane.addKeyListener(handler);
    panel.add(scrollPane, BorderLayout.SOUTH);

    frame.getContentPane().add(panel);
    frame.pack();
    frame.setVisible(true);
  }

  private static Options buildOptions() {
    Options options = new Options();
    options.addOption(ARG_HOSTNAME, true, "hostname to connect to");
    options.addOption(ARG_PORT, true, "host port to connect to");
    options.addOption(ARG_CALLER_ID, true, "callerId to use");
    return options;
  }

  private static void addButton(JPanel panel, final AgiClientScriptImpl script,
      KeyPressHandler handler, final char key) {

    String label = Character.toString(key);
    JButton button = new JButton(label);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        script.pushChar(key);
      }
    });
    button.addKeyListener(handler);
    panel.add(button);
  }

  private static class AgiClientScriptImpl implements AgiClientScript {

    private static final String COMMAND_WAIT_FOR_DIGIT = "WAIT FOR DIGIT";

    private static final String COMMAND_SAY_ALPHA = "SAY ALPHA";

    private Deque<Character> _queue = new ArrayDeque<Character>();

    private Document _document = new PlainDocument();

    public Document getDocument() {
      return _document;
    }

    @Override
    public synchronized void service(AgiReply reply, AgiClientChannel channel)
        throws AgiException {

      String firstLine = reply.getFirstLine();

      if (firstLine.startsWith(COMMAND_WAIT_FOR_DIGIT)) {

        String token = firstLine.substring(COMMAND_WAIT_FOR_DIGIT.length());
        token = token.trim();
        long value = Long.parseLong(token);

        try {
          wait(value);
        } catch (InterruptedException e) {
          return;
        }

      } else if (firstLine.startsWith(COMMAND_SAY_ALPHA)) {

        String token = firstLine.substring(COMMAND_SAY_ALPHA.length());
        token = token.trim();

        Matcher matcher = TEXT_PATTERN.matcher(token);
        if (matcher.matches())
          token = matcher.group(1);

        if (token.length() > 0) {
          appendLineToOutput(token);

          long value = 40 * token.length();

          try {
            wait(value);
          } catch (InterruptedException e) {
            return;
          }
        }

      } else {
        System.out.println(firstLine);
      }

      char result = 0;

      if (!_queue.isEmpty()) {
        result = _queue.poll();
        appendLineToOutput(">> " + result);
      }

      channel.sendDigit(result);
    }

    private void appendLineToOutput(String token) {
      try {
        _document.insertString(_document.getLength(), token + "\n", null);
      } catch (BadLocationException ex) {
        throw new IllegalStateException(ex);
      }
    }

    public synchronized void pushChar(char value) {
      _queue.add(value);
      notify();
    }
  }

  private static class ScrollDocumentToEnd implements DocumentListener {

    private JTextArea _textArea;

    public ScrollDocumentToEnd(JTextArea textArea) {
      _textArea = textArea;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
      Document document = e.getDocument();
      try {
        String text = document.getText(0, document.getLength());
        int position = document.getLength() - 1;
        int index = text.lastIndexOf('\n', document.getLength() - 2);
        if (index != -1)
          position = Math.min(position, index + 20);
        _textArea.setCaretPosition(position);
      } catch (BadLocationException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }

    @Override
    public void removeUpdate(DocumentEvent e) {

    }
  }
  
  private static class KeyPressHandler extends KeyAdapter {
    
    private AgiClientScriptImpl _script;

    public KeyPressHandler(AgiClientScriptImpl script) {
      _script = script;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
      _script.pushChar(e.getKeyChar());
    }
  }
}
