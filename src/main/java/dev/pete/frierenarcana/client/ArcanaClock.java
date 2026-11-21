package dev.pete.frierenarcana.client;

/**
 * The clock every client-side cutscene and effect timeline reads. Normally the real {@link System#nanoTime()}; when
 * the cutscene is being captured frame by frame ({@link CinemaCapture}) it is a virtual clock that advances exactly one
 * film frame per rendered frame, so the captured frames land on the episode's timestamps however slowly the game draws.
 */
public final class ArcanaClock {
    private static boolean virtual;
    private static long offset;
    private static long virtualNow;

    private ArcanaClock() {
    }

    public static long nanos() {
        return virtual ? virtualNow : System.nanoTime();
    }

    static void startVirtual() {
        virtualNow = System.nanoTime();
        offset = 0L;
        virtual = true;
    }

    static void advance(long nanos) {
        virtualNow += nanos;
        offset += nanos;
    }

    static boolean isVirtual() {
        return virtual;
    }
}
