/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
