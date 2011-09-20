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
package org.onebusaway.container.cache;

public class MockServiceImpl implements MockService {

  private int _evalauteBeanWithParameterAnnotationCount;

  public int getEvalauteBeanWithParameterAnnotationCount() {
    return _evalauteBeanWithParameterAnnotationCount;
  }

  /****
   * {@link MockService} Interface
   ****/

  @Override
  public String evalaute(String value) {
    return "test";
  }

  @Override
  public String evalauteMultiArg(String valueA, String valueB) {
    return valueA;
  }

  @Override
  public String evalauteBean(MockBean bean) {
    return "test";
  }

  @Override
  @Cacheable(keyFactory = MockCacheableMethodKeyFactory.class)
  public String evalauteBeanWithAnnotation(MockBean bean) {
    return "test";
  }

  @Override
  public String evalauteBeanWithParameterAnnotation(
      @CacheableArgument(keyProperty = "id") MockBean bean,
      @CacheableArgument(cacheRefreshIndicator = true) boolean forceRefresh) {
    _evalauteBeanWithParameterAnnotationCount++;
    return "test";
  }

  public String evalauteBeanWithoutParameterAnnotation(MockBean bean,
      boolean forceRefresh) {
    return "test";
  }

}
