package org.onebusaway.users.impl;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.users.model.User;
import org.onebusaway.users.services.UserDao;

public class UserLastAccessTimeServiceImplTest {

  @Test
  public void test() throws InterruptedException {
    
    User userA = new User();
    userA.setId(1);
    
    User userB = new User();
    userB.setId(2);
    
    UserDao dao = Mockito.mock(UserDao.class);
    Mockito.when(dao.getUserForId(1)).thenReturn(userA);
    Mockito.when(dao.getUserForId(2)).thenReturn(userB);
    
    UserLastAccessTimeServiceImpl service = new UserLastAccessTimeServiceImpl();
    service.setEvictionFrequency(2000);
    service.setEvictionThreshold(5000);
    service.setUserDao(dao);
        
    service.start();
    
    assertEquals(0,service.getNumberOfActiveUsers());
    
    long t1 = System.currentTimeMillis();
    service.handleAccessForUser(1, t1);
    
    Mockito.verify(dao).getUserForId(1);
    Mockito.verify(dao).saveOrUpdateUser(userA);
    Mockito.verifyNoMoreInteractions(dao);
    
    assertEquals(new Date(t1),userA.getLastAccessTime());
    assertEquals(1,service.getNumberOfActiveUsers());
    
    Thread.sleep(3000);
    
    long t2 = System.currentTimeMillis();
    service.handleAccessForUser(2, t2);
    
    Mockito.verify(dao).getUserForId(2);
    Mockito.verify(dao).saveOrUpdateUser(userB);
    Mockito.verifyNoMoreInteractions(dao);
    
    assertEquals(new Date(t2),userB.getLastAccessTime());
    assertEquals(2,service.getNumberOfActiveUsers());
    
    Thread.sleep(3000);
    
    long t3 = System.currentTimeMillis();
    service.handleAccessForUser(2, t3);

    assertEquals(new Date(t2),userB.getLastAccessTime());
    assertEquals(1,service.getNumberOfActiveUsers());
    
    Thread.sleep(3000);
    
    assertEquals(new Date(t2),userB.getLastAccessTime());
    assertEquals(1,service.getNumberOfActiveUsers());
    
    Thread.sleep(3000);
    
    assertEquals(0,service.getNumberOfActiveUsers());
    
    service.stop();
  }
}
