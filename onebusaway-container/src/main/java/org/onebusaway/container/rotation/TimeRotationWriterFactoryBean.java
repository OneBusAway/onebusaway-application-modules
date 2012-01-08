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
public class TimeRotationWriterFactoryBean extends AbstractFactoryBean<RotationWriter> {

  private String _path;

  public void setPath(String path) {
    _path = path;
  }

  @Override
  public Class<?> getObjectType() {
    return RotationWriter.class;
  }

  @Override
  protected RotationWriter createInstance() throws Exception {
    TimeRotationStrategy strategy = new TimeRotationStrategy(_path);
    return new RotationWriter(strategy);
  }
}
