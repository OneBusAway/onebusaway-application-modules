package org.onebusaway.container.cache;

import java.lang.reflect.Method;
import java.util.Collections;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.aop.framework.ReflectiveMethodInvocation;

public class ProceedingJoinPointFactory {

  public static ProceedingJoinPoint create(Object proxy, Object target,
      Class<?> targetClass, Method method, Object... arguments) {
    return new MethodInvocationProceedingJoinPoint(new Blah(proxy, target,
        method, arguments, targetClass));
  }

  private static class Blah extends ReflectiveMethodInvocation {

    protected Blah(Object proxy, Object target, Method method,
        Object[] arguments, Class<?> targetClass) {
      super(proxy, target, method, arguments, targetClass,
          Collections.emptyList());
    }
  }
}
