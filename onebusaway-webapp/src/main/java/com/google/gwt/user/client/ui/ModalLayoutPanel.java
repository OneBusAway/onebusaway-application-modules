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
package com.google.gwt.user.client.ui;

import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class ModalLayoutPanel extends LayoutPanel implements
    HasCloseHandlers<ModalLayoutPanel> {

  private boolean showing;

  private List<Element> autoHidePartners;

  private HandlerRegistration nativePreviewHandlerRegistration;

  private HandlerRegistration resizeHandlerRegistration;

  public HandlerRegistration addCloseHandler(
      CloseHandler<ModalLayoutPanel> handler) {
    return addHandler(handler, CloseEvent.getType());
  }

  /**
   * Hides the popup and detaches it from the page. This has no effect if it is
   * not currently showing.
   */
  public void hide() {
    hide(false);
  }

  /**
   * Hides the popup and detaches it from the page. This has no effect if it is
   * not currently showing.
   * 
   * @param autoClosed the value that will be passed to
   *          {@link CloseHandler#onClose(CloseEvent)} when the popup is closed
   */
  public void hide(boolean autoClosed) {
    if (!isShowing()) {
      return;
    }
    setState(false);
    CloseEvent.fire(this, this, autoClosed);
  }

  /**
   * Determines whether or not this popup is showing.
   * 
   * @return <code>true</code> if the popup is showing
   * @see #show()
   * @see #hide()
   */
  public boolean isShowing() {
    return showing;
  }

  /**
   * Determines whether or not this popup is visible. Note that this just checks
   * the <code>visibility</code> style attribute, which is set in the
   * {@link #setVisible(boolean)} method. If you want to know if the popup is
   * attached to the page, use {@link #isShowing()} instead.
   * 
   * @return <code>true</code> if the object is visible
   * @see #setVisible(boolean)
   */
  @Override
  public boolean isVisible() {
    return !"hidden".equals(getElement().getStyle().getProperty("visibility"));
  }

  /**
   * Remove an autoHide partner.
   * 
   * @param partner the auto hide partner to remove
   */
  public void removeAutoHidePartner(Element partner) {
    assert partner != null : "partner cannot be null";
    if (autoHidePartners != null) {
      autoHidePartners.remove(partner);
    }
  }

  /**
   * Sets whether this object is visible. This method just sets the
   * <code>visibility</code> style attribute. You need to call {@link #show()}
   * to actually attached/detach the {@link PopupPanel} to the page.
   * 
   * @param visible <code>true</code> to show the object, <code>false</code> to
   *          hide it
   * @see #show()
   * @see #hide()
   */
  @Override
  public void setVisible(boolean visible) {
    // We use visibility here instead of UIObject's default of display
    // Because the panel is absolutely positioned, this will not create
    // "holes" in displayed contents and it allows normal layout passes
    // to occur so the size of the PopupPanel can be reliably determined.
    DOM.setStyleAttribute(getElement(), "visibility", visible ? "visible"
        : "hidden");
  }

  /**
   * Shows the popup and attach it to the page. It must have a child widget
   * before this method is called.
   */
  public void show() {
    if (showing) {
      return;
    }
    setState(true);
  }

  protected void onPreviewNativeEvent(NativePreviewEvent event) {

  }

  @Override
  protected void onUnload() {
    // Just to be sure, we perform cleanup when the popup is unloaded (i.e.
    // removed from the DOM). This is normally taken care of in hide(), but it
    // can be missed if someone removes the popup directly from the RootPanel.
    if (isShowing()) {
      setState(false);
    }
  }

  /**
   * Does the event target one of the partner elements?
   * 
   * @param event the native event
   * @return true if the event targets a partner
   */
  private boolean eventTargetsPartner(NativeEvent event) {
    if (autoHidePartners == null) {
      return false;
    }

    EventTarget target = event.getEventTarget();
    if (Element.is(target)) {
      for (Element elem : autoHidePartners) {
        if (elem.isOrHasChild(Element.as(target))) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Does the event target this popup?
   * 
   * @param event the native event
   * @return true if the event targets the popup
   */
  private boolean eventTargetsPopup(NativeEvent event) {
    EventTarget target = event.getEventTarget();
    if (Element.is(target)) {
      return getElement().isOrHasChild(Element.as(target));
    }
    return false;
  }

  /**
   * Preview the {@link NativePreviewEvent}.
   * 
   * @param event the {@link NativePreviewEvent}
   */
  private void previewNativeEvent(NativePreviewEvent event) {
    // If the event has been canceled or consumed, ignore it
    if (event.isCanceled() || event.isConsumed()) {
      // We need to ensure that we cancel the event even if its been consumed so
      // that popups lower on the stack do not auto hide
      event.cancel();
      return;
    }

    // Fire the event hook and return if the event is canceled
    onPreviewNativeEvent(event);
    if (event.isCanceled()) {
      return;
    }

    // If the event targets the popup or the partner, consume it
    Event nativeEvent = Event.as(event.getNativeEvent());
    boolean eventTargetsPopupOrPartner = eventTargetsPopup(nativeEvent)
        || eventTargetsPartner(nativeEvent);
    if (eventTargetsPopupOrPartner) {
      event.consume();
    }

    // Cancel the event if it doesn't target the modal popup. Note that the
    // event can be both canceled and consumed.
    event.cancel();
  }

  /**
   * Set the showing state of the popup.
   * 
   * @param showing the new state
   */
  private void setState(boolean showing) {
    this.showing = showing;

    // Create or remove the native preview handler
    if (showing) {

      RootPanel.get().add(this);
      getLayout().onAttach();
      getLayout().fillParent();
      
      nativePreviewHandlerRegistration = Event.addNativePreviewHandler(new NativePreviewHandler() {
        public void onPreviewNativeEvent(NativePreviewEvent event) {
          previewNativeEvent(event);
        }
      });

      resizeHandlerRegistration = Window.addResizeHandler(new ResizeHandler() {
        public void onResize(ResizeEvent event) {
          ModalLayoutPanel.this.onResize();
        }
      });

    } else if (nativePreviewHandlerRegistration != null) {
      nativePreviewHandlerRegistration.removeHandler();
      nativePreviewHandlerRegistration = null;
      resizeHandlerRegistration.removeHandler();
      resizeHandlerRegistration = null;
      RootPanel.get().remove(this);
    }
  }
}
