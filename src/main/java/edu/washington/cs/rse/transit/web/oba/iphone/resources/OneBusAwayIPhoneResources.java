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
package edu.washington.cs.rse.transit.web.oba.iphone.resources;

import edu.washington.cs.rse.transit.web.oba.gwt.resources.CssDataResource;
import edu.washington.cs.rse.transit.web.oba.gwt.resources.DataResource;
import edu.washington.cs.rse.transit.web.oba.gwt.resources.ImmutableResourceBundle;

public interface OneBusAwayIPhoneResources extends ImmutableResourceBundle {
    
    @Resource("arrow.gif")
    public DataResource getImageArrow();
    
    @Resource("arrow-bus.png")
    public DataResource getImageArrowBus();
    
    @Resource("bar.png")
    public DataResource getImageBar();
    
    @Resource("bg.png")
    public DataResource getImageBackground();
    
    @Resource("bgbot.png")
    public DataResource getImageBackgroundBot();
    
    @Resource("bgbot2.png")
    public DataResource getImageBackgroundBot2();
    
    @Resource("busicon.png")
    public DataResource getImageBusIcon();
    
    @Resource("hborder.png")
    public DataResource getImageHBorder();
    
    @Resource("StopIdentification.jpg")
    public DataResource getImageStopIdentification();
    
    @Resource("OneBusAway.css")
    public CssDataResource getCss();
}
