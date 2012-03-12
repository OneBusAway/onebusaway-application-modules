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
package org.onebusaway.quickstart.bootstrap.gui.widgets;

import java.beans.PropertyChangeListener;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jdesktop.beansbinding.AutoBinding;

/**
 * Custom {@link JTextField} that generates {@link PropertyChangeListener}
 * "text" property events when the underlying {@link Document} changes. This
 * allows bi-directional widget-model binding using {@link AutoBinding}.
 * 
 * @author bdferris
 * 
 */
public class JCustomTextField extends JTextField {

  private static final long serialVersionUID = 1L;

  private DocumentChangeHandler _handler = new DocumentChangeHandler();

  public void addTextPropertyChangeEvent() {
    getDocument().addDocumentListener(_handler);
  }

  public void removeTextPropertyChangeEvent() {
    getDocument().removeDocumentListener(_handler);
  }

  private class DocumentChangeHandler implements DocumentListener {

    @Override
    public void insertUpdate(DocumentEvent e) {
      firePropertyChange(e.getDocument());
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      firePropertyChange(e.getDocument());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      firePropertyChange(e.getDocument());
    }

    private void firePropertyChange(Document document) {
      try {
        String text = document.getText(0, document.getLength());
        String prev = text.isEmpty() ? "x" : "";
        JCustomTextField.this.firePropertyChange("text", prev, text);
      } catch (BadLocationException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
