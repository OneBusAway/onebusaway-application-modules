/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.where.web.common.client.widgets;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClickListenerCollection;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MouseListenerCollection;
import com.google.gwt.user.client.ui.MouseWheelListener;
import com.google.gwt.user.client.ui.MouseWheelListenerCollection;
import com.google.gwt.user.client.ui.SourcesClickEvents;
import com.google.gwt.user.client.ui.SourcesMouseEvents;
import com.google.gwt.user.client.ui.SourcesMouseWheelEvents;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractElementPanel extends ComplexPanel implements SourcesClickEvents, SourcesMouseEvents,
        SourcesMouseWheelEvents {

    private ClickListenerCollection clickListeners;

    private MouseListenerCollection mouseListeners;

    private MouseWheelListenerCollection mouseWheelListeners;

    /**
     * Creates an empty flow panel.
     */
    public AbstractElementPanel() {
        setElement(createElement());
    }

    /**
     * Adds a new child widget to the panel.
     * 
     * @param w
     *            the widget to be added
     */
    @Override
    public void add(Widget w) {
        super.add(w, getElement());
    }

    /**
     * Inserts a widget before the specified index.
     * 
     * @param w
     *            the widget to be inserted
     * @param beforeIndex
     *            the index before which it will be inserted
     * @throws IndexOutOfBoundsException
     *             if <code>beforeIndex</code> is out of range
     */
    public void insert(Widget w, int beforeIndex) {
        super.insert(w, getElement(), beforeIndex, true);
    }

    protected abstract Element createElement();

    public void addClickListener(ClickListener listener) {
        if (clickListeners == null) {
            clickListeners = new ClickListenerCollection();
            sinkEvents(Event.ONCLICK);
        }
        clickListeners.add(listener);
    }

    public void addMouseListener(MouseListener listener) {
        if (mouseListeners == null) {
            mouseListeners = new MouseListenerCollection();
            sinkEvents(Event.MOUSEEVENTS);
        }
        mouseListeners.add(listener);
    }

    public void addMouseWheelListener(MouseWheelListener listener) {
        if (mouseWheelListeners == null) {
            mouseWheelListeners = new MouseWheelListenerCollection();
            sinkEvents(Event.ONMOUSEWHEEL);
        }
        mouseWheelListeners.add(listener);
    }

    @Override
    public void onBrowserEvent(Event event) {
        switch (DOM.eventGetType(event)) {
        case Event.ONCLICK:
            if (clickListeners != null) {
                clickListeners.fireClick(this);
            }
            break;

        case Event.ONMOUSEDOWN:
        case Event.ONMOUSEUP:
        case Event.ONMOUSEMOVE:
        case Event.ONMOUSEOVER:
        case Event.ONMOUSEOUT:
            if (mouseListeners != null) {
                mouseListeners.fireMouseEvent(this, event);
            }
            break;

        case Event.ONMOUSEWHEEL:
            if (mouseWheelListeners != null) {
                mouseWheelListeners.fireMouseWheelEvent(this, event);
            }
            break;
        }
    }

    public void removeClickListener(ClickListener listener) {
        if (clickListeners != null) {
            clickListeners.remove(listener);
            if (clickListeners.isEmpty())
                unsinkEvents(Event.ONCLICK);
        }

    }

    public void clearClickListeners() {
        if (clickListeners != null) {
            clickListeners.clear();
            clickListeners = null;
            unsinkEvents(Event.ONCLICK);
        }
    }

    public void removeMouseListener(MouseListener listener) {
        if (mouseListeners != null) {
            mouseListeners.remove(listener);
        }
    }

    public void removeMouseWheelListener(MouseWheelListener listener) {
        if (mouseWheelListeners != null) {
            mouseWheelListeners.remove(listener);
        }
    }
}
