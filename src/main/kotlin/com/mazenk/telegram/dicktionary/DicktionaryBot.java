package com.mazenk.telegram.dicktionary;

import com.google.gson.Gson;
import com.jtelegram.api.TelegramBot;
import com.jtelegram.api.TelegramBotRegistry;
import com.jtelegram.api.events.inline.InlineQueryEvent;
import com.jtelegram.api.inline.InlineQuery;
import com.jtelegram.api.inline.input.InputTextMessageContent;
import com.jtelegram.api.inline.result.InlineResultArticle;
import com.jtelegram.api.requests.inline.AnswerInlineQuery;
import com.jtelegram.api.requests.message.framework.ParseMode;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DicktionaryBot {
    private final ExecutorService service = Executors.newWorkStealingPool(3);
    private String errorImageUrl = "https://i.imgur.com/4QLcKXj.jpg";
    private String logoUrl = "https://i.imgur.com/oIiW7zp.png";
    private final Gson gson = new Gson();
    private OkHttpClient httpClient;
    private TelegramBot bot;

    public DicktionaryBot(TelegramBotRegistry registry) {
        this.httpClient = registry.getClient();

        registry.registerBot(System.getenv("DICKTIONARY_KEY"), (bot, error) -> {
                    if (error != null) {
                        System.out.println("Unable to start bot!");
                        error.printStackTrace();
                        return;
                    }

                    System.out.println("Logged in as @" + bot.getBotInfo().getUsername());
                    this.bot = bot;

                    bot.getEventRegistry().registerEvent(InlineQueryEvent.class, this::onInlineQueryReceived);
                });
    }

    public DicktionaryResult search(String query) throws IOException, URISyntaxException {
        URI uri = new URI (
                "http",
                "api.urbandictionary.com",
                "/v0/define",
                "term=" + query,
                null
        );
        Request request = new Request.Builder()
                .url(uri.toString()).get().build();
        Response rawResponse = httpClient.newCall(request).execute();

        return gson.fromJson(rawResponse.body().string(), DicktionaryResult.class);
    }

    private void sendError(InlineQuery query, Exception ex) {
        bot.perform(AnswerInlineQuery.builder()
                .queryId(query.getId())
                .results(Collections.singletonList(
                        InlineResultArticle.builder()
                                .id("1")
                                .title("Search failed!")
                                .description("Please contact @MazenK for help")
                                .thumbUrl(errorImageUrl)
                                .thumbWidth(200).thumbHeight(200)
                                .inputMessageContent(InputTextMessageContent.builder()
                                        .messageText("Urban Dictionary Search for " + query.getQuery() + " failed, contact @MazenK")
                                        .parseMode(ParseMode.NONE)
                                        .build())
                                .build()
                ))
                .isPersonal(true)
                .build());
    }

    public void onInlineQueryReceived(InlineQueryEvent event) {
        service.execute(() -> {
            InlineQuery query = event.getQuery();
            DicktionaryResult result;

            if (query.getQuery().isEmpty()) {
                bot.perform(AnswerInlineQuery.builder()
                        .queryId(query.getId())
                        .results(Arrays.asList(
                                InlineResultArticle.builder()
                                        .id("1")
                                        .thumbUrl(logoUrl)
                                        .thumbHeight(200).thumbWidth(200)
                                        .title("Search on Urban Dictionary!")
                                        .description("Bot made by @MazenK")
                                        .inputMessageContent(
                                                InputTextMessageContent.builder()
                                                        .messageText("Use @Dicktionary bot inline by tagging it then entering your query!")
                                                        .parseMode(ParseMode.NONE)
                                                        .build()
                                        )
                                        .build()
                                ))
                        .build()
                );
                return;
            }

            try {
                result = search(query.getQuery());
            } catch (Exception ex) {
                ex.printStackTrace();
                sendError(query, ex);
                return;
            }

            bot.perform(AnswerInlineQuery.builder()
                    .queryId(query.getId())
                    .results(result.asResults(query.getQuery()))
                    .isPersonal(false)
                    .build()
            );
        });
    }
}