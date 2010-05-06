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
package org.onebusaway.webapp.gwt.oba_application.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.CssResource.Strict;

public interface OneBusAwayStandardResources extends ClientBundle {

  public static OneBusAwayStandardResources INSTANCE = GWT.create(OneBusAwayStandardResources.class);

  @Source("Marker.png")
  public DataResource getMarker();

  @Source("MarkerA.png")
  public DataResource getMarkerA();

  @Source("MarkerB.png")
  public DataResource getMarkerB();

  @Source("MarkerC.png")
  public DataResource getMarkerC();

  @Source("MarkerD.png")
  public DataResource getMarkerD();

  @Source("MarkerE.png")
  public DataResource getMarkerE();

  @Source("MarkerF.png")
  public DataResource getMarkerF();

  @Source("MarkerG.png")
  public DataResource getMarkerG();

  @Source("MarkerH.png")
  public DataResource getMarkerH();

  @Source("MarkerI.png")
  public DataResource getMarkerI();

  @Source("MarkerJ.png")
  public DataResource getMarkerJ();

  @Source("MarkerK.png")
  public DataResource getMarkerK();

  @Source("MarkerL.png")
  public DataResource getMarkerL();

  @Source("MarkerM.png")
  public DataResource getMarkerM();

  @Source("MarkerN.png")
  public DataResource getMarkerN();

  @Source("MarkerO.png")
  public DataResource getMarkerO();

  @Source("MarkerP.png")
  public DataResource getMarkerP();

  @Source("MarkerQ.png")
  public DataResource getMarkerQ();

  @Source("MarkerR.png")
  public DataResource getMarkerR();

  @Source("MarkerS.png")
  public DataResource getMarkerS();

  @Source("MarkerT.png")
  public DataResource getMarkerT();

  @Source("MarkerU.png")
  public DataResource getMarkerU();

  @Source("MarkerV.png")
  public DataResource getMarkerV();

  @Source("MarkerW.png")
  public DataResource getMarkerW();

  @Source("MarkerX.png")
  public DataResource getMarkerX();

  @Source("MarkerY.png")
  public DataResource getMarkerY();

  @Source("MarkerZ.png")
  public DataResource getMarkerZ();
  
  @Source("Star-15.png")
  public DataResource getStar15();
  
  @Source("Star-20.png")
  public DataResource getStar20();
  
  @Source("Star-30.png")
  public DataResource getStar30();
  
  @Source("TriangleUp.png")
  public DataResource getTriangleUp();
  
  @Source("TriangleDown.png")
  public DataResource getTriangleDown();

  @Source("OneBusAway.css")
  @Strict
  public CssResource getCss();
}
