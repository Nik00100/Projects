package com.github.nik00102.weatherbot.fivedaysweatherbot.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherUtils {
    public final static Map<String, List<String>> weatherIconsCodes = new HashMap<>();


    static {
        List<String> listClear = new ArrayList<>();
        listClear.add("Ясно");
        listClear.add("☀");
        weatherIconsCodes.put("Clear", listClear);
        List<String> listRain = new ArrayList<>();
        listRain.add("Дождь");
        listRain.add("☔");
        weatherIconsCodes.put("Rain", listRain);
        List<String> listSnow = new ArrayList<>();
        listSnow.add("Снег");
        listSnow.add("❄");
        weatherIconsCodes.put("Snow", listSnow);
        List<String> listClouds = new ArrayList<>();
        listClouds.add("Облачно");
        listClouds.add("☁");
        weatherIconsCodes.put("Clouds", listClouds);
    }
}
