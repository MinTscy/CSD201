package com.csd201.dungeon.ds.graph;

import com.csd201.dungeon.model.Room;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

public class DungeonGraph {
    private final Map<Integer, Room> rooms;
    private final Map<Integer, List<RoomEdge>> adjacency;

    public DungeonGraph() {
        this.rooms = new HashMap<>();
        this.adjacency = new HashMap<>();
    }

    public void addRoom(Room room) {
        if (room == null) {
            return;
        }

        rooms.put(room.getId(), room);
        adjacency.putIfAbsent(room.getId(), new ArrayList<>());
    }

    public boolean containsRoom(int roomId) {
        return rooms.containsKey(roomId);
    }

    public Room getRoom(int roomId) {
        return rooms.get(roomId);
    }

    public int roomCount() {
        return rooms.size();
    }

    public List<Room> getRooms() {
        List<Room> allRooms = new ArrayList<>(rooms.values());
        allRooms.sort(Comparator.comparingInt(Room::getId));
        return Collections.unmodifiableList(allRooms);
    }

    public List<RoomEdge> getNeighbors(int roomId) {
        List<RoomEdge> edges = adjacency.get(roomId);
        if (edges == null) {
            return List.of();
        }
        return Collections.unmodifiableList(edges);
    }

    public void addConnection(int fromRoomId, int toRoomId) {
        addConnection(fromRoomId, toRoomId, 1);
    }

    public void addConnection(int fromRoomId, int toRoomId, int weight) {
        validateEdge(fromRoomId, toRoomId, weight);
        adjacency.get(fromRoomId).add(new RoomEdge(fromRoomId, toRoomId, weight));
        adjacency.get(toRoomId).add(new RoomEdge(toRoomId, fromRoomId, weight));
    }

    public PathResult findShortestPathBfs(int startRoomId, int targetRoomId) {
        return findPathBfs(startRoomId, room -> room.getId() == targetRoomId);
    }

    public PathResult findPathToNearestExitBfs(int startRoomId) {
        return findPathBfs(startRoomId, Room::isExit);
    }

    public PathResult findPathToNearestTreasureBfs(int startRoomId) {
        return findPathBfs(startRoomId, Room::hasTreasure);
    }

    public List<Room> traverseDfs(int startRoomId) {
        if (!containsRoom(startRoomId)) {
            return List.of();
        }

        List<Room> visitedOrder = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        dfs(startRoomId, visited, visitedOrder);
        return Collections.unmodifiableList(visitedOrder);
    }

    public PathResult findShortestPathDijkstra(int startRoomId, int targetRoomId) {
        if (!containsRoom(startRoomId) || !containsRoom(targetRoomId)) {
            return new PathResult(List.of(), -1);
        }

        Map<Integer, Integer> distance = new HashMap<>();
        Map<Integer, Integer> previous = new HashMap<>();
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));

        for (Room room : rooms.values()) {
            distance.put(room.getId(), Integer.MAX_VALUE);
        }

        distance.put(startRoomId, 0);
        pq.offer(new int[]{startRoomId, 0});

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int currentRoomId = current[0];
            int currentDistance = current[1];

            if (currentDistance > distance.get(currentRoomId)) {
                continue;
            }

            if (currentRoomId == targetRoomId) {
                break;
            }

            for (RoomEdge edge : adjacency.getOrDefault(currentRoomId, List.of())) {
                int nextRoomId = edge.getToRoomId();
                int candidate = currentDistance + edge.getWeight();

                if (candidate < distance.get(nextRoomId)) {
                    distance.put(nextRoomId, candidate);
                    previous.put(nextRoomId, currentRoomId);
                    pq.offer(new int[]{nextRoomId, candidate});
                }
            }
        }

        int totalCost = distance.getOrDefault(targetRoomId, Integer.MAX_VALUE);
        if (totalCost == Integer.MAX_VALUE) {
            return new PathResult(List.of(), -1);
        }

        return new PathResult(reconstructPath(previous, startRoomId, targetRoomId), totalCost);
    }

    private void validateEdge(int fromRoomId, int toRoomId, int weight) {
        if (!containsRoom(fromRoomId) || !containsRoom(toRoomId)) {
            throw new IllegalArgumentException("Both rooms must exist before adding a connection.");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Edge weight must be positive.");
        }
    }

    private PathResult findPathBfs(int startRoomId, Predicate<Room> targetCondition) {
        if (!containsRoom(startRoomId)) {
            return new PathResult(List.of(), -1);
        }

        Queue<Integer> queue = new ArrayDeque<>();
        Set<Integer> visited = new HashSet<>();
        Map<Integer, Integer> previous = new HashMap<>();

        queue.offer(startRoomId);
        visited.add(startRoomId);

        while (!queue.isEmpty()) {
            int currentRoomId = queue.poll();
            Room currentRoom = rooms.get(currentRoomId);

            if (targetCondition.test(currentRoom)) {
                List<Room> path = reconstructPath(previous, startRoomId, currentRoomId);
                return new PathResult(path, path.size() - 1);
            }

            for (RoomEdge edge : adjacency.getOrDefault(currentRoomId, List.of())) {
                int nextRoomId = edge.getToRoomId();
                if (visited.contains(nextRoomId)) {
                    continue;
                }

                visited.add(nextRoomId);
                previous.put(nextRoomId, currentRoomId);
                queue.offer(nextRoomId);
            }
        }

        return new PathResult(List.of(), -1);
    }

    private List<Room> reconstructPath(Map<Integer, Integer> previous, int startRoomId, int targetRoomId) {
        ArrayDeque<Room> stack = new ArrayDeque<>();
        Integer current = targetRoomId;

        while (current != null) {
            Room room = rooms.get(current);
            if (room == null) {
                return List.of();
            }
            stack.push(room);
            if (current == startRoomId) {
                break;
            }
            current = previous.get(current);
        }

        if (stack.isEmpty() || stack.peek().getId() != startRoomId) {
            return List.of();
        }

        return new ArrayList<>(stack);
    }

    private void dfs(int roomId, Set<Integer> visited, List<Room> visitedOrder) {
        visited.add(roomId);
        visitedOrder.add(rooms.get(roomId));

        for (RoomEdge edge : adjacency.getOrDefault(roomId, List.of())) {
            int nextRoomId = edge.getToRoomId();
            if (!visited.contains(nextRoomId)) {
                dfs(nextRoomId, visited, visitedOrder);
            }
        }
    }
}
