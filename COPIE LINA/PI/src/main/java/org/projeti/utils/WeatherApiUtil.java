package org.projeti.utils;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherApiUtil {
    private static final String API_KEY = "c0a6aa6bae204fde954162110250403";
    private static final String BASE_URL = "http://api.weatherapi.com/v1/forecast.json";

    public static WeatherInfo getWeather(String location, String date) {
        try {
            String urlString = BASE_URL + "?key=" + API_KEY + "&q=" + location + "&dt=" + date;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Vérifier si la réponse contient bien les données attendues
                JSONObject json = new JSONObject(response.toString());
                if (!json.has("forecast")) {
                    System.err.println("Erreur: 'forecast' introuvable dans la réponse API.");
                    return null;
                }

                JSONObject forecast = json.getJSONObject("forecast");
                JSONArray forecastDays = forecast.getJSONArray("forecastday");
                if (forecastDays.length() == 0) {
                    System.err.println("Erreur: 'forecastday' vide.");
                    return null;
                }

                JSONObject forecastDay = forecastDays.getJSONObject(0);
                JSONObject day = forecastDay.getJSONObject("day");
                double minTemp = day.optDouble("mintemp_c", -1); // Valeur par défaut -1 si absent
                double maxTemp = day.optDouble("maxtemp_c", -1);
                String condition = day.getJSONObject("condition").optString("text", "Inconnu");

                return new WeatherInfo(minTemp, maxTemp, condition);
            } else {
                System.err.println("Échec de la requête API. Code réponse: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class WeatherInfo {
        private double minTemp;
        private double maxTemp;
        private String condition;

        public WeatherInfo(double minTemp, double maxTemp, String condition) {
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.condition = condition;
        }

        public double getMinTemp() {
            return minTemp;
        }

        public double getMaxTemp() {
            return maxTemp;
        }

        public String getCondition() {
            return condition;
        }
    }
}
