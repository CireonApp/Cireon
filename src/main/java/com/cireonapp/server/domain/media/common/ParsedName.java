package com.cireonapp.server.domain.media.common;

public class ParsedName {
    private String name;
    private String year;

    public ParsedName(String name, String year) {
        this.name = name;
        this.year = year;
    }

    public ParsedName() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "ParsedName{" +
                "name='" + name + '\'' +
                ", year='" + year + '\'' +
                '}';
    }
}
