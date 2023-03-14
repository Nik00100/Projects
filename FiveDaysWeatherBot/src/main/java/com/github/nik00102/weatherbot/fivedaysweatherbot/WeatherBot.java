package com.github.nik00102.weatherbot.fivedaysweatherbot;


import com.github.nik00102.weatherbot.fivedaysweatherbot.parcers.OpenWeatherMapJsonParser;
import com.github.nik00102.weatherbot.fivedaysweatherbot.service.SendBotMessageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


@Slf4j
@Component
public class WeatherBot extends TelegramLongPollingBot {

    private SendBotMessageServiceImpl sendBotMessageService = new SendBotMessageServiceImpl(this);

    @Autowired
    private OpenWeatherMapJsonParser openWeatherMapJsonParser;

    @Value("${bot.username}")
    private String username;
    @Value("${bot.token}")
    private String token;

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();

            if(update.getMessage().getText().trim().equals("/start")) {
                sendBotMessageService.sendMessage(chatId,"Введите город, в котором хотите узнать погоду:");
            } else {
                String city = update.getMessage().getText().trim();
                String message = openWeatherMapJsonParser.getReadyForecast(city);
                sendBotMessageService.sendMessage(chatId,message);
            }
        }
    }

}
