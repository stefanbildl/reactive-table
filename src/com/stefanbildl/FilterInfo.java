package com.stefanbildl;

/**
 * Created by stefan on 09.07.17.
 */
public class FilterInfo {
    private boolean isAscending;
    private String column;

    public FilterInfo(boolean isAscending, String column) {
        this.isAscending = isAscending;
        this.column = column;
    }

    public boolean isAscending() {
        return isAscending;
    }

    public void setAscending(boolean ascending) {
        isAscending = ascending;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }
}
