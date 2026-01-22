package com.csd201.dungeon.ds.linkedlist;


import com.csd201.dungeon.model.Item;

public class Node {
    public Item data;
    public Node next;

    public Node(Item data) {
        this.data = data;
        this.next = null;
    }
}
