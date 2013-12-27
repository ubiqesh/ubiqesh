package io.ubiqesh.edge.vertx.rpc;

import java.lang.annotation.*;

@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.PARAMETER)
public @interface Param {
    String value();

    String defaultValue() default "";
}