package nsk.nu.ashcore.api.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentifiedApiTest {

    @Test
    void requireValidId_acceptsNonBlankId() {
        // We test positive validation path for provider identifiers.
        assertEquals("namespace:name", Identified.requireValidId("namespace:name", this));
    }

    @Test
    void requireValidId_rejectsNullOrBlank() {
        // We test provider-id validation guards used by ServiceRegistry loading.
        assertThrows(IllegalStateException.class, () -> Identified.requireValidId(null, this));
        assertThrows(IllegalStateException.class, () -> Identified.requireValidId("   ", this));
    }

    @Test
    void requireValidLookupId_validatesArguments() {
        // We test lookup-id validation paths exposed as API utility.
        assertEquals("abc", Identified.requireValidLookupId("abc", "id"));
        assertThrows(NullPointerException.class, () -> Identified.requireValidLookupId("abc", null));
        assertThrows(IllegalArgumentException.class, () -> Identified.requireValidLookupId(" ", "id"));
    }
}

