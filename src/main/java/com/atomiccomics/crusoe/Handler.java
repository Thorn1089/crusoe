package com.atomiccomics.crusoe;

import com.atomiccomics.crusoe.event.Event;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Handler {

    Class<?> value();

}
