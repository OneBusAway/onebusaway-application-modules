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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WizardController {

  public enum ECompletionState {
    NOT_COMPLETED, FINISHED, CANCELLED
  }

  public static final String PROPERTY_CURRENT_PANEL = "currentPanel";

  public static final String PROPERTY_BACK_BUTTON_ENABLED = "backButtonEnabled";

  public static final String PROPERTY_NEXT_BUTTON_ENABLED = "nextButtonEnabled";

  public static final Object FINISH_PANEL_ID = new Object();

  private Set<WizardCompletionListener> _completionListeners = new HashSet<WizardCompletionListener>();

  private PropertyChangeSupport _changes = new PropertyChangeSupport(this);

  private Map<Object, WizardPanelController> _panelControllersById = new HashMap<Object, WizardPanelController>();

  private WizardPanelController _currentController = null;

  private boolean _backButtonEnabled = false;

  private boolean _nextButtonEnabled = false;

  private ECompletionState _completionState = ECompletionState.NOT_COMPLETED;

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    _changes.addPropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName,
      PropertyChangeListener listener) {
    _changes.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    _changes.removePropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(String propertyName,
      PropertyChangeListener listener) {
    _changes.removePropertyChangeListener(propertyName, listener);
  }

  public void addCompletionListener(WizardCompletionListener listener) {
    _completionListeners.add(listener);
  }

  public void removeCompletionListener(WizardCompletionListener listener) {
    _completionListeners.remove(listener);
  }

  public void addPanel(Object panelId, WizardPanelController panelController) {
    _panelControllersById.put(panelId, panelController);
  }

  public void setCurrentPanel(Object wizardPanelControllerId) {
    WizardPanelController controller = _panelControllersById.get(wizardPanelControllerId);
    if (controller == null)
      throw new IllegalStateException("unknown WizardPanelController id: "
          + wizardPanelControllerId);
    if (controller == _currentController)
      return;
    WizardPanelController oldController = _currentController;
    if (oldController != null) {
      oldController.willSetVisible(this, false);
    }
    if (controller != null) {
      controller.willSetVisible(this, true);
    }
    _currentController = controller;
    _changes.firePropertyChange(PROPERTY_CURRENT_PANEL, oldController,
        _currentController);
    if (oldController != null) {
      oldController.didSetVisible(this, false);
    }
    if (_currentController != null) {
      setBackButtonEnabled(_currentController.getBackPanelId() != null);
      setNextButtonEnabled(_currentController.getNextPanelId() != null);
      _currentController.didSetVisible(this, true);
    }
  }

  public WizardPanelController getCurrentController() {
    return _currentController;
  }

  public boolean isBackButtonEnabled() {
    return _backButtonEnabled;
  }

  public void setBackButtonEnabled(boolean backButtonEnabled) {
    if (_backButtonEnabled == backButtonEnabled)
      return;
    _backButtonEnabled = backButtonEnabled;
    _changes.firePropertyChange("backButtonEnabled", !_backButtonEnabled,
        _backButtonEnabled);
  }

  public boolean isNextButtonEnabled() {
    return _nextButtonEnabled;
  }

  public void setNextButtonEnabled(boolean nextButtonEnabled) {
    if (_nextButtonEnabled == nextButtonEnabled)
      return;
    _nextButtonEnabled = nextButtonEnabled;
    _changes.firePropertyChange("nextButtonEnabled", !_nextButtonEnabled,
        _nextButtonEnabled);
  }

  public ECompletionState getCompletionState() {
    return _completionState;
  }

  public void handleBackButton() {
    if (_currentController == null)
      return;
    Object backId = _currentController.getBackPanelId();
    if (backId == null)
      return;
    setCurrentPanel(backId);
  }

  public void handleNextButton() {
    if (_currentController == null)
      return;
    Object nextId = _currentController.getNextPanelId();
    if (nextId == null)
      return;
    if (nextId == FINISH_PANEL_ID) {
      _completionState = ECompletionState.FINISHED;
      for (WizardCompletionListener listener : _completionListeners) {
        listener.handleFinished();
      }
    } else {
      setCurrentPanel(nextId);
    }
  }

  public void handleCancelButton() {
    _completionState = ECompletionState.CANCELLED;
    for (WizardCompletionListener listener : _completionListeners) {
      listener.handleCanceled();
    }
  }

}
