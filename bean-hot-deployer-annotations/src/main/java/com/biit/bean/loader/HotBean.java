package com.biit.bean.loader;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Any class containing this annotation is susceptible of being deployed as a
 * bean using this library.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HotBean {

}
