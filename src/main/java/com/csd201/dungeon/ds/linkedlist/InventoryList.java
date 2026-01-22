package com.csd201.dungeon.ds.linkedlist;

import com.csd201.dungeon.model.Item;

public class InventoryList {
    private Node head;
    private Node tail;
    private int size;

    public InventoryList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void addFirst(Item item) {
        if (item == null) return;

        Node n = new Node(item);
        if (head == null) {
            head = tail = n;
        } else {
            n.next = head;
            head = n;
        }
        size++;
    }

    public void addLast(Item item) {
        if (item == null) return;

        Node n = new Node(item);
        if (tail == null) {
            head = tail = n;
        } else {
            tail.next = n;
            tail = n;
        }
        size++;
    }

    public Item findById(int id) {
        Node cur = head;
        while (cur != null) {
            if (cur.data != null && cur.data.getId() == id) return cur.data;
            cur = cur.next;
        }
        return null;
    }

    public Item findByName(String name) {
        if (name == null) return null;
        String key = name.trim().toLowerCase();

        Node cur = head;
        while (cur != null) {
            if (cur.data != null && cur.data.getName() != null) {
                String n = cur.data.getName().trim().toLowerCase();
                if (n.equals(key)) return cur.data;
            }
            cur = cur.next;
        }
        return null;
    }

    public boolean removeById(int id) {
        if (head == null) return false;

        // remove head
        if (head.data != null && head.data.getId() == id) {
            head = head.next;
            size--;
            if (head == null) tail = null; // list became empty
            return true;
        }

        Node prev = head;
        Node cur = head.next;

        while (cur != null) {
            if (cur.data != null && cur.data.getId() == id) {
                prev.next = cur.next;
                size--;
                if (cur == tail) tail = prev; // removed tail
                return true;
            }
            prev = cur;
            cur = cur.next;
        }

        return false;
    }

    public void clear() {
        head = tail = null;
        size = 0;
    }

    public String toDisplayString() {
        if (head == null) return "[Inventory empty]";
        StringBuilder sb = new StringBuilder();
        sb.append("Inventory (").append(size).append(" items)\n");

        Node cur = head;
        int i = 1;
        while (cur != null) {
            sb.append(i).append(". ").append(cur.data).append("\n");
            i++;
            cur = cur.next;
        }
        return sb.toString();
    }

    public void printAll() {
        System.out.print(toDisplayString());
    }
}
