package com.csd201.dungeon;

import com.csd201.dungeon.web.DungeonWebServer;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        DungeonWebServer server = new DungeonWebServer(port);
        server.start();
        System.out.println("Dungeon web UI is running at http://localhost:" + port);
    }
}
