package dev.civilized.cbchunkfix;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** No game dependencies, so the invalidated-map regression can be tested directly. */
public final class SnapshotRefresh {
    private SnapshotRefresh() {}

    public static <T> boolean tryUpdate(Supplier<? extends Collection<T>> source,
                                        Predicate<T> stillCurrent, Consumer<T> originalUpdate) {
        final Object[] snapshot;
        try {
            // No model-update callbacks run until capture finishes. In particular, do
            // not use Collection.forEach or a stream on the live HashMap values view.
            snapshot = source.get().toArray();
        } catch (ConcurrentModificationException | ArrayIndexOutOfBoundsException unstableCapture) {
            // A genuinely off-thread map writer can also invalidate toArray's size.
            // Only snapshot capture is guarded; callback failures remain visible.
            return false;
        }
        for (Object entry : snapshot) {
            if (entry == null) continue;
            @SuppressWarnings("unchecked") T value = (T) entry;
            if (stillCurrent.test(value)) originalUpdate.accept(value);
        }
        return true;
    }
}
