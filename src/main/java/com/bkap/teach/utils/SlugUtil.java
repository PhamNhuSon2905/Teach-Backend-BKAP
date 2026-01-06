package com.bkap.teach.utils;

import java.text.Normalizer;

public class SlugUtil {

    public static String toSlugFilename(String filename) {
        if (filename == null) return null;

        int dot = filename.lastIndexOf(".");
        String name = dot > 0 ? filename.substring(0, dot) : filename;
        String ext  = dot > 0 ? filename.substring(dot) : "";

        name = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        name = name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");

        return name + ext.toLowerCase();
    }
}
