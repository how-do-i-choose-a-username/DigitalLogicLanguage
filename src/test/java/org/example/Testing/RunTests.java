package org.example.Testing;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunTests {
    @org.junit.jupiter.api.Test
    void runMainTests() {
        TestsRunnerMain.main(new String[] {});
        assertTrue(TestsRunnerMain.getTestCount() > 0 && TestsRunnerMain.getFailures() == 0, "Some of the automated compilation tests failed. Run it directly or check the log file for results");
    }
}
