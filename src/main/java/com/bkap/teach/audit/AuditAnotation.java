package com.bkap.teach.audit;

import com.bkap.teach.enums.Action;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditAnotation {

    Action action();

    String objectType() default "";
}
