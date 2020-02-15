package com.mazenk.telegram.dicktionary;

import com.google.gson.annotations.SerializedName;
import com.jtelegram.api.inline.input.InputTextMessageContent;
import com.jtelegram.api.inline.result.InlineResultArticle;
import com.jtelegram.api.inline.result.InlineResultAudio;
import com.jtelegram.api.inline.result.framework.InlineResult;
import com.jtelegram.api.requests.message.framework.ParseMode;

import java.util.LinkedList;
import java.util.List;

public class DicktionaryResult {
    private List<String> tags;
    @SerializedName(value = "result_type")
    private String resultType;
    private List<DicktionaryDefinition> list;
    private List<String> sounds; // list of URLs

    public List<String> tags() {
        return tags;
    }

    public String resultType() {
        return resultType;
    }

    public List<DicktionaryDefinition> definitions() {
        return list;
    }

    public List<String> sounds() {
        return sounds;
    }

    public List<InlineResult> asResults(String query) {
        List<InlineResult> articles = new LinkedList<>();
        int idCount = 0;

        for (DicktionaryDefinition definition : definitions()) {
            InlineResultArticle.InlineResultArticleBuilder builder =
                    InlineResultArticle.builder()
                            .id(String.valueOf(definition.definitionId()))
                            .title(definition.word() == null || definition.word().isEmpty() ? query : definition.word())
                            .description(definition.definition())
                            .url(definition.permalink());


            builder.inputMessageContent(InputTextMessageContent.builder()
                    .messageText(definition.toString())
                    .parseMode(ParseMode.HTML)
                    .build());
            articles.add(builder.build());

            if (articles.size() >= 4) {
                break; // overload of results
            }
        }

        if (sounds != null) {
            for (String url : sounds) {
                articles.add(InlineResultAudio.builder()
                        .id(String.valueOf(++idCount))
                        .url(url)
                        .title(query).build());

                if (idCount >= 3) {
                    break; // nobody wants this many audio clips
                }
            }
        }

        return articles;
    }
}