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
package org.onebusaway.webapp.gwt.oba_application.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface OneBusAwayStandardResources extends ClientBundle {

  public static OneBusAwayStandardResources INSTANCE = GWT.create(OneBusAwayStandardResources.class);

  @Source("Marker.png")
  public ImageResource getMarker();

  @Source("MarkerA.png")
  public ImageResource getMarkerA();

  @Source("MarkerB.png")
  public ImageResource getMarkerB();

  @Source("MarkerC.png")
  public ImageResource getMarkerC();

  @Source("MarkerD.png")
  public ImageResource getMarkerD();

  @Source("MarkerE.png")
  public ImageResource getMarkerE();

  @Source("MarkerF.png")
  public ImageResource getMarkerF();

  @Source("MarkerG.png")
  public ImageResource getMarkerG();

  @Source("MarkerH.png")
  public ImageResource getMarkerH();

  @Source("MarkerI.png")
  public ImageResource getMarkerI();

  @Source("MarkerJ.png")
  public ImageResource getMarkerJ();

  @Source("MarkerK.png")
  public ImageResource getMarkerK();

  @Source("MarkerL.png")
  public ImageResource getMarkerL();

  @Source("MarkerM.png")
  public ImageResource getMarkerM();

  @Source("MarkerN.png")
  public ImageResource getMarkerN();

  @Source("MarkerO.png")
  public ImageResource getMarkerO();

  @Source("MarkerP.png")
  public ImageResource getMarkerP();

  @Source("MarkerQ.png")
  public ImageResource getMarkerQ();

  @Source("MarkerR.png")
  public ImageResource getMarkerR();

  @Source("MarkerS.png")
  public ImageResource getMarkerS();

  @Source("MarkerT.png")
  public ImageResource getMarkerT();

  @Source("MarkerU.png")
  public ImageResource getMarkerU();

  @Source("MarkerV.png")
  public ImageResource getMarkerV();

  @Source("MarkerW.png")
  public ImageResource getMarkerW();

  @Source("MarkerX.png")
  public ImageResource getMarkerX();

  @Source("MarkerY.png")
  public ImageResource getMarkerY();

  @Source("MarkerZ.png")
  public ImageResource getMarkerZ();

  @Source("Star-15.png")
  public ImageResource getStar15();

  @Source("Star-20.png")
  public ImageResource getStar20();

  @Source("Star-30.png")
  public ImageResource getStar30();

  @Source("TriangleUp.png")
  public ImageResource getTriangleUp();

  @Source("TriangleDown.png")
  public ImageResource getTriangleDown();

  @Source("OneBusAway.css")
  public OneBusAwayCssResource getCss();
}
