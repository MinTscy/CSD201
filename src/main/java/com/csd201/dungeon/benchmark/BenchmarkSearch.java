package com.csd201.dungeon.benchmark;

import com.csd201.dungeon.model.Monster;
import com.csd201.dungeon.service.MonsterBSTLookup;
import com.csd201.dungeon.service.MonsterListLookup;
import com.csd201.dungeon.service.MonsterLookup;

public class BenchmarkSearch {
    public static void main(String[] args) {
        final int n = 10_000;
        final int q = 2_000;
        final int warmupRounds = 3;
        final int measureRounds = 10;

        final int shuffleSeed = 12345;
        final int querySeed = 67890;

        MonsterDataFactory factory = new MonsterDataFactory();

        Monster[] monsters = factory.generateMonsters(n);
        int[] shuffledIds = factory.shuffledIds(n, shuffleSeed);
        int[] queries = factory.randomQueries(q, n, querySeed);

        Monster[] bstInsertionData = new Monster[n];
        for (int i = 0; i < n; i++) {
            int id = shuffledIds[i];
            int idx = id - 1;
            if (idx >= 0 && idx < monsters.length) {
                bstInsertionData[i] = monsters[idx];
            }
        }

        MonsterLookup listLookup = new MonsterListLookup();
        listLookup.build(monsters);

        MonsterLookup bstLookup = new MonsterBSTLookup();
        bstLookup.build(bstInsertionData);

        int sink = 0;

        // Warm-up (no recording)
        for (int r = 0; r < warmupRounds; r++) {
            for (int i = 0; i < queries.length; i++) {
                Monster m1 = listLookup.findById(queries[i]);
                if (m1 != null) sink ^= m1.getId();
                Monster m2 = bstLookup.findById(queries[i]);
                if (m2 != null) sink ^= m2.getId();
            }
        }

        long listTotalNs = 0L;
        long bstTotalNs = 0L;

        for (int r = 0; r < measureRounds; r++) {
            long t0 = System.nanoTime();
            for (int i = 0; i < queries.length; i++) {
                Monster m = listLookup.findById(queries[i]);
                if (m != null) sink ^= m.getId();
            }
            long t1 = System.nanoTime();

            long t2 = System.nanoTime();
            for (int i = 0; i < queries.length; i++) {
                Monster m = bstLookup.findById(queries[i]);
                if (m != null) sink ^= m.getId();
            }
            long t3 = System.nanoTime();

            listTotalNs += (t1 - t0);
            bstTotalNs += (t3 - t2);
        }

        long avgListNs = listTotalNs / measureRounds;
        long avgBstNs = bstTotalNs / measureRounds;
        double speedup = (avgBstNs == 0L) ? Double.POSITIVE_INFINITY : ((double) avgListNs / (double) avgBstNs);

        System.out.println("N=" + n + ", Q=" + q + ", rounds=" + measureRounds + ", warmup=" + warmupRounds);
        System.out.println("avg time list (ns): " + avgListNs);
        System.out.println("avg time bst  (ns): " + avgBstNs);
        System.out.println("speedup (list/bst): " + speedup);

        // Prevent JIT from completely discarding loops
        if (sink == 42) System.out.print("");
    }
}

