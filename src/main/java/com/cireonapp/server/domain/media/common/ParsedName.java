package com.cireonapp.server.domain.media.common;

import java.nio.file.Path;

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

    public static ParsedName parseName(Path filePath) {
        String file = filePath.getFileName().toString();
        file = file.replaceFirst("\\.[^.]+$", "");
        file = file.replaceAll("(?i)\\b(1337x|thepiratebay|tpb|torrentgalaxy|tgx|limetorrents|magnetdl|yts|yify|nyaa|fitgirl|repacks|eztv|rutracker|zooqle|kickasstorrents|kat|torrentz2|btdig|snowfl|animetosho|myanonamouse|mam|academictorrents|tamilrockers|extto|torlock|torrentdownloads|yourbittorrent|rarbg|rutor|ibit|demonoid|pirateiro|kinozal|btmet|qbittorrent|deluge|transmission|utorrent|biglybt|tixati|vuze)\\b", "");
        file = file.replaceAll("(?i)\\b(1080p|720p|2160p|4k|bluray|brrip|webrip|x264|x265|h264|h265)\\b", "");
        file = file.replaceAll("\\[.*?]", "");
        file = file.replaceAll("\\.", " ");
        file = file.trim();

        ParsedName result = new ParsedName();

        var match = file.matches(".*\\(\\d{4}\\)$");

        if (match) {
            int idx = file.lastIndexOf('(');
            result.setName(file.substring(0, idx).trim());
            result.setYear(file.substring(idx + 1, idx + 5));
            return result;
        }

        var yearMatcher = java.util.regex.Pattern.compile("(\\d{4})").matcher(file);
        if (yearMatcher.find()) {
            result.setYear(yearMatcher.group(1));
            file = file.substring(0, yearMatcher.start());
        }

        result.setName(file.trim());
        return result;
    }
}
