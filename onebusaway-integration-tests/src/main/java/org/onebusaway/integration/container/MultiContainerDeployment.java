package org.onebusaway.integration.container;

import java.io.IOException;
import java.net.URL;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.installer.Installer;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.util.log.SimpleLogger;

public class MultiContainerDeployment {

  public static void main(String[] args) throws IOException, InterruptedException {
    
    // (1) Optional step to install the container from a URL pointing to its
    // distribution
    System.out.println("install");
    Installer installer = new ZipURLInstaller(new URL(
        "http://developer.onebusaway.org/dist/apache-tomcat-5.5.29.zip"),"/tmp/cargo-installs");
    installer.install();
    System.out.println("install complete");
    
    // (2) Create the Cargo Container instance wrapping our physical container
    System.out.println("configuration");
    DefaultConfigurationFactory configurationFactory = new DefaultConfigurationFactory();
    LocalConfiguration configuration = (LocalConfiguration) configurationFactory.createConfiguration(
        "tomcat5x", ContainerType.INSTALLED, ConfigurationType.STANDALONE);
    System.out.println("configuration complete");

    System.out.println("container");
    DefaultContainerFactory containerFactory = new DefaultContainerFactory();
    InstalledLocalContainer container = (InstalledLocalContainer) containerFactory.createContainer(
        "tomcat5x", ContainerType.INSTALLED, configuration);
    container.setHome(installer.getHome());
    container.setOutput("/tmp/tomcat.log");
    System.out.println("container complete");
    container.setLogger(new SimpleLogger());
    
    // (3) Statically deploy some WAR (optional)
    configuration.addDeployable(new WAR("/Users/bdferris/oba/onebusaway-application-modules/onebusaway-federations-webapp/target/onebusaway-federations-webapp.war"));

    // (4) Start the container
    System.out.println("container start");
    container.start();
    System.out.println("container start complete");
  }
}
