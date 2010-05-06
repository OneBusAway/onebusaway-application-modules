package org.onebusaway.gtfs.csv.schema;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BeanWrapperFactoryTest {

  @Test
  public void test() {
    AB ab = new AB();
    ab.setA("a");
    ab.setB("b");

    BeanWrapper wrapper = BeanWrapperFactory.wrap(ab);
    assertEquals(ab, wrapper.getWrappedInstance(AB.class));

    assertEquals("a", wrapper.getPropertyValue("a"));
    assertEquals("b", wrapper.getPropertyValue("b"));
    
    ab.setA("c");
    ab.setB("d");
    
    assertEquals("c", wrapper.getPropertyValue("a"));
    assertEquals("d", wrapper.getPropertyValue("b"));
    
    wrapper.setPropertyValue("a", "e");
    wrapper.setPropertyValue("b", "f");
    
    assertEquals("e",ab.getA());
    assertEquals("f",ab.getB());
    
    assertEquals("e", wrapper.getPropertyValue("a"));
    assertEquals("f", wrapper.getPropertyValue("b"));
  }

  private static class AB {

    private String a;

    private String b;

    public String getA() {
      return a;
    }

    public void setA(String a) {
      this.a = a;
    }

    public String getB() {
      return b;
    }

    public void setB(String b) {
      this.b = b;
    }

  }
}
