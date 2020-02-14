package com.mazenk.telegram.aesthethicc;

import com.jtelegram.api.TelegramBot;
import com.jtelegram.api.TelegramBotRegistry;
import com.jtelegram.api.events.inline.InlineQueryEvent;
import com.jtelegram.api.inline.input.InputTextMessageContent;
import com.jtelegram.api.inline.result.InlineResultArticle;
import com.jtelegram.api.requests.inline.AnswerInlineQuery;
import com.jtelegram.api.requests.message.framework.ParseMode;
import com.mazenk.telegram.aesthethicc.transform.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AesthethiccBot {
    private final ExecutorService service = Executors.newWorkStealingPool(3);
    private TelegramBot bot;
    private List<TextTransformer> transformers = new ArrayList<>();

    public AesthethiccBot(TelegramBotRegistry registry) {
        transformers.add(new ClassicAesthethicc());
        transformers.add(new ClapperAesthethicc());
        transformers.add(new ChillAesthethicc());
        transformers.add(new YellAesthethicc());
        transformers.add(new MockingAesthethicc());


        registry.registerBot(System.getenv("AESTHETHICC_KEY"), (bot, error) -> {
                    if (error != null) {
                        System.out.println("Unable to start AesthethiccBot!");
                        return;
                    }

                    System.out.println("Logged in as @" + bot.getBotInfo().getUsername());
                    this.bot = bot;

                    bot.getEventRegistry().registerEvent(InlineQueryEvent.class, this::handleIncomingQuery);
                });
    }

    private void handleIncomingQuery(InlineQueryEvent event) {
        service.execute(() -> {
            String input = event.getQuery().getQuery();

            if (input.length() <= 1) {
                return;
            }

            AnswerInlineQuery.AnswerInlineQueryBuilder builder = AnswerInlineQuery.builder();

            for (int i = 0; i < transformers.size(); i++) {
                TextTransformer transformer = transformers.get(i);
                String output = transformer.transform(input);

                builder.addResult(InlineResultArticle.builder()
                        .id(String.valueOf(i))
                        .title(transformer.getTitle())
                        .description(output)
                        .inputMessageContent(
                                InputTextMessageContent.builder()
                                        .messageText(output)
                                        .parseMode(ParseMode.MARKDOWN)
                                        .build()
                        )
                        .build());
            }

            bot.perform(builder.queryId(event.getQuery().getId())
                    .callback(() -> {})
                    .errorHandler((c) -> {
                        System.out.println("Didn't work because " + c.getDescription());
                    })
                    .build());
        });
    }
}