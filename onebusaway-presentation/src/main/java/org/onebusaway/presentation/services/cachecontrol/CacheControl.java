package org.onebusaway.presentation.services.cachecontrol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
public @interface CacheControl {

  public boolean shortCircuit() default true;

  public boolean isPublic() default false;

  public boolean isPrivate() default false;

  public boolean noCache() default false;

  public boolean noStore() default false;

  public boolean noTransform() default false;

  public boolean mustRevalidate() default false;

  public boolean proxyRevalidate() default false;

  public int maxAge() default -1;

  public int sharedMaxAge() default -1;

  public long expiresOffset() default 0L;

  public String etagMethod() default "";

  public String lastModifiedMethod() default "";

  public String expiresMethod() default "";

}
