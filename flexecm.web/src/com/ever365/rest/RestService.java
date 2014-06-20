package com.ever365.rest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RestService {
	String method();
	String uri();
	boolean runAsAdmin() default false;
	boolean multipart() default false;
	boolean authenticated() default true;
	boolean webcontext() default false;
}
