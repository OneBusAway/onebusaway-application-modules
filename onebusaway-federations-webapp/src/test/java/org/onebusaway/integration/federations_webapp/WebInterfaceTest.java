package org.onebusaway.integration.federations_webapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class WebInterfaceTest {

  private WebDriver driver;

  @Before
  public void setup() {
    driver = new FirefoxDriver();
  }

  @After
  public void teardown() {
    driver.close();
  }

  @Test
  public void testUntitled() throws Exception {

    int port = Integer.parseInt(System.getProperty(
        "org.onebusaway.federations_webapp.port", "9900"));
    System.out.println("port=" + port);

    driver.get("http://localhost:" + port
        + "/onebusaway-federations-webapp/remoting/index.action");

    WebElement urlElement = driver.findElement(By.name("url"));
    urlElement.sendKeys("http://onebusaway.org/");

    WebElement serviceClassElement = driver.findElement(By.name("serviceClass"));
    serviceClassElement.sendKeys("org.onebusaway.TransitData");

    WebElement submitElement = driver.findElement(By.name("submit"));
    submitElement.click();

    assertEquals("org.onebusaway.TransitData",
        driver.findElement(By.xpath("//table[@id='services']/tbody/tr[2]/td[1]")).getText());
    assertEquals("http://onebusaway.org/",
        driver.findElement(By.xpath("//table[@id='services']/tbody/tr[2]/td[2]/a")).getText());
    assertEquals("true",
        driver.findElement(By.xpath("//table[@id='services']/tbody/tr[2]/td[3]")).getText());

    driver.findElement(By.xpath("//table[@id='services']/tbody/tr[2]/td[5]/a")).click();

    assertEquals("false",
        driver.findElement(By.xpath("//table[@id='services']/tbody/tr[2]/td[3]")).getText());

    driver.findElement(By.xpath("//table[@id='services']/tbody/tr[2]/td[6]/a")).click();

    try {
      WebElement element = driver.findElement(By.xpath("//table[@id='services']/tbody/tr[2]/td[1]"));
      System.out.println(element.getText());
      fail();
    } catch (NoSuchElementException ex) {

    }
  }
}
