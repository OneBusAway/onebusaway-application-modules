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
