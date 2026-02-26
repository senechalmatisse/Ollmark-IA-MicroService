
package com.example.demo.integrationTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.assertTrue;
@Tag("integrationTest")
class IntegrationTest {

    @Test
    void alwaysPasses() {
        assertTrue(true, "Ce test passe toujours");
    }
}