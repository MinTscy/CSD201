package com.csd201.dungeon.web;

import com.csd201.dungeon.app.DungeonProjectData;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class DungeonWebServer {
    private final HttpServer server;

    public DungeonWebServer(int port) throws IOException {
        DungeonWebState state = new DungeonWebState(DungeonProjectData.createDefault());
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        server.createContext("/", exchange -> serveStatic(exchange, "/web/index.html", "text/html; charset=UTF-8"));
        server.createContext("/styles.css", exchange -> serveStatic(exchange, "/web/styles.css", "text/css; charset=UTF-8"));
        server.createContext("/app.js", exchange -> serveStatic(exchange, "/web/app.js", "application/javascript; charset=UTF-8"));

        server.createContext("/api/state", exchange -> sendJson(exchange, 200, state.buildStateJson()));
        server.createContext("/api/monster", exchange -> handle(exchange, "GET", params ->
                sendJson(exchange, 200, state.findMonsterJson(parseInt(params, "id", -1)))));
        server.createContext("/api/monster/current", exchange -> sendJson(exchange, 200, state.inspectCurrentRoomMonsterJson()));
        server.createContext("/api/inventory/find", exchange -> handle(exchange, "GET", params ->
                sendJson(exchange, 200, state.findInventoryJson(params.getOrDefault("name", "")))));
        server.createContext("/api/inventory/add", exchange -> handle(exchange, "POST", params ->
                sendJson(exchange, 200, state.addItemJson(parseInt(params, "itemId", -1)))));
        server.createContext("/api/inventory/create", exchange -> handle(exchange, "POST", params ->
                sendJson(exchange, 200, state.createItemJson(
                        params.getOrDefault("name", ""),
                        params.getOrDefault("type", "MISC"),
                        parseInt(params, "value", 0)
                ))));
        server.createContext("/api/inventory/update", exchange -> handle(exchange, "POST", params ->
                sendJson(exchange, 200, state.updateItemJson(
                        parseInt(params, "itemId", -1),
                        params.getOrDefault("name", ""),
                        params.getOrDefault("type", "MISC"),
                        parseInt(params, "value", 0)
                ))));
        server.createContext("/api/inventory/remove", exchange -> handle(exchange, "POST", params ->
                sendJson(exchange, 200, state.removeItemJson(parseInt(params, "itemId", -1)))));
        server.createContext("/api/inventory/use-potion", exchange -> handle(exchange, "POST", params ->
                sendJson(exchange, 200, state.usePotionJson())));
        server.createContext("/api/player/move", exchange -> handle(exchange, "POST", params ->
                sendJson(exchange, 200, state.moveHeroJson(parseInt(params, "roomId", -1)))));
        server.createContext("/api/game/attack", exchange -> handle(exchange, "POST", params ->
                sendJson(exchange, 200, state.attackMonsterJson())));
        server.createContext("/api/game/claim-treasure", exchange -> handle(exchange, "POST", params ->
                sendJson(exchange, 200, state.claimTreasureJson())));
        server.createContext("/api/path/exit", exchange -> sendJson(exchange, 200, state.nearestExitJson()));
        server.createContext("/api/path/treasure", exchange -> sendJson(exchange, 200, state.nearestTreasureJson()));
        server.createContext("/api/path/dijkstra", exchange -> handle(exchange, "GET", params ->
                sendJson(exchange, 200, state.dijkstraJson(parseInt(params, "targetRoomId", -1)))));
        server.createContext("/api/path/dfs", exchange -> sendJson(exchange, 200, state.dfsJson()));
    }

    public void start() {
        server.start();
    }

    private void serveStatic(HttpExchange exchange, String resourcePath, String contentType) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed", "text/plain; charset=UTF-8");
            return;
        }

        try (InputStream stream = DungeonWebServer.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                sendText(exchange, 404, "Not Found", "text/plain; charset=UTF-8");
                return;
            }

            byte[] bytes = stream.readAllBytes();
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", contentType);
            headers.set("Cache-Control", "no-store");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        }
    }

    private void handle(HttpExchange exchange, String expectedMethod, HandlerAction action) throws IOException {
        if (!expectedMethod.equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed", "text/plain; charset=UTF-8");
            return;
        }
        action.run(parseQuery(exchange.getRequestURI()));
    }

    private void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        sendText(exchange, status, body, "application/json; charset=UTF-8");
    }

    private void sendText(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        headers.set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx < 0) {
                params.put(decode(pair), "");
            } else {
                params.put(decode(pair.substring(0, idx)), decode(pair.substring(idx + 1)));
            }
        }
        return params;
    }

    private int parseInt(Map<String, String> params, String key, int fallback) {
        try {
            return Integer.parseInt(params.getOrDefault(key, String.valueOf(fallback)));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String decode(String raw) {
        return URLDecoder.decode(raw, StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    private interface HandlerAction {
        void run(Map<String, String> params) throws IOException;
    }
}
