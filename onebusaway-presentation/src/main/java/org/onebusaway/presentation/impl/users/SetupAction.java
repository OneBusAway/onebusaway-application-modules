package org.onebusaway.presentation.impl.users;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marking an action class with this annotation will cause it be pass-through by
 * the {@link IsSetupInterceptor}.
 * 
 * @author bdferris
 * @see IsSetupInterceptor
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface SetupAction {
  boolean onlyAllowIfNotSetup() default false;
}
