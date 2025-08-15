package nsk.nu.api.util;

import java.util.function.LongSupplier;

/** Token-bucket rate limiter in calls per window. */
public final class RateLimiter {
    private final double ratePerSec, capacity;
    private double tokens;
    private long lastNanos;
    public RateLimiter(double ratePerSec, double burst, LongSupplier nanoTime){
        if (ratePerSec<=0 || burst<=0) throw new IllegalArgumentException();
        this.ratePerSec = ratePerSec; this.capacity = burst; this.tokens = burst; this.lastNanos = nanoTime.getAsLong();
    }
    public synchronized boolean tryAcquire(LongSupplier nanoTime){
        long now = nanoTime.getAsLong();
        double dt = (now - lastNanos) * 1e-9;
        tokens = Math.min(capacity, tokens + dt * ratePerSec);
        lastNanos = now;
        if (tokens >= 1){ tokens -= 1; return true; }
        return false;
    }
}