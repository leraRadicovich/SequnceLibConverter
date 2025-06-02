package org.example.converter.helper.model;

public class Box {
    private final String name;
    private final String color;

    public Box(String name, String color) {
        this.name = name;
        this.color = color != null ? color : "";
    }

    public String name() {
        return name;
    }

    public String color() {
        return color;
    }
}