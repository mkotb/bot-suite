package com.mazenk.telegram.dicktionary;

import com.google.gson.annotations.SerializedName;

public class DicktionaryDefinition {
    private String definition;
    private String permalink;
    @SerializedName(value = "thumbs_up")
    private int thumbsUp;
    private String author;
    private String word;
    @SerializedName(value = "defid")
    private int definitionId;
    private String example;
    @SerializedName(value = "thumbs_down")
    private int thumbsDown;

    private String htmlEscape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public String definition() {
        return definition;
    }

    public String permalink() {
        return permalink;
    }

    public int thumbsUp() {
        return thumbsUp;
    }

    public String author() {
        return author;
    }

    public String word() {
        return word;
    }

    public int definitionId() {
        return definitionId;
    }

    public String example() {
        return example;
    }

    public int thumbsDown() {
        return thumbsDown;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("<b>").append(htmlEscape(word)).append("</b>\n\n");

        if (author != null) {
            builder.append("Definition by <b>")
                    .append(htmlEscape(author))
                    .append("</b>\n\n");
        }

        if (definition != null && !"".equals(definition.trim())) {
            builder.append("\u2139️ <b>Definition</b>");

            if (!definition.startsWith("\n"))
                builder.append("\n");

            builder.append(htmlEscape(definition)).append("\n\n");
        }

        if (example != null && !"".equals(example.trim())) {
            builder.append("\uD83D\uDCCC <b>Examples</b>");

            if (!example.startsWith("\n"))
                builder.append("\n");

            builder.append(htmlEscape(example))
                    .append("\n\n");
        }

        builder.append("\uD83D\uDC4D ")
                .append(thumbsUp)
                .append(' ')
                .append("\uD83D\uDC4E ")
                .append(thumbsDown);

        return builder.toString();
    }
}