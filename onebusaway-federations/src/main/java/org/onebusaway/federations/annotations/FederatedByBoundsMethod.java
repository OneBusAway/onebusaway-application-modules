package org.onebusaway.federations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface FederatedByBoundsMethod {
  public int lat1Argument() default 0;
  public int lon1Argument() default 1;
  public int lat2Argument() default 2;
  public int lon2Argument() default 3;
}
