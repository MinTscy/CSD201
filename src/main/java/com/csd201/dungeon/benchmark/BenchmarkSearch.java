package com.csd201.dungeon.benchmark;

import com.csd201.dungeon.model.Monster;
import com.csd201.dungeon.service.MonsterBSTLookup;
import com.csd201.dungeon.service.MonsterListLookup;
import com.csd201.dungeon.service.MonsterLookup;

public class BenchmarkSearch {
    // So luong monster dung de tao du lieu benchmark
    private static final int MONSTER_COUNT = 10_000;
    // So luong lan tim kiem se duoc thuc hien
    private static final int QUERY_COUNT = 2_000;
    // Chay thu truoc khi do thoi gian that
    private static final int WARMUP_ROUNDS = 3;
    // So lan do de lay thoi gian trung binh
    private static final int MEASURE_ROUNDS = 10;

    // Seed co dinh de ket qua random co the lap lai
    private static final int SHUFFLE_SEED = 12345;
    private static final int QUERY_SEED = 67890;

    // Bien phu tro de tranh JVM bo qua vong lap khi toi uu
    private static int sink = 0;

    public static void main(String[] args) {
        MonsterDataFactory factory = new MonsterDataFactory();

        // Tao danh sach monster goc va danh sach query can tim
        Monster[] monsters = factory.generateMonsters(MONSTER_COUNT);
        int[] shuffledIds = factory.shuffledIds(MONSTER_COUNT, SHUFFLE_SEED);
        int[] queries = factory.randomQueries(QUERY_COUNT, MONSTER_COUNT, QUERY_SEED);

        // Tao thu tu chen vao BST theo ID da xao tron
        Monster[] bstData = createBstData(monsters, shuffledIds);

        // Cau truc 1: tim tuyen tinh trong mang
        MonsterLookup listLookup = new MonsterListLookup();
        listLookup.build(monsters);

        // Cau truc 2: tim kiem bang cay BST
        MonsterLookup bstLookup = new MonsterBSTLookup();
        bstLookup.build(bstData);

        // Chay thu de JVM on dinh truoc khi benchmark that
        warmUp(listLookup, bstLookup, queries);

        // Do thoi gian trung binh cua tung cach tim kiem
        long listAverageTime = measureAverageTime(listLookup, queries);
        long bstAverageTime = measureAverageTime(bstLookup, queries);

        // In ket qua so sanh ra man hinh
        printResult(listAverageTime, bstAverageTime);

        if (sink == 42) {
            System.out.print("");
        }
    }

    private static Monster[] createBstData(Monster[] monsters, int[] shuffledIds) {
        // Tao mang moi de dua du lieu vao BST theo thu tu xao tron
        Monster[] bstData = new Monster[shuffledIds.length];

        for (int i = 0; i < shuffledIds.length; i++) {
            int monsterId = shuffledIds[i];
            bstData[i] = monsters[monsterId - 1];
        }

        return bstData;
    }

    private static void warmUp(MonsterLookup listLookup, MonsterLookup bstLookup, int[] queries) {
        // Chay thu ca list va BST trong vai vong de ket qua do on dinh hon
        for (int round = 0; round < WARMUP_ROUNDS; round++) {
            runQueries(listLookup, queries);
            runQueries(bstLookup, queries);
        }
    }

    private static long measureAverageTime(MonsterLookup lookup, int[] queries) {
        // Cong tong thoi gian cua nhieu lan do roi lay trung binh
        long totalTime = 0L;

        for (int round = 0; round < MEASURE_ROUNDS; round++) {
            long startTime = System.nanoTime();
            runQueries(lookup, queries);
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }

        return totalTime / MEASURE_ROUNDS;
    }

    private static void runQueries(MonsterLookup lookup, int[] queries) {
        // Duyet tung ID can tim va goi ham findById
        for (int queryId : queries) {
            Monster monster = lookup.findById(queryId);
            if (monster != null) {
                sink ^= monster.getId();
            }
        }
    }

    private static void printResult(long listAverageTime, long bstAverageTime) {
        // Tinh toc do chenh lech giua list va BST
        double speedup = (bstAverageTime == 0L)
                ? Double.POSITIVE_INFINITY
                : (double) listAverageTime / bstAverageTime;

        System.out.println("Monster count: " + MONSTER_COUNT);
        System.out.println("Query count: " + QUERY_COUNT);
        System.out.println("Warm-up rounds: " + WARMUP_ROUNDS);
        System.out.println("Measure rounds: " + MEASURE_ROUNDS);
        System.out.println("Average list search time (ns): " + listAverageTime);
        System.out.println("Average BST search time  (ns): " + bstAverageTime);
        System.out.println("List/BST speedup: " + speedup);
    }
}
