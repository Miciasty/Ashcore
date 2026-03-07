package nsk.nu.ashcore.api.hash;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HashApiTest {

    @Test
    void fnv1a_stringAndBytes_areEquivalent() {
        // We test contract consistency between string and byte-array overloads.
        String text = "ashcore-hash-contract";
        long fromString = Hash64.fnv1a(text);
        long fromBytes = Hash64.fnv1a(text.getBytes(StandardCharsets.UTF_8));

        assertEquals(fromBytes, fromString);
    }

    @Test
    void mix64_isDeterministic() {
        // We test deterministic mixing for stable seed derivation.
        long input = 123456789L;

        assertEquals(Hash64.mix64(input), Hash64.mix64(input));
        assertNotEquals(Hash64.mix64(input), Hash64.mix64(input + 1));
    }

    @Test
    void morton2d_roundTrip_preservesCoordinates() {
        // We test encode/decode symmetry for 2D Morton mapping.
        int x = -123_456_789;
        int y = 987_654_321;

        long code = Morton.encode2D(x, y);

        assertEquals(x, Morton.decode2D_X(code));
        assertEquals(y, Morton.decode2D_Y(code));
    }

    @Test
    void morton3d_encode_isDeterministicAndSensitiveToInput() {
        // We test deterministic encoding and that coordinate changes affect the code.
        long c1 = Morton.encode3D(1, 2, 3);
        long c2 = Morton.encode3D(1, 2, 3);
        long c3 = Morton.encode3D(1, 2, 4);

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
    }

    @Test
    void morton3d_roundTrip_preservesCoordinatesWithinSupportedRange() {
        // We test encode/decode symmetry for valid 21-bit signed 3D coordinates.
        int x = -123_456;
        int y = 654_321;
        int z = -777_777;

        long code = Morton.encode3D(x, y, z);

        assertEquals(x, Morton.decode3D_X(code));
        assertEquals(y, Morton.decode3D_Y(code));
        assertEquals(z, Morton.decode3D_Z(code));
    }

    @Test
    void morton3d_rejectsOutOfRangeCoordinates() {
        // We test explicit guards for values outside supported signed 21-bit range.
        assertThrows(IllegalArgumentException.class, () -> Morton.encode3D(1_048_576, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> Morton.encode3D(0, -1_048_577, 0));
    }
}
