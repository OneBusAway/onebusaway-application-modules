package org.onebusaway.container.rotation;

import org.springframework.beans.factory.config.AbstractFactoryBean;

public class TimeRotationWriterFactoryBean extends AbstractFactoryBean {

  private String _path;

  public void setPath(String path) {
    _path = path;
  }

  @Override
  public Class<?> getObjectType() {
    return RotationWriter.class;
  }

  @Override
  protected Object createInstance() throws Exception {
    TimeRotationStrategy strategy = new TimeRotationStrategy(_path);
    return new RotationWriter(strategy);
  }
}
