package com.example.dulich.utils;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class for creating URL-friendly slugs from strings
 */
public class SlugUtil {
    
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTIPLE_DASHES = Pattern.compile("-+");
    
    /**
     * Create a URL-friendly slug from a string
     * 
     * @param input the string to convert to a slug
     * @return the slugified string
     */
    public static String makeSlug(String input) {
        if (input == null) {
            return "";
        }
        
        // Normalize the string to remove diacritical marks
        String noWhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = MULTIPLE_DASHES.matcher(slug).replaceAll("-");
        slug = slug.toLowerCase(Locale.ENGLISH);
        slug = slug.trim();
        
        // Remove starting and ending dashes
        if (slug.startsWith("-")) {
            slug = slug.substring(1);
        }
        if (slug.endsWith("-")) {
            slug = slug.substring(0, slug.length() - 1);
        }
        
        return slug;
    }
}
