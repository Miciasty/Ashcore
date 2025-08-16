package nsk.nu.ashcore.api.spi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Type-safe registry backed by Java's ServiceLoader. Loads all providers of a given SPI interface
 * and exposes them by their {@link Identified#id()}.
 *
 * <p>Design notes:
 * <ul>
 *   <li>No reflection calls to arbitrary methods; the contract is {@code Identified}.</li>
 *   <li>No global singletons; callers keep their own registry instance.</li>
 * </ul>
 */
public final class ServiceRegistry<T extends Identified> {

    private final Map<String, T> byId = new ConcurrentHashMap<>();

    private ServiceRegistry(Class<T> type) {
        ServiceLoader.load(type).forEach(impl -> byId.put(impl.id(), impl));
    }

    /**
     * Creates a new registry and eagerly loads providers via ServiceLoader.
     */
    public static <T extends Identified> ServiceRegistry<T> of(Class<T> type) {
        return new ServiceRegistry<>(type);
    }

    /**
     * @return provider by id or empty if not registered
     */
    public Optional<T> get(String id) { return Optional.ofNullable(byId.get(id)); }

    /**
     * @return immutable view of all loaded providers
     */
    public Collection<T> all() { return Collections.unmodifiableCollection(byId.values()); }
}