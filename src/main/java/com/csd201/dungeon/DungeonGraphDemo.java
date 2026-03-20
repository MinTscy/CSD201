package com.csd201.dungeon;

import com.csd201.dungeon.ds.graph.DungeonGraph;
import com.csd201.dungeon.ds.graph.PathResult;
import com.csd201.dungeon.model.Room;
import com.csd201.dungeon.service.DungeonMapLoader;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class DungeonGraphDemo {
    public static void main(String[] args) throws IOException {
        DungeonMapLoader loader = new DungeonMapLoader();
        DungeonGraph graph;
        try {
            graph = loader.loadFromResource("/dungeon-map.txt");
        } catch (IOException ex) {
            graph = loader.loadFromFile("src/main/resources/dungeon-map.txt");
        }

        System.out.println("=== Dungeon Graph Demo ===");
        System.out.println("Rooms loaded: " + graph.roomCount());
        printRooms(graph.getRooms());

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                printMenu();
                int choice = readInt(scanner, "Chon chuc nang: ");

                if (choice == 0) {
                    System.out.println("Thoat chuong trinh.");
                    break;
                }

                switch (choice) {
                    case 1 -> {
                        int startRoomId = readInt(scanner, "Nhap room bat dau: ");
                        PathResult exitPath = graph.findPathToNearestExitBfs(startRoomId);
                        printPathResult("Shortest BFS path to exit", exitPath);
                    }
                    case 2 -> {
                        int startRoomId = readInt(scanner, "Nhap room bat dau: ");
                        PathResult treasurePath = graph.findPathToNearestTreasureBfs(startRoomId);
                        printPathResult("Shortest BFS path to treasure", treasurePath);
                    }
                    case 3 -> {
                        int startRoomId = readInt(scanner, "Nhap room bat dau: ");
                        int targetRoomId = readInt(scanner, "Nhap room dich: ");
                        PathResult weightedPath = graph.findShortestPathDijkstra(startRoomId, targetRoomId);
                        printPathResult("Shortest Dijkstra path", weightedPath);
                    }
                    case 4 -> {
                        int startRoomId = readInt(scanner, "Nhap room bat dau: ");
                        List<Room> dfsOrder = graph.traverseDfs(startRoomId);
                        printDfsOrder(dfsOrder);
                    }
                    default -> System.out.println("Lua chon khong hop le. Vui long thu lai.");
                }

                System.out.println();
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1. Tim duong ngan nhat den exit bang BFS");
        System.out.println("2. Tim treasure gan nhat bang BFS");
        System.out.println("3. Tim duong ngan nhat bang Dijkstra");
        System.out.println("4. Duyet DFS");
        System.out.println("0. Thoat");
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println("Vui long nhap so nguyen hop le.");
            }
        }
    }

    private static void printRooms(List<Room> rooms) {
        System.out.println("Danh sach room:");
        for (Room room : rooms) {
            System.out.println("- Room " + room.getId() + ": " + room.getName());
        }
    }

    private static void printPathResult(String label, PathResult pathResult) {
        if (!pathResult.isReachable()) {
            System.out.println(label + ": khong tim thay duong di.");
            return;
        }

        System.out.println(label + ": " + pathResult.toRoomIdPath() + " | cost=" + pathResult.getTotalCost());
    }

    private static void printDfsOrder(List<Room> dfsOrder) {
        if (dfsOrder.isEmpty()) {
            System.out.println("DFS order: khong co room nao duoc tham.");
            return;
        }

        System.out.print("DFS order: ");
        for (int i = 0; i < dfsOrder.size(); i++) {
            if (i > 0) {
                System.out.print(" -> ");
            }
            System.out.print(dfsOrder.get(i).getId());
        }
        System.out.println();
    }
}
