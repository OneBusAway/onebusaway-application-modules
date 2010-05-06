package org.onebusaway.container.spring;

import org.springframework.beans.factory.FactoryBean;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileFactoryBean implements FactoryBean {

  private String _path;

  public void setPath(String path) {
    _path = path;
  }

  public Object getObject() throws Exception {
    Pattern p = Pattern.compile("\\$\\{([^}]+)\\}");
    Matcher m = p.matcher(_path);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String property = m.group(1);
      String value = System.getProperty(property);
      m.appendReplacement(sb, value);
    }
    m.appendTail(sb);
    return new File(sb.toString());
  }

  public Class<?> getObjectType() {
    return File.class;
  }

  public boolean isSingleton() {
    return true;
  }

}
