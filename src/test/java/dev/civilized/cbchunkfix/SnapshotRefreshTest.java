package dev.civilized.cbchunkfix;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class SnapshotRefreshTest {
    static int assertions;
    static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        // Reproduce the exact failure shape: an update mutates live map values.
        Map<Integer,String> original = new HashMap<>(Map.of(1,"first",2,"second"));
        boolean failed = false;
        try { original.values().forEach(v -> original.put(3,"new")); }
        catch (ConcurrentModificationException expected) { failed = true; }
        check(failed, "Unpatched live HashMap.values().forEach must reproduce CME");

        Map<Integer,String> map = new HashMap<>(Map.of(1,"first",2,"second"));
        List<String> updated = new ArrayList<>();
        check(SnapshotRefresh.tryUpdate(map::values, v -> true, v -> {updated.add(v);map.put(3,"new");}), "Snapshot refresh failed");
        check(updated.size()==2 && !updated.contains("new"), "Only captured original entries should update");
        check(map.size()==3, "Original callback's map mutation must be preserved");

        // Removed/replaced entries must not be refreshed later in the snapshot.
        Map<Integer,Object> entities = new LinkedHashMap<>();
        Object a=new Object(),b=new Object(),replacement=new Object();entities.put(1,a);entities.put(2,b);
        List<Object> seen=new ArrayList<>();
        check(SnapshotRefresh.tryUpdate(entities::values,
                v -> entities.get(v==a?1:2)==v,
                v -> {seen.add(v);entities.put(2,replacement);}),"Replacement test failed");
        check(seen.equals(List.of(a)),"Stale entity should be skipped");

        AtomicInteger captures=new AtomicInteger(),callbacks=new AtomicInteger();
        Collection<String> unstable=new AbstractCollection<>() {
            public Iterator<String> iterator(){throw new AssertionError("Must not iterate live view");}
            public int size(){return 1;}
            public Object[] toArray(){if(captures.getAndIncrement()==0)throw new ConcurrentModificationException();return new Object[]{"ok"};}
        };
        check(!SnapshotRefresh.tryUpdate(()->unstable,v->true,v->callbacks.incrementAndGet()),"Unstable capture must request deferral");
        check(callbacks.get()==0,"No callbacks may run on failed capture");
        check(SnapshotRefresh.tryUpdate(()->unstable,v->true,v->callbacks.incrementAndGet()),"Later stable capture must recover");
        check(callbacks.get()==1,"Recovery callback count");

        Collection<String> sizeRace=new AbstractCollection<>() {
            public Iterator<String> iterator(){throw new AssertionError();}
            public int size(){return 0;}
            public Object[] toArray(){throw new ArrayIndexOutOfBoundsException();}
        };
        check(!SnapshotRefresh.tryUpdate(()->sizeRace,v->true,v->{}),"toArray size races must defer");
        check(SnapshotRefresh.tryUpdate(()->Arrays.asList(null,"ok"),v->true,v->{}),"Null race entries must be skipped");

        boolean callbackVisible=false;
        try { SnapshotRefresh.tryUpdate(()->List.of("x"),v->true,v->{throw new ConcurrentModificationException("callback");}); }
        catch(ConcurrentModificationException expected){callbackVisible=true;}
        check(callbackVisible,"Unrelated callback failures must not be swallowed");
        check(SnapshotRefresh.tryUpdate(List::of,v->true,v->{throw new AssertionError();}),"Empty chunks must work");
        System.out.println("PASS: "+assertions+" assertions; reproduces original CME, preserves model callbacks, skips stale entities, defers capture races, and exposes unrelated errors.");
    }
}
