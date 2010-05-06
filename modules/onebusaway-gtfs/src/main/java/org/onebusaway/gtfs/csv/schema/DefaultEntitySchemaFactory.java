package org.onebusaway.gtfs.csv.schema;

import org.onebusaway.gtfs.csv.schema.beans.CsvEntityMappingBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultEntitySchemaFactory extends AbstractEntitySchemaFactoryImpl {

  private List<BeanDefinitionSource> _sources = new ArrayList<BeanDefinitionSource>();

  public void addBean(CsvEntityMappingBean bean) {
    _sources.add(new CsvEntityMappingBeanSource(bean));
  }

  public void addFactory(ListableCsvMappingFactory factory) {
    _sources.add(new ListableCsvMappingFactorySource(factory));
  }

  /****
   * {@link AbstractEntitySchemaFactoryImpl} Interface
   ****/

  @Override
  protected void processBeanDefinitions() {
    for (BeanDefinitionSource source : _sources)
      source.processBeanDefinitions();
  }

  private interface BeanDefinitionSource {
    public void processBeanDefinitions();
  }

  private class CsvEntityMappingBeanSource implements BeanDefinitionSource {

    private CsvEntityMappingBean _bean;

    public CsvEntityMappingBeanSource(CsvEntityMappingBean bean) {
      _bean = bean;
    }

    public void processBeanDefinitions() {
      registerBeanDefinition(_bean);
    }
  }

  private class ListableCsvMappingFactorySource implements BeanDefinitionSource {

    private ListableCsvMappingFactory _factory;

    public ListableCsvMappingFactorySource(ListableCsvMappingFactory factory) {
      _factory = factory;
    }

    public void processBeanDefinitions() {
      Collection<CsvEntityMappingBean> beans = _factory.getEntityMappings();
      for (CsvEntityMappingBean bean : beans)
        registerBeanDefinition(bean);
    }
  }

}
