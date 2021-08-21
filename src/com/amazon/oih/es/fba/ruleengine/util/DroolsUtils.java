package com.amazon.oih.es.fba.ruleengine.util;

import java.util.Objects;

/**
 * This class is used for setting breakpoint when debugging .drl
 */
public class DroolsUtils {
    private DroolsUtils() {}
    public static boolean debugLhsValue(Object value) {
        Objects.nonNull(value);
        return true;
    }
}

