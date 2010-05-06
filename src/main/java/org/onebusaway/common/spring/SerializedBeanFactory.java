package org.onebusaway.common.spring;


import org.onebusaway.common.impl.ObjectSerializationLibrary;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.File;

public class SerializedBeanFactory extends AbstractFactoryBean {

  private File _file;

  public void setFile(File file) {
    _file = file;
  }

  @Override
  public Class<?> getObjectType() {
    return null;
  }

  @Override
  protected Object createInstance() throws Exception {
    return ObjectSerializationLibrary.readObject(_file);
  }
}
