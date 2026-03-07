package com.auto.constant;

import java.util.HashSet;
import java.util.Set;

public class FeatureConstant {
    public static final Set<String>FEATURES = new HashSet<>();
    public static final Set<String>DIRECTION = new HashSet<>();
    static {
        FEATURES.add("D");
        FEATURES.add("CE");
        FEATURES.add("DE");
        FEATURES.add("LCC");
        FEATURES.add("EXTD");
        FEATURES.add("ACCD");
        FEATURES.add("NM");
        FEATURES.add("COREDJ");
        FEATURES.add("COREDC");
        FEATURES.add("COREDP");
        FEATURES.add("COREDPA");
        DIRECTION.add("i");
        DIRECTION.add("o");
    }
}
