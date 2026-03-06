package nsk.nu.ashcore.api.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Type-safe registry backed by {@link ServiceLoader}.
 *
 * <p>The registry is immutable after construction. Providers are loaded eagerly and
 * indexed by {@link Identified#id()}.</p>
 *
 * @param <T> SPI type that also implements {@link Identified}
 */
public final class ServiceRegistry<T extends Identified> {

    private final Map<String, T> byId;

    private ServiceRegistry(Class<T> type, ClassLoader loader) {
        if (type == null) throw new NullPointerException("type");
        if (loader == null) throw new NullPointerException("loader");

        LinkedHashMap<String, T> mutable = new LinkedHashMap<>();
        ServiceLoader.load(type, loader).forEach(impl -> {
            String id = Identified.requireValidId(impl.id(), impl);
            T prev = mutable.putIfAbsent(id, impl);
            if (prev != null) {
                throw new IllegalStateException(
                        "Duplicate service id '" + id + "': "
                                + prev.getClass().getName()
                                + " and "
                                + impl.getClass().getName()
                );
            }
        });

        this.byId = Map.copyOf(mutable);
    }

    /**
     * Creates a registry using the current thread context class loader.
     *
     * @param type SPI type to load
     * @param <T> SPI generic type
     * @return immutable service registry
     */
    public static <T extends Identified> ServiceRegistry<T> of(Class<T> type) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) loader = type.getClassLoader();
        if (loader == null) loader = ClassLoader.getSystemClassLoader();
        return new ServiceRegistry<>(type, loader);
    }

    /**
     * Creates a registry using an explicit class loader.
     *
     * @param type SPI type to load
     * @param loader class loader used by ServiceLoader
     * @param <T> SPI generic type
     * @return immutable service registry
     */
    public static <T extends Identified> ServiceRegistry<T> of(Class<T> type, ClassLoader loader) {
        return new ServiceRegistry<>(type, loader);
    }

    /**
     * Looks up a provider by identifier.
     *
     * @param id provider identifier
     * @return provider wrapped in Optional, or empty if not found
     */
    public Optional<T> get(String id) {
        return Optional.ofNullable(byId.get(Identified.requireValidLookupId(id, "id")));
    }

    /**
     * Looks up a provider by identifier and fails if not found.
     *
     * @param id provider identifier
     * @return provider instance
     * @throws IllegalArgumentException if id is null/blank
     * @throws IllegalStateException if provider is not registered
     */
    public T require(String id) {
        String validId = Identified.requireValidLookupId(id, "id");
        T value = byId.get(validId);
        if (value == null) {
            throw new IllegalStateException("No service registered for id: " + validId);
        }
        return value;
    }

    /**
     * Checks whether a provider with the given identifier exists.
     *
     * @param id provider identifier
     * @return true if provider is registered
     */
    public boolean contains(String id) {
        return byId.containsKey(Identified.requireValidLookupId(id, "id"));
    }

    /**
     * Returns all loaded provider identifiers.
     *
     * @return immutable set of ids
     */
    public Set<String> ids() {
        return Collections.unmodifiableSet(byId.keySet());
    }

    /**
     * Returns all loaded providers.
     *
     * @return immutable collection of providers
     */
    public Collection<T> all() {
        return Collections.unmodifiableCollection(byId.values());
    }

    /**
     * Returns number of loaded providers.
     *
     * @return provider count
     */
    public int size() {
        return byId.size();
    }

    /**
     * Returns true when no providers are loaded.
     *
     * @return true if registry is empty
     */
    public boolean isEmpty() {
        return byId.isEmpty();
    }
}