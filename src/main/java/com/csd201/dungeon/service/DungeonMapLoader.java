package com.csd201.dungeon.service;

import com.csd201.dungeon.ds.graph.DungeonGraph;
import com.csd201.dungeon.model.Room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DungeonMapLoader {

    public DungeonGraph loadFromFile(String filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            return parse(reader);
        }
    }

    public DungeonGraph loadFromResource(String resourcePath) throws IOException {
        InputStream inputStream = DungeonMapLoader.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return parse(reader);
        }
    }

    private DungeonGraph parse(BufferedReader reader) throws IOException {
        DungeonGraph graph = new DungeonGraph();
        List<int[]> pendingEdges = new ArrayList<>();
        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            String trimmed = line.trim();

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            String[] parts = trimmed.split("\\|");
            String recordType = parts[0].trim().toUpperCase();

            switch (recordType) {
                case "ROOM" -> graph.addRoom(parseRoom(parts, lineNumber));
                case "EDGE" -> pendingEdges.add(parseEdge(parts, lineNumber));
                default -> throw new IOException("Unknown record type at line " + lineNumber + ": " + parts[0]);
            }
        }

        for (int[] edge : pendingEdges) {
            graph.addConnection(edge[0], edge[1], edge[2]);
        }

        return graph;
    }

    private Room parseRoom(String[] parts, int lineNumber) throws IOException {
        if (parts.length != 7) {
            throw new IOException("Invalid ROOM record at line " + lineNumber + ". Expected 7 columns.");
        }

        try {
            int id = Integer.parseInt(parts[1].trim());
            String name = parts[2].trim();
            String description = parts[3].trim();
            boolean isExit = Boolean.parseBoolean(parts[4].trim());
            boolean hasTreasure = Boolean.parseBoolean(parts[5].trim());
            int monsterId = Integer.parseInt(parts[6].trim());
            return new Room(id, name, description, isExit, hasTreasure, monsterId);
        } catch (NumberFormatException ex) {
            throw new IOException("Invalid ROOM number at line " + lineNumber, ex);
        }
    }

    private int[] parseEdge(String[] parts, int lineNumber) throws IOException {
        if (parts.length != 4) {
            throw new IOException("Invalid EDGE record at line " + lineNumber + ". Expected 4 columns.");
        }

        try {
            int fromRoomId = Integer.parseInt(parts[1].trim());
            int toRoomId = Integer.parseInt(parts[2].trim());
            int weight = Integer.parseInt(parts[3].trim());
            return new int[]{fromRoomId, toRoomId, weight};
        } catch (NumberFormatException ex) {
            throw new IOException("Invalid EDGE number at line " + lineNumber, ex);
        }
    }
}
