package com.csd201.dungeon.benchmark;
import com.csd201.dungeon.model.Monster;
import java.util.Random;

public class MonsterDataFactory {

    public Monster[] generateMonsters(int n) {
        if (n <= 0) return new Monster[0];

        Monster[] data = new Monster[n];
        for (int i = 1; i <= n; i++) {
            int level = 1 + ((i - 1) % 10);
            int hp = 50 + ((i - 1) % 151);   // 50..200
            int atk = 5 + ((i - 1) % 46);    // 5..50
            int dropItemId = ((i % 5) == 0) ? i : -1;
            data[i - 1] = new Monster(i, "Monster" + i, level, hp, atk, dropItemId);
        }
        return data;
    }

    public int[] shuffledIds(int n, int seed) {
        if (n <= 0) return new int[0];

        int[] ids = new int[n];
        for (int i = 0; i < n; i++) ids[i] = i + 1;

        Random rng = new Random(seed);
        for (int i = n - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = ids[i];
            ids[i] = ids[j];
            ids[j] = tmp;
        }
        return ids;
    }

    public int[] randomQueries(int q, int n, int seed) {
        if (q <= 0 || n <= 0) return new int[0];

        int[] queries = new int[q];
        Random rng = new Random(seed);
        for (int i = 0; i < q; i++) {
            queries[i] = 1 + rng.nextInt(n);
        }
        return queries;
    }
}

