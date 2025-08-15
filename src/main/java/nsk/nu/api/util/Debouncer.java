package nsk.nu.api.util;

/** Debounce: trigger only if idle for given millis. */
public final class Debouncer {
    private long lastTrigger = Long.MIN_VALUE;
    private final long quietMillis;
    public Debouncer(long quietMillis){ this.quietMillis = quietMillis; }
    public boolean shouldFire(long nowMillis){
        if (nowMillis - lastTrigger >= quietMillis){ lastTrigger = nowMillis; return true; }
        return false;
    }
}