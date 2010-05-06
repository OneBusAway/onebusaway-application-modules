package org.onebusaway.container.spring;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.AbstractFactoryBean;

public class PathFactoryBean extends AbstractFactoryBean {

  private List<Object> _segments = new ArrayList<Object>();

  public void setPath(List<Object> segments) {
    _segments = segments;
  }

  @Override
  public Class<?> getObjectType() {
    return File.class;
  }

  @Override
  protected Object createInstance() throws Exception {
    StringBuilder b = new StringBuilder();
    boolean first = true;
    for (Object segment : _segments) {
      if (!first)
        b.append(File.separatorChar);
      b.append(getSegmentAsString(segment));
      first = false;
    }
    return new File(b.toString());
  }

  private String getSegmentAsString(Object segment) {
    if (segment instanceof File) {
      return ((File) segment).getAbsolutePath();
    }
    return segment.toString();
  }

}
