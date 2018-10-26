package io.eroshenkoam.sitemap.runnable;

/**
 * @author Artem Eroshenko.
 */
public interface SafeRunnable extends Runnable {

    default void run() {
        try {
            runUnsafe();
        } catch (Throwable e) {
            onError(e);
        }
    }

    void runUnsafe() throws Throwable;

    void onError(Throwable e);

}
