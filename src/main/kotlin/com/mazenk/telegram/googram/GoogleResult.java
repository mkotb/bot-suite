package com.mazenk.telegram.googram;

import com.jtelegram.api.inline.input.InputTextMessageContent;
import com.jtelegram.api.inline.result.InlineResultArticle;
import com.jtelegram.api.requests.message.framework.ParseMode;

public class GoogleResult {
    private String title;
    private String link;
    private String snippet;
    private GoogleResultImage image;

    public String title() {
        return title;
    }

    public String link() {
        return link;
    }

    public String snippet() {
        return snippet;
    }

    public GoogleResultImage image() {
        return image;
    }

    public InlineResultArticle toArticle(int id) {
        InlineResultArticle.InlineResultArticleBuilder builder =
                InlineResultArticle.builder()
                .id(String.valueOf(id))
                .title(title)
                .url(link);

        if (snippet != null) {
            builder.description(snippet);
        }

        if (image != null) {
            builder.thumbUrl(image.link())
                    .thumbHeight(image.height())
                    .thumbWidth(image.width());
        }

        builder.inputMessageContent(InputTextMessageContent.builder()
                .messageText("[" + title + "](" + link + ")")
                .parseMode(ParseMode.MARKDOWN)
                .build()
        );

        return builder.build();
    }
}