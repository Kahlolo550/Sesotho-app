package com.example.demo19;

import java.util.List;

public class Question {
    private final String category;
    private final String level;
    private final String questionText;
    private final List<String> options;
    private final int correctAnswerIndex;
    private final String mediaPath;

    public Question(String category, String level, String questionText, List<String> options, int correctAnswerIndex, String mediaPath) {
        this.category = category;
        this.level = level;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.mediaPath = mediaPath;
    }

    // Getters
    public String getCategory() { return category; }
    public String getLevel() { return level; }
    public String getQuestionText() { return questionText; }
    public List<String> getOptions() { return options; }
    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public String getMediaPath() { return mediaPath; }
}