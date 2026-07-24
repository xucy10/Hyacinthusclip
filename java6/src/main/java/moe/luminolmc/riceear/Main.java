package moe.luminolmc.riceear;

import java.lang.reflect.Method;

public final class Main {

    public static void main(final String[] args) {
        try {
            final Class<?> riceearClazz = Class.forName("moe.luminolmc.riceear.Riceear");
            final Method mainMethod = riceearClazz.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) args);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}