package com.github.lemniscate.sql.injector;

import java.lang.annotation.*;

/**
 * @Author dave 1/22/15 1:20 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface SqlQuery {


    String value() default "";
    String resourcePath() default "";

}
