package com.granpa.pumpselector;

public class Option {
    public final String value;
    public final String label;
    public final String detail;
    public final boolean mainCategory;

    public Option(String value, String label) {
        this(value, label, "", false);
    }

    public Option(String value, String label, String detail) {
        this(value, label, detail, false);
    }

    public Option(String value, String label, String detail, boolean mainCategory) {
        this.value = value;
        this.label = label;
        this.detail = detail == null ? "" : detail;
        this.mainCategory = mainCategory;
    }

    @Override
    public String toString() {
        return label;
    }
}
