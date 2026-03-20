package com.csd201.dungeon.unit;

public final class UnitTestRunner {
    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        if (runSuite("InventoryListUnitTest", InventoryListUnitTest::runAll)) {
            passed++;
        } else {
            failed++;
        }

        if (runSuite("DungeonGraphUnitTest", DungeonGraphUnitTest::runAll)) {
            passed++;
        } else {
            failed++;
        }

        System.out.println();
        System.out.println("Unit test suites passed: " + passed);
        System.out.println("Unit test suites failed: " + failed);

        if (failed > 0) System.exit(1);
    }

    private static boolean runSuite(String name, Runnable suite) {
        System.out.println("== " + name + " ==");
        try {
            suite.run();
            System.out.println("PASS " + name);
            return true;
        } catch (Throwable t) {
            System.out.println("FAIL " + name);
            t.printStackTrace(System.out);
            return false;
        }
    }
}

