package com.cireonapp.server.util;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TextSimilarityHelper {

    public static double ratio(String s1, String s2) {
        if (s1.equals(s2)) return 100.0;
        int distance = getLevenshteinDistance(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        return maxLen == 0 ? 100.0 : (1.0 - ((double) distance / maxLen)) * 100.0;
    }

    private static int getLevenshteinDistance(String s1, String s2) {
        LevenshteinDistance distance = new LevenshteinDistance();
        return distance.apply(s1, s2);
    }


    // 2. Partial Ratio
    public static double partialRatio(String s1, String s2) {
        if (s1.isEmpty() || s2.isEmpty()) return 0;
        String shorter = s1.length() < s2.length() ? s1 : s2;
        String longer = s1.length() < s2.length() ? s2 : s1;

        double maxRatio = 0;
        int shortLen = shorter.length();
        for (int i = 0; i <= longer.length() - shortLen; i++) {
            String sub = longer.substring(i, i + shortLen);
            maxRatio = Math.max(maxRatio, ratio(shorter, sub));
        }
        return maxRatio;
    }

    // 3. Token Set Ratio (Crucial for true WRatio)
    public static double tokenSetRatio(String s1, String s2) {
        Set<String> set1 = tokenizeToSet(s1);
        Set<String> set2 = tokenizeToSet(s2);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> diff1to2 = new HashSet<>(set1);
        diff1to2.removeAll(set2);

        Set<String> diff2to1 = new HashSet<>(set2);
        diff2to1.removeAll(set1);

        List<String> sortedIntersection = intersection.stream().sorted().collect(Collectors.toList());
        List<String> sortedDiff1 = diff1to2.stream().sorted().collect(Collectors.toList());
        List<String> sortedDiff2 = diff2to1.stream().sorted().collect(Collectors.toList());

        String t0 = String.join(" ", sortedIntersection).trim();
        String t1 = (t0 + " " + String.join(" ", sortedDiff1)).trim();
        String t2 = (t0 + " " + String.join(" ", sortedDiff2)).trim();

        double r1 = ratio(t0, t1);
        double r2 = ratio(t0, t2);
        double r3 = ratio(t1, t2);

        return Math.max(Math.max(r1, r2), r3);
    }

    private static Set<String> tokenizeToSet(String s) {
        String[] tokens = s.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");
        return new HashSet<>(Arrays.asList(tokens));
    }

    // 4. Pure Java WRatio Implementation
    public static int weightedRatio(String s1, String s2) {
        String str1 = s1.toLowerCase().trim();
        String str2 = s2.toLowerCase().trim();

        double base = ratio(str1, str2);
        double lenRatio = (double) Math.max(str1.length(), str2.length()) /
                Math.min(str1.length(), str2.length());

        if (lenRatio < 1.5) {
            double tokenSet = tokenSetRatio(str1, str2) * 0.95;
            return (int) Math.round(Math.max(base, tokenSet));
        }

        double partial = partialRatio(str1, str2) * 0.9;
        double partialTokenSet = tokenSetRatio(str1, str2) * 0.85; // Scaled down for variance

        return (int) Math.round(Math.max(Math.max(base, partial), partialTokenSet));
    }
}
