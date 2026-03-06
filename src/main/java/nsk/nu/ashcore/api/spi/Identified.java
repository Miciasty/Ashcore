package nsk.nu.ashcore.api.spi;

import java.util.Objects;

/**
 * Contract for pluggable implementations addressable by a stable identifier.
 *
 * <p>Identifier requirements:
 * <ul>
 *   <li>Must be non-null and non-blank.</li>
 *   <li>Must be unique within one SPI interface group.</li>
 *   <li>Should remain stable across releases.</li>
 * </ul>
 *
 * <p>Recommended format: {@code namespace:name} (for example {@code random:splitmix64}).</p>
 */
public interface Identified {

    /**
     * Returns the stable identifier of this implementation.
     *
     * @return non-null, non-blank identifier
     */
    String id();

    /**
     * Validates that an identifier is non-null and non-blank.
     *
     * @param id identifier to validate
     * @param owner object used only for error context
     * @return validated identifier
     * @throws IllegalStateException if id is null or blank
     */
    static String requireValidId(String id, Object owner) {
        if (id == null || id.isBlank()) {
            String ownerName = owner == null ? "unknown" : owner.getClass().getName();
            throw new IllegalStateException("Service id must be non-empty: " + ownerName);
        }
        return id;
    }

    /**
     * Validates that an identifier string passed by caller is non-null and non-blank.
     *
     * @param id identifier to validate
     * @param argName parameter name for error context
     * @return validated identifier
     * @throws IllegalArgumentException if id is null or blank
     */
    static String requireValidLookupId(String id, String argName) {
        Objects.requireNonNull(argName, "argName");
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(argName + " must be non-null and non-blank");
        }
        return id;
    }
}