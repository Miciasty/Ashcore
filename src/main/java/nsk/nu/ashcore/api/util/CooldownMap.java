package nsk.nu.ashcore.api.util;

import java.util.HashMap;
import java.util.Map;

/** Per-key cooldowns. */
public final class CooldownMap<K> {
    private final Map<K, Long> until = new HashMap<>();
    public boolean tryUse(K key, long nowMillis, long cooldownMillis){
        long u = until.getOrDefault(key, 0L);
        if (nowMillis >= u){ until.put(key, nowMillis + cooldownMillis); return true; }
        return false;
    }
}