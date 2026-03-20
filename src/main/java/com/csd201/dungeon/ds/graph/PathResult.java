package com.csd201.dungeon.ds.graph;

import com.csd201.dungeon.model.Room;

import java.util.Collections;
import java.util.List;

public class PathResult {
    private final List<Room> rooms;
    private final int totalCost;

    public PathResult(List<Room> rooms, int totalCost) {
        this.rooms = rooms == null ? List.of() : List.copyOf(rooms);
        this.totalCost = totalCost;
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public int getTotalCost() {
        return totalCost;
    }

    public boolean isReachable() {
        return !rooms.isEmpty();
    }

    public String toRoomIdPath() {
        if (rooms.isEmpty()) {
            return "[No path]";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rooms.size(); i++) {
            if (i > 0) {
                sb.append(" -> ");
            }
            sb.append(rooms.get(i).getId());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "PathResult{rooms=" + toRoomIdPath() + ", totalCost=" + totalCost + "}";
    }
}
