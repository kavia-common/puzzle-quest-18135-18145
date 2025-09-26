package org.example.app

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Simple smoke test to ensure the Gradle test task discovers and executes at least one test.
 * This prevents CI failures when no other tests are present yet.
 */
class SmokeTest {

    @Test
    fun testAlwaysPasses() {
        assertTrue(true, "Smoke test should always pass")
    }
}
