package com.csd201.dungeon.ds.graph;

public class RoomEdge {
    private final int fromRoomId;
    private final int toRoomId;
    private final int weight;

    public RoomEdge(int fromRoomId, int toRoomId, int weight) {
        this.fromRoomId = fromRoomId;
        this.toRoomId = toRoomId;
        this.weight = weight;
    }

    public int getFromRoomId() {
        return fromRoomId;
    }

    public int getToRoomId() {
        return toRoomId;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "RoomEdge{fromRoomId=" + fromRoomId + ", toRoomId=" + toRoomId + ", weight=" + weight + "}";
    }
}
