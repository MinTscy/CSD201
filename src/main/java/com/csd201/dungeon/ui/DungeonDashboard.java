package com.csd201.dungeon.ui;

import com.csd201.dungeon.app.DungeonProjectData;
import com.csd201.dungeon.ds.graph.DungeonGraph;
import com.csd201.dungeon.ds.graph.PathResult;
import com.csd201.dungeon.ds.graph.RoomEdge;
import com.csd201.dungeon.model.Item;
import com.csd201.dungeon.model.Monster;
import com.csd201.dungeon.model.Player;
import com.csd201.dungeon.model.Room;
import com.csd201.dungeon.service.MonsterLookup;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DungeonDashboard extends JFrame {
    private static final Color PAGE_BG = new Color(241, 236, 225);
    private static final Color CARD_BG = new Color(255, 251, 243);
    private static final Color INK = new Color(44, 37, 32);
    private static final Color ACCENT = new Color(123, 64, 43);
    private static final Color MUTED = new Color(118, 109, 100);

    private final DungeonProjectData data;
    private final Player player;
    private final DungeonGraph graph;
    private final MonsterLookup monsterLookup;
    private final Map<Integer, Item> itemCatalog;

    private final DefaultListModel<String> inventoryModel = new DefaultListModel<>();
    private final List<Item> inventoryItems = new ArrayList<>();
    private final DefaultListModel<Room> roomListModel = new DefaultListModel<>();

    private final JLabel playerSummaryLabel = new JLabel();
    private final JLabel roomStatusLabel = new JLabel();
    private final JTextArea roomDetailsArea = createTextArea();
    private final JTextArea pathArea = createTextArea();
    private final JTextArea monsterDetailsArea = createTextArea();
    private final JTextField searchMonsterField = new JTextField(8);
    private final JTextField searchInventoryField = new JTextField(10);
    private final JComboBox<String> addItemComboBox = new JComboBox<>();
    private final JComboBox<Room> targetRoomComboBox = new JComboBox<>();
    private final JList<Room> roomList = new JList<>(roomListModel);

    public DungeonDashboard(DungeonProjectData data) {
        this.data = data;
        this.player = data.getPlayer();
        this.graph = data.getGraph();
        this.monsterLookup = data.getMonsterLookup();
        this.itemCatalog = data.getItemCatalog();

        setTitle("Dungeon Crawler Project UI - Week 10");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1220, 760));
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout(18, 18));
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        content.setBackground(PAGE_BG);
        setContentPane(content);

        content.add(createHeader(), BorderLayout.NORTH);
        content.add(createMainArea(), BorderLayout.CENTER);

        initializeData();
        refreshAll();
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            configureLookAndFeel();
            try {
                DungeonDashboard frame = new DungeonDashboard(DungeonProjectData.createDefault());
                frame.setVisible(true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Cannot load dungeon data: " + ex.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setOpaque(false);

        JLabel title = new JLabel("Dungeon Crawler Control Room");
        title.setFont(new Font("Georgia", Font.BOLD, 28));
        title.setForeground(INK);

        JLabel subtitle = new JLabel("Week 10 UI integration for Linked List, BST, and Graph modules");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        JPanel textGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        textGroup.setOpaque(false);
        textGroup.add(title);
        textGroup.add(subtitle);

        JPanel badge = new JPanel(new BorderLayout());
        badge.setBackground(new Color(232, 214, 192));
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 178, 146), 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JLabel badgeText = new JLabel("Project Demo UI", SwingConstants.CENTER);
        badgeText.setFont(new Font("Segoe UI", Font.BOLD, 15));
        badgeText.setForeground(ACCENT);
        badge.add(badgeText, BorderLayout.CENTER);

        header.add(textGroup, BorderLayout.CENTER);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    private Component createMainArea() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createRightPanel());
        splitPane.setResizeWeight(0.46);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        return splitPane;
    }

    private JPanel createLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(16, 16));
        left.setOpaque(false);
        left.add(createPlayerCard(), BorderLayout.NORTH);
        left.add(createInventoryCard(), BorderLayout.CENTER);
        return left;
    }

    private JPanel createRightPanel() {
        JPanel right = new JPanel(new BorderLayout(16, 16));
        right.setOpaque(false);
        right.add(createMonsterCard(), BorderLayout.NORTH);
        right.add(createMapCard(), BorderLayout.CENTER);
        return right;
    }

    private JPanel createPlayerCard() {
        JPanel card = createCard("Player Snapshot");
        JPanel body = new JPanel(new GridLayout(2, 1, 0, 8));
        body.setOpaque(false);

        playerSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        playerSummaryLabel.setForeground(INK);

        roomStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roomStatusLabel.setForeground(MUTED);

        body.add(playerSummaryLabel);
        body.add(roomStatusLabel);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createInventoryCard() {
        JPanel card = createCard("Inventory Linked List");

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setOpaque(false);
        styleField(searchInventoryField);

        JButton searchButton = createAccentButton("Find Item");
        searchButton.addActionListener(e -> findInventoryItem());

        JButton removeButton = createSecondaryButton("Remove Selected");
        removeButton.addActionListener(e -> removeSelectedInventoryItem());

        topBar.add(new JLabel("Name:"));
        topBar.add(searchInventoryField);
        topBar.add(searchButton);
        topBar.add(removeButton);

        JPanel addBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        addBar.setOpaque(false);
        styleCombo(addItemComboBox);

        JButton addButton = createAccentButton("Add Item");
        addButton.addActionListener(e -> addSelectedCatalogItem());

        JButton usePotionButton = createSecondaryButton("Use Potion");
        usePotionButton.addActionListener(e -> usePotion());

        addBar.add(new JLabel("Catalog:"));
        addBar.add(addItemComboBox);
        addBar.add(addButton);
        addBar.add(usePotionButton);

        JList<String> inventoryList = new JList<>(inventoryModel);
        inventoryList.setBackground(CARD_BG);
        inventoryList.setForeground(INK);
        inventoryList.setFont(new Font("Consolas", Font.PLAIN, 14));

        JPanel controls = new JPanel(new GridLayout(2, 1, 0, 8));
        controls.setOpaque(false);
        controls.add(topBar);
        controls.add(addBar);

        card.add(controls, BorderLayout.NORTH);
        card.add(createScrollPane(inventoryList), BorderLayout.CENTER);
        return card;
    }

    private JPanel createMonsterCard() {
        JPanel card = createCard("Monster BST Lookup");

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);
        styleField(searchMonsterField);

        JButton searchButton = createAccentButton("Search by ID");
        searchButton.addActionListener(e -> findMonsterById());

        JButton inspectRoomButton = createSecondaryButton("Inspect Current Room");
        inspectRoomButton.addActionListener(e -> inspectCurrentRoomMonster());

        controls.add(new JLabel("Monster ID:"));
        controls.add(searchMonsterField);
        controls.add(searchButton);
        controls.add(inspectRoomButton);

        card.add(controls, BorderLayout.NORTH);
        card.add(createScrollPane(monsterDetailsArea), BorderLayout.CENTER);
        return card;
    }

    private JPanel createMapCard() {
        JPanel card = createCard("Dungeon Graph Explorer");

        roomList.setBackground(CARD_BG);
        roomList.setForeground(INK);
        roomList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roomList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value instanceof Room room) {
                    label.setText("Room " + room.getId() + " - " + room.getName());
                }
                return label;
            }
        });
        roomList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Room room = roomList.getSelectedValue();
                if (room != null) {
                    targetRoomComboBox.setSelectedItem(room);
                    showRoomDetails(room);
                }
            }
        });

        styleCombo(targetRoomComboBox);
        targetRoomComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value instanceof Room room) {
                    label.setText("Room " + room.getId() + " - " + room.getName());
                }
                return label;
            }
        });

        JPanel commandBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        commandBar.setOpaque(false);

        JButton nearestExitButton = createAccentButton("BFS to Exit");
        nearestExitButton.addActionListener(e -> runNearestExit());

        JButton nearestTreasureButton = createAccentButton("BFS to Treasure");
        nearestTreasureButton.addActionListener(e -> runNearestTreasure());

        JButton dijkstraButton = createSecondaryButton("Dijkstra to Target");
        dijkstraButton.addActionListener(e -> runDijkstra());

        JButton dfsButton = createSecondaryButton("DFS Traverse");
        dfsButton.addActionListener(e -> runDfs());

        JButton moveButton = createSecondaryButton("Move Hero");
        moveButton.addActionListener(e -> moveHeroToSelection());

        commandBar.add(new JLabel("Target:"));
        commandBar.add(targetRoomComboBox);
        commandBar.add(nearestExitButton);
        commandBar.add(nearestTreasureButton);
        commandBar.add(dijkstraButton);
        commandBar.add(dfsButton);
        commandBar.add(moveButton);

        JSplitPane innerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createScrollPane(roomList),
                createMapDetailsPanel());
        innerSplit.setResizeWeight(0.33);
        innerSplit.setBorder(null);

        card.add(commandBar, BorderLayout.NORTH);
        card.add(innerSplit, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMapDetailsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 12));
        panel.setOpaque(false);
        panel.add(createLabeledArea("Room Details", roomDetailsArea));
        panel.add(createLabeledArea("Path Result", pathArea));
        return panel;
    }

    private JPanel createLabeledArea(String title, JTextArea area) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(INK);

        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(createScrollPane(area), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 208, 190), 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 19));
        titleLabel.setForeground(ACCENT);
        card.add(titleLabel, BorderLayout.NORTH);
        return card;
    }

    private JScrollPane createScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 208, 190), 1));
        scrollPane.getViewport().setBackground(CARD_BG);
        return scrollPane;
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(CARD_BG);
        area.setForeground(INK);
        area.setFont(new Font("Consolas", Font.PLAIN, 14));
        return area;
    }

    private JButton createAccentButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(224, 216, 201));
        button.setForeground(INK);
        button.setFocusPainted(false);
        return button;
    }

    private void styleField(JTextField field) {
        field.setPreferredSize(new Dimension(120, 30));
    }

    private void styleCombo(JComboBox<?> comboBox) {
        comboBox.setPreferredSize(new Dimension(160, 30));
    }

    private void initializeData() {
        inventoryItems.add(data.createCatalogItemCopy(3));
        inventoryItems.add(data.createCatalogItemCopy(1));
        inventoryItems.add(data.createCatalogItemCopy(2));

        for (Item item : itemCatalog.values()) {
            addItemComboBox.addItem(item.getId() + " - " + item.getName() + " [" + item.getType() + "]");
        }

        for (Room room : graph.getRooms()) {
            roomListModel.addElement(room);
            targetRoomComboBox.addItem(room);
        }
    }

    private void refreshAll() {
        refreshPlayerSummary();
        refreshInventoryView();
        refreshCurrentRoomView();
        inspectCurrentRoomMonster();
    }

    private void refreshPlayerSummary() {
        playerSummaryLabel.setText(player.getName() + "  |  HP " + player.getHp() + "  |  ATK " + player.getAtk()
                + "  |  Inventory " + inventoryItems.size());

        Room currentRoom = graph.getRoom(player.getCurrentRoomId());
        if (currentRoom == null) {
            roomStatusLabel.setText("Current room is unavailable.");
            return;
        }

        String flags = currentRoom.isExit() ? "Exit room"
                : (currentRoom.hasTreasure() ? "Treasure room" : "Standard room");
        roomStatusLabel
                .setText("Current room: " + currentRoom.getId() + " - " + currentRoom.getName() + "  |  " + flags);
    }

    private void refreshInventoryView() {
        inventoryModel.clear();
        if (inventoryItems.isEmpty()) {
            inventoryModel.addElement("[Inventory empty]");
            return;
        }

        for (int i = 0; i < inventoryItems.size(); i++) {
            Item item = inventoryItems.get(i);
            inventoryModel.addElement((i + 1) + ". ID " + item.getId() + " | " + item.getName()
                    + " | " + item.getType() + " | value=" + item.getValue());
        }
    }

    private void refreshCurrentRoomView() {
        Room currentRoom = graph.getRoom(player.getCurrentRoomId());
        if (currentRoom != null) {
            roomList.setSelectedValue(currentRoom, true);
            targetRoomComboBox.setSelectedItem(currentRoom);
            showRoomDetails(currentRoom);
        }
    }

    private void showRoomDetails(Room room) {
        StringBuilder builder = new StringBuilder();
        builder.append("Room ").append(room.getId()).append(" - ").append(room.getName()).append("\n\n");
        builder.append(room.getDescription()).append("\n\n");
        builder.append("Exit: ").append(room.isExit()).append("\n");
        builder.append("Treasure: ").append(room.hasTreasure()).append("\n");
        builder.append("Monster ID: ").append(room.getMonsterId()).append("\n");
        builder.append("Neighbors: ").append(formatNeighbors(room.getId()));
        roomDetailsArea.setText(builder.toString());
    }

    private String formatNeighbors(int roomId) {
        List<RoomEdge> neighbors = graph.getNeighbors(roomId);
        if (neighbors.isEmpty()) {
            return "None";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < neighbors.size(); i++) {
            RoomEdge edge = neighbors.get(i);
            Room neighbor = graph.getRoom(edge.getToRoomId());
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(edge.getToRoomId());
            if (neighbor != null) {
                builder.append(" (").append(neighbor.getName()).append(")");
            }
            builder.append(" w=").append(edge.getWeight());
        }
        return builder.toString();
    }

    private void addSelectedCatalogItem() {
        int selectedIndex = addItemComboBox.getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }

        int itemId = new ArrayList<>(itemCatalog.keySet()).get(selectedIndex);
        Item item = data.createCatalogItemCopy(itemId);
        if (item == null) {
            return;
        }

        player.getInventory().addLast(item);
        inventoryItems.add(item);
        refreshAll();
    }

    private void removeSelectedInventoryItem() {
        if (inventoryItems.isEmpty()) {
            return;
        }

        String input = JOptionPane.showInputDialog(this, "Enter item ID to remove:", "Remove Item",
                JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.isBlank()) {
            return;
        }

        try {
            int itemId = Integer.parseInt(input.trim());
            boolean removed = player.getInventory().removeById(itemId);
            if (removed) {
                inventoryItems.removeIf(item -> item.getId() == itemId);
                refreshAll();
            } else {
                showMessage("Item ID " + itemId + " was not found in the linked list.");
            }
        } catch (NumberFormatException ex) {
            showMessage("Please enter a valid integer item ID.");
        }
    }

    private void findInventoryItem() {
        String query = searchInventoryField.getText().trim();
        if (query.isEmpty()) {
            showMessage("Enter an item name first.");
            return;
        }

        Item item = player.getInventory().findByName(query);
        if (item == null) {
            showMessage("No inventory item matched \"" + query + "\".");
            return;
        }

        showMessage("Found: ID " + item.getId() + " - " + item.getName() + " [" + item.getType() + "]");
    }

    private void usePotion() {
        Item potion = null;
        for (Item item : inventoryItems) {
            if ("POTION".equalsIgnoreCase(item.getType())) {
                potion = item;
                break;
            }
        }

        if (potion == null) {
            showMessage("There is no potion available to use.");
            return;
        }

        player.heal(potion.getValue());
        player.getInventory().removeById(potion.getId());
        inventoryItems.remove(potion);
        refreshAll();
        showMessage("Used " + potion.getName() + " and restored " + potion.getValue() + " HP.");
    }

    private void findMonsterById() {
        String input = searchMonsterField.getText().trim();
        if (input.isEmpty()) {
            showMessage("Enter a monster ID first.");
            return;
        }

        try {
            int monsterId = Integer.parseInt(input);
            Monster monster = monsterLookup.findById(monsterId);
            if (monster == null) {
                monsterDetailsArea.setText("Monster " + monsterId + " was not found in the BST.");
            } else {
                monsterDetailsArea.setText(formatMonster(monster));
            }
        } catch (NumberFormatException ex) {
            showMessage("Please enter a valid integer monster ID.");
        }
    }

    private void inspectCurrentRoomMonster() {
        Room currentRoom = graph.getRoom(player.getCurrentRoomId());
        if (currentRoom == null || currentRoom.getMonsterId() < 0) {
            monsterDetailsArea.setText("The current room has no monster.");
            return;
        }

        Monster monster = monsterLookup.findById(currentRoom.getMonsterId());
        if (monster == null) {
            monsterDetailsArea.setText("Monster ID " + currentRoom.getMonsterId() + " is missing from the BST.");
            return;
        }

        monsterDetailsArea.setText("Current room encounter\n\n" + formatMonster(monster));
    }

    private String formatMonster(Monster monster) {
        return "Monster ID: " + monster.getId() + "\n"
                + "Name: " + monster.getName() + "\n"
                + "Level: " + monster.getLevel() + "\n"
                + "HP: " + monster.getHp() + "\n"
                + "ATK: " + monster.getAtk() + "\n"
                + "Drop Item ID: " + monster.getDropItemId();
    }

    private void runNearestExit() {
        PathResult result = graph.findPathToNearestExitBfs(player.getCurrentRoomId());
        pathArea.setText(formatPath("BFS path to nearest exit", result));
    }

    private void runNearestTreasure() {
        PathResult result = graph.findPathToNearestTreasureBfs(player.getCurrentRoomId());
        pathArea.setText(formatPath("BFS path to nearest treasure", result));
    }

    private void runDijkstra() {
        Room target = (Room) targetRoomComboBox.getSelectedItem();
        if (target == null) {
            return;
        }

        PathResult result = graph.findShortestPathDijkstra(player.getCurrentRoomId(), target.getId());
        pathArea.setText(formatPath("Dijkstra shortest path", result));
    }

    private void runDfs() {
        List<Room> order = graph.traverseDfs(player.getCurrentRoomId());
        StringBuilder builder = new StringBuilder("DFS traversal from current room\n\n");
        if (order.isEmpty()) {
            builder.append("No rooms visited.");
        } else {
            for (int i = 0; i < order.size(); i++) {
                Room room = order.get(i);
                builder.append(i + 1).append(". Room ").append(room.getId()).append(" - ").append(room.getName())
                        .append("\n");
            }
        }
        pathArea.setText(builder.toString());
    }

    private void moveHeroToSelection() {
        Room target = (Room) targetRoomComboBox.getSelectedItem();
        if (target == null) {
            return;
        }

        player.setCurrentRoomId(target.getId());
        refreshAll();
        pathArea.setText("Hero moved to room " + target.getId() + " - " + target.getName() + ".");
    }

    private String formatPath(String title, PathResult result) {
        StringBuilder builder = new StringBuilder(title).append("\n\n");
        if (!result.isReachable()) {
            builder.append("No path found.");
            return builder.toString();
        }

        builder.append("Path: ").append(result.toRoomIdPath()).append("\n");
        builder.append("Cost: ").append(result.getTotalCost()).append("\n\n");
        builder.append("Rooms:\n");
        for (Room room : result.getRooms()) {
            builder.append("- ").append(room.getId()).append(": ").append(room.getName()).append("\n");
        }
        return builder.toString();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Dungeon UI", JOptionPane.INFORMATION_MESSAGE);
    }
}
