package com.example.internship.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CvKeywordExtractor {

    private final Map<String, String> synonymMap = new HashMap<>();

    public CvKeywordExtractor() {
        synonymMap.put("js", "javascript");
        synonymMap.put("ml", "machine learning");
        synonymMap.put("ai", "artificial intelligence");
        synonymMap.put("oop", "object oriented programming");
        synonymMap.put("db", "database");
        synonymMap.put("css3", "css");
        synonymMap.put("html5", "html");
        synonymMap.put("node", "nodejs");
        synonymMap.put("py", "python");
        synonymMap.put("c++", "cpp");
        synonymMap.put("reactjs", "react");
        synonymMap.put("vuejs", "vue");
    }

    public List<String> extractAndNormalize(String skillsStr) {
        List<String> normalizedList = new ArrayList<>();
        if (skillsStr == null || skillsStr.trim().isEmpty()) {
            return normalizedList;
        }

        String[] skills = skillsStr.split(",");
        for (String skill : skills) {
            String cleanSkill = skill.trim().toLowerCase();
            normalizedList.add(synonymMap.getOrDefault(cleanSkill, cleanSkill));
        }

        return normalizedList;
    }
}
