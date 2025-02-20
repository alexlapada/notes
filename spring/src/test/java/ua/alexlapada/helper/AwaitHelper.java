package ua.alexlapada.helper;

import org.testcontainers.shaded.org.awaitility.Awaitility;

public class AwaitHelper {

    public static void awaitUntil(Runnable runnable) {
        Awaitility.await().until(() -> {
            try {
                runnable.run();
                return true;
            } catch (AssertionError e) {
                return false;
            }
        });
    }
}
