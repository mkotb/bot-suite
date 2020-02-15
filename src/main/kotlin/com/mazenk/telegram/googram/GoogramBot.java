package com.mazenk.telegram.googram;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jtelegram.api.TelegramBot;
import com.jtelegram.api.TelegramBotRegistry;
import com.jtelegram.api.events.inline.InlineQueryEvent;
import com.jtelegram.api.inline.InlineQuery;
import com.jtelegram.api.inline.input.InputTextMessageContent;
import com.jtelegram.api.inline.result.InlineResultArticle;
import com.jtelegram.api.inline.result.framework.InlineResult;
import com.jtelegram.api.requests.inline.AnswerInlineQuery;
import com.jtelegram.api.requests.message.framework.ParseMode;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class GoogramBot {
    private final ExecutorService service = Executors.newWorkStealingPool(3);
    private final Gson gson = new Gson();
    private List<String> keys;
    private TelegramBot bot;
    private OkHttpClient httpClient;
    private AtomicInteger keyIndex = new AtomicInteger(-1);

    public GoogramBot(TelegramBotRegistry botRegistry) {
        keys = Arrays.asList(System.getenv("GOOGLE_KEYS").split("//"));

        httpClient = botRegistry.getClient();

        botRegistry.registerBot(System.getenv("GOOGRAM_KEY"), (bot, error) -> {
                    if (error != null) {
                        System.out.println("Could not log into Telegram, printing error");
                        error.printStackTrace();

                        System.exit(-1);
                        return;
                    }

                    this.bot = bot;
                    bot.getEventRegistry().registerEvent(InlineQueryEvent.class, this::onInlineQuery);

                    System.out.println("Logged in as @" + bot.getBotInfo().getUsername());
                });
    }

    public List<GoogleResult> search(String query, int timeout) throws URISyntaxException, IOException, DailyLimitExceededException {
        List<GoogleResult> results = new ArrayList<>();

        URI uri = new URI(
                "https",
                "www.googleapis.com",
                "/customsearch/v1",
                "q=" + query + "&" +
                "key=" + keys.get(nextKeyIndex()) + "&" +
                "cx=" + "000917504380048684589:konlxv5xaaw",
                null
        );
        Request request = new Request.Builder()
                .url(uri.toString()).get().build();
        Response rawResponse = httpClient.newCall(request).execute();
        JsonObject response = gson.fromJson(rawResponse.body().string(), JsonObject.class);
        JsonArray array = new JsonArray();

        if (response.has("items")) {
            array = response.getAsJsonArray("items");
        }

        if (response.has("error") && response.getAsJsonObject("error").get("code").getAsInt() == 403) {
            if (timeout < keys.size() - 1) {
                // we hit our daily limit, let's try another key
                return search(query, timeout + 1);
            } else {
                throw new DailyLimitExceededException();
            }
        }

        array.forEach((e) -> {
            if (e instanceof JsonObject) {
                results.add(gson.fromJson(e.toString(), GoogleResult.class));
            }
        });

        return results;
    }

    public int nextKeyIndex() {
        int next = keyIndex.incrementAndGet(); // threads lock here

        if (next == keys.size()) {
            keyIndex.set(0);
            next = 0;
        }

        return next;
    }

    private void sendError(InlineQuery query, Exception ex) {
        bot.perform(AnswerInlineQuery.builder()
                .queryId(query.getId())
                .results(Collections.singletonList(
                        InlineResultArticle.builder()
                                .id("1")
                                .title("Search failed!")
                                .description("Please contact @MazenK for help")
                                .thumbUrl("https://i.imgur.com/4QLcKXj.jpg")
                                .thumbWidth(200).thumbHeight(200)
                                .inputMessageContent(InputTextMessageContent.builder()
                                        .messageText("Google Search for " + query.getQuery() + " failed, contact @MazenK")
                                        .parseMode(ParseMode.NONE)
                                        .build())
                                .build()
                ))
                .isPersonal(true)
                .build()
        );
    }

    private void sendExceededNotification(InlineQuery query) {
        bot.perform(AnswerInlineQuery.builder()
                .queryId(query.getId())
                .results(Collections.singletonList(
                        InlineResultArticle.builder()
                                .id("1")
                                .title("Daily limit exceeded")
                                .description("The bot has exceeded its maximum queries for today. Try again later!")
                                .thumbUrl("https://i.imgur.com/4QLcKXj.jpg")
                                .thumbWidth(200).thumbHeight(200)
                                .inputMessageContent(InputTextMessageContent.builder()
                                        .messageText("The bot has exceeded its maximum queries for today. Try again later!")
                                        .parseMode(ParseMode.NONE)
                                        .build())
                                .build()
                ))
                .isPersonal(true)
                .build()
        );
    }

    public void onInlineQuery(InlineQueryEvent event) {
        service.execute(() -> {
            InlineQuery query = event.getQuery();
            List<GoogleResult> results;

            try {
                results = search(query.getQuery(), 0);
            } catch (DailyLimitExceededException ex) {
                sendExceededNotification(query);
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
                sendError(query, ex);
                return;
            }

            AnswerInlineQuery.AnswerInlineQueryBuilder responseBuilder =
                    AnswerInlineQuery.builder().queryId(query.getId()).cacheTime(6000);
            List<InlineResult> queryResults = new ArrayList<>();
            int id = 0;

            for (GoogleResult result : results) {
                queryResults.add(result.toArticle(++id));
            }

            bot.perform(responseBuilder.results(queryResults).build());
        });
    }
}