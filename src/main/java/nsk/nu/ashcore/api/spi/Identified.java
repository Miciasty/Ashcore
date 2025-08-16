package nsk.nu.ashcore.api.spi;

/**
 * Marker interface for pluggable implementations that can be addressed by a stable identifier.
 * Implementations should return a short, human-readable id (e.g. "grid-raycaster:dda").
 */
public interface Identified {
    /**
     * @return stable identifier of this implementation, unique within its interface group.
     */
    String id();
}