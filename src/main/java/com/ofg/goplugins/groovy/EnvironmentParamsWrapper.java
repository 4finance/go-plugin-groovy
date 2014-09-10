package com.ofg.goplugins.groovy;

import java.util.Map;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyObjectSupport;

public class EnvironmentParamsWrapper extends GroovyObjectSupport {

    public EnvironmentParamsWrapper(final Map<String, String> params) {
        setMetaClass(new DelegatingMetaClass(getMetaClass()) {

            @Override
            public Object getProperty(Object object, String property) {
                if (params.containsKey(property)) {
                    return params.get(property);
                }

                return super.getProperty(object, property);
            }
        });
    }
}
