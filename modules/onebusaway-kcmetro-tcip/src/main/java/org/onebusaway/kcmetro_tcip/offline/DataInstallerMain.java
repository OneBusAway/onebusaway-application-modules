package org.onebusaway.kcmetro_tcip.offline;

import org.onebusaway.container.ContainerLibrary;

import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;

public class DataInstallerMain {
  
  public static void main(String[] args) throws IOException {
    
    if( args.length != 2) {
      System.err.println("usage: config.xml gtfs_path");
      System.exit(-1);
    }
      
    ApplicationContext context = ContainerLibrary.createContext("file:"+args[0],"classpath:org/onebusaway/kcmetro_tcip/offline/application-context-installer.xml");
    SessionFactory sessionFactory = (SessionFactory) context.getBean("sessionFactory");
    
    DataLoader loader = new DataLoader();
    loader.setSessionFactory(sessionFactory);
    loader.setInputLocation(new File(args[1]));
    loader.run();
  }
}
