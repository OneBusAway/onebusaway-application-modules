package org.onebusaway.container.cache;

public interface MockService {

  public String evalaute(String value);
  
  public String evalauteMultiArg(String valueA, String valueB);

  public String evalauteBean(MockBean bean);

  public String evalauteBeanWithAnnotation(MockBean bean);

  public String evalauteBeanWithParameterAnnotation(MockBean bean,
      boolean forceRefresh);

  public String evalauteBeanWithoutParameterAnnotation(MockBean bean,
      boolean forceRefresh);
}
