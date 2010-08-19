package org.onebusaway.webapp.actions;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.springframework.transaction.annotation.Transactional;

/* wrap the entire request in a single transaction */
@Transactional
public class TransactionalInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
      return invocation.invoke();
  }

}
