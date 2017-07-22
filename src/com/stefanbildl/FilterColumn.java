package com.stefanbildl;

/**
 * Created by stefan on 09.07.17.
 */
public class FilterColumn {
    private String column;
    private String label;
    private int width;

    public FilterColumn(String column, String label, int width) {
        this.column = column;
        this.label = label;
        this.width = width;
    }
    public FilterColumn() {}

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
