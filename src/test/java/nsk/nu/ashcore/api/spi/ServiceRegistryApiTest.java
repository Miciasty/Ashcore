package nsk.nu.ashcore.api.spi;

import nsk.nu.ashcore.api.spi.testing.DuplicateService;
import nsk.nu.ashcore.api.spi.testing.GoodService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceRegistryApiTest {

    @Test
    void of_withGoodService_loadsProviderAndSupportsLookups() {
        // We test the nominal ServiceLoader path with one unique provider id.
        ServiceRegistry<GoodService> registry = ServiceRegistry.of(GoodService.class, GoodService.class.getClassLoader());

        assertFalse(registry.isEmpty());
        assertEquals(1, registry.size());
        assertTrue(registry.contains("test:good"));
        assertTrue(registry.get("test:good").isPresent());
        assertNotNull(registry.require("test:good"));
        assertEquals(1, registry.ids().size());
        assertEquals(1, registry.all().size());
    }

    @Test
    void of_withDuplicateIds_throwsIllegalStateException() {
        // We test duplicate-id detection during registry bootstrap.
        assertThrows(IllegalStateException.class,
                () -> ServiceRegistry.of(DuplicateService.class, DuplicateService.class.getClassLoader()));
    }

    @Test
    void require_throwsWhenIdNotFound() {
        // We test strict lookup behavior for missing provider ids.
        ServiceRegistry<GoodService> registry = ServiceRegistry.of(GoodService.class, GoodService.class.getClassLoader());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.require("test:missing"));
        assertTrue(ex.getMessage().contains("No service registered for id"));
    }

    @Test
    void lookup_methods_validateBlankOrNullIds() {
        // We test id validation shared by get/contains/require methods.
        ServiceRegistry<GoodService> registry = ServiceRegistry.of(GoodService.class, GoodService.class.getClassLoader());

        assertThrows(IllegalArgumentException.class, () -> registry.get(" "));
        assertThrows(IllegalArgumentException.class, () -> registry.contains(""));
        assertThrows(IllegalArgumentException.class, () -> registry.require(null));
    }

    @Test
    void factory_guards_rejectNullArguments() {
        // We test explicit constructor factory guards for null type/loader.
        assertThrows(NullPointerException.class, () -> ServiceRegistry.of(null));
        assertThrows(NullPointerException.class, () -> ServiceRegistry.of(GoodService.class, null));
    }
}

