package com.texmo.pumpselector;

public class Option {
    public final String value;
    public final String label;

    public Option(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
