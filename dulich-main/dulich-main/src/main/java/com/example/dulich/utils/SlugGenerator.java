package com.example.dulich.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugGenerator {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

    public static String toSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .toLowerCase(Locale.ENGLISH);
                
        // Thay thế các ký tự tiếng Việt
        normalized = normalized.replaceAll("[đ]", "d");
        normalized = normalized.replaceAll("[êếềểễệ]", "e");
        normalized = normalized.replaceAll("[áàảãạâấầẩẫậăắằẳẵặ]", "a");
        normalized = normalized.replaceAll("[óòỏõọôốồổỗộơớờởỡợ]", "o");
        normalized = normalized.replaceAll("[íìỉĩị]", "i");
        normalized = normalized.replaceAll("[úùủũụưứừửữự]", "u");
        normalized = normalized.replaceAll("[ýỳỷỹỵ]", "y");
        
        normalized = NONLATIN.matcher(normalized).replaceAll("");
        normalized = WHITESPACE.matcher(normalized).replaceAll("-");
        normalized = EDGESDHASHES.matcher(normalized).replaceAll("");
        
        return normalized;
    }
}
