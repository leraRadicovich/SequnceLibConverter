package org.example.converter.helper.model;

public class Participant {
    private final String type;
    private final String name;
    private final String alias;
    private final String order;

    public Participant(String type, String name, String alias, String order) {
        this.type = type;
        this.name = name;
        this.alias = alias;
        this.order = order;
    }

    public String toPartiesString() {
        return String.format("parties(%s,\"%s\",%s,%s)",
                type, name, alias != null ? alias : "", order != null ? order : "");
    }

    public String aliasOrName() {
        return alias != null ? alias : name;
    }
}