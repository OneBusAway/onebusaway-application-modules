package org.onebusaway.webapp.resources;

import org.onebusaway.presentation.services.resources.WebappSource;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface WhereStandardResources extends ClientBundle {

  @WebappSource("/WEB-INF/css/where-standard.css")
  public CssResource getCss();
  
  //@WebappSource("/WEB-INF/css/where-standard-stop.css")
  //public CssResource getStopCss();
  
  @WebappSource("/WEB-INF/css/where-standard-trip.css")
  public CssResource getTripCss();
  
  @WebappSource("/WEB-INF/css/where-standard-schedule.css")
  public CssResource getScheduleCss();

  @WebappSource("/images/Snow.png")
  public ImageResource getSnowImage();
  
  @WebappSource("/images/index/Tool-Android.png")
  public ImageResource getToolAndroid();
  
  @WebappSource("/images/index/Tool-iPhone.png")
  public ImageResource getToolIPhone();
  
  @WebappSource("/images/index/Tool-MobileWeb.png")
  public ImageResource getToolMobileWeb();
  
  @WebappSource("/images/index/Tool-Phone.png")
  public ImageResource getToolPhone();
  
  @WebappSource("/images/index/Tool-SMS.png")
  public ImageResource getToolSMS();
  
  @WebappSource("/images/index/Tool-Web.png")
  public ImageResource getToolWeb();
  
  @WebappSource("/images/index/Tool-Explore.png")
  public ImageResource getToolExplore();
  
  @WebappSource("/images/index/Logo.cache.png")
  public ImageResource getLogoImage();
  
  @WebappSource("/images/Feedback.png")
  public ImageResource getFeedback();
}
