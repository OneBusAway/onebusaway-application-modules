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
package org.onebusaway.oba.web.standard.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.libideas.resources.client.CssResource;
import com.google.gwt.libideas.resources.client.DataResource;
import com.google.gwt.libideas.resources.client.ImmutableResourceBundle;

public interface OneBusAwayStandardResources extends ImmutableResourceBundle {

  public static OneBusAwayStandardResources INSTANCE = GWT.create(OneBusAwayStandardResources.class);

  @Resource("Marker.png")
  public DataResource getMarker();

  @Resource("MarkerA.png")
  public DataResource getMarkerA();

  @Resource("MarkerB.png")
  public DataResource getMarkerB();

  @Resource("MarkerC.png")
  public DataResource getMarkerC();

  @Resource("MarkerD.png")
  public DataResource getMarkerD();

  @Resource("MarkerE.png")
  public DataResource getMarkerE();

  @Resource("MarkerF.png")
  public DataResource getMarkerF();

  @Resource("MarkerG.png")
  public DataResource getMarkerG();

  @Resource("MarkerH.png")
  public DataResource getMarkerH();

  @Resource("MarkerI.png")
  public DataResource getMarkerI();

  @Resource("MarkerJ.png")
  public DataResource getMarkerJ();

  @Resource("MarkerK.png")
  public DataResource getMarkerK();

  @Resource("MarkerL.png")
  public DataResource getMarkerL();

  @Resource("MarkerM.png")
  public DataResource getMarkerM();

  @Resource("MarkerN.png")
  public DataResource getMarkerN();

  @Resource("MarkerO.png")
  public DataResource getMarkerO();

  @Resource("MarkerP.png")
  public DataResource getMarkerP();

  @Resource("MarkerQ.png")
  public DataResource getMarkerQ();

  @Resource("MarkerR.png")
  public DataResource getMarkerR();

  @Resource("MarkerS.png")
  public DataResource getMarkerS();

  @Resource("MarkerT.png")
  public DataResource getMarkerT();

  @Resource("MarkerU.png")
  public DataResource getMarkerU();

  @Resource("MarkerV.png")
  public DataResource getMarkerV();

  @Resource("MarkerW.png")
  public DataResource getMarkerW();

  @Resource("MarkerX.png")
  public DataResource getMarkerX();

  @Resource("MarkerY.png")
  public DataResource getMarkerY();

  @Resource("MarkerZ.png")
  public DataResource getMarkerZ();
  
  @Resource("Star-15.png")
  public DataResource getStar15();
  
  @Resource("Star-20.png")
  public DataResource getStar20();
  
  @Resource("Star-30.png")
  public DataResource getStar30();
  
  @Resource("TriangleUp.png")
  public DataResource getTriangleUp();
  
  @Resource("TriangleDown.png")
  public DataResource getTriangleDown();

  @Resource("OneBusAway.css")
  public CssResource getCss();
}
