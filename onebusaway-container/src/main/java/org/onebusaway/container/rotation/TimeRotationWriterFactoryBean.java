package org.onebusaway.container.rotation;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * Spring {@link FactoryBean} to construct a {@link RotationWriter} with a
 * {@link TimeRotationStrategy}. Specify the {@code path} property to set the
 * output format/path for the {@link TimeRotationStrategy}.
 * 
 * @author bdferris
 * 
 */
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
