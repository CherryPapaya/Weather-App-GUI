import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

// retrieve latest weather data from external API which will be displayed by the GUI to the user.
public class WeatherApp {
    // fetch weather data for searched location
    public static JSONObject getWeatherData(String locationName) {
        // get location using the geolocation API
        JSONArray locationData = getLocationData(locationName);

        // extract latitude and longitude data
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // build API request URL with location coordinates
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,weather_code,wind_speed_10m,relative_humidity_2m&timezone=Africa%2FCairo";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);

            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            } else {
                StringBuilder resultsJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                while (scanner.hasNext()) {
                    resultsJson.append(scanner.nextLine());
                }

                scanner.close();
                conn.disconnect();

                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultsJson));

                JSONObject hourly = (JSONObject) resultsJsonObj.get("hourly");

                // get index of current hour to extract that hour's data
                JSONArray time = (JSONArray) hourly.get("time");
//                int index = findIndexOfCurrentTime(time);
                int index = 0;

//                // array to recursively extract the data
//                String[] weatherFactors = {"temperature_2m", "weather_code", "wind_speed_10m", "relative_humidity_2m"};
//
//                // extract data from each weather factor and place into JSON Object
//                for (String weatherFactor : weatherFactors) {
//                    JSONArray weatherFactorArray = (JSONArray) hourly.get(weatherFactor);
//
//                    if (String.valueOf(weatherFactorArray.get(index)).contains(".")) {
//                        double weatherFactorValue = (double) weatherFactorArray.get(index);
//                        weatherData.put(weatherFactor, weatherFactorValue);
//                    } else {
//                        long weatherFactorValue = (long) weatherFactorArray.get(index);
//                        weatherData.put(weatherFactor, weatherFactorValue);
//                    }
//                }

                // get data for each weather factor
                JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
                double temperature = (double) temperatureData.get(index);

                JSONArray weatherCode = (JSONArray) hourly.get("weather_code");
                String weatherCondition = convertWeatherCode((long) weatherCode.get(index));

                JSONArray humidityData = (JSONArray) hourly.get("relative_humidity_2m");
                long humidity = (long) humidityData.get(index);

                JSONArray windSpeedData = (JSONArray) hourly.get("wind_speed_10m");
                double windSpeed = (double) windSpeedData.get(index);

                // JSONObject to store all the data
                JSONObject weatherData = new JSONObject();
                weatherData.put("temperature", temperature);
                weatherData.put("weather_condition", weatherCondition);
                weatherData.put("humidity", humidity);
                weatherData.put("wind_speed", windSpeed);

                return weatherData;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public static JSONArray getLocationData(String locationName) {
        // replace any white spaces in the location name with "+" to adhere to API's request format
        locationName = locationName.replaceAll(" ", "+");

        // build API URL with location parameter
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name="
                + locationName + "&count=10&language=en&format=json";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);

            // check response status
            // 200 means successful connection
                // HTTP response codes
            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            } else {
                // store the API results
                StringBuilder resultsJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                // read and store data in the string-builder
                while (scanner.hasNext()) {
                    resultsJson.append(scanner.nextLine());
                }

                scanner.close();
                conn.disconnect();

                // parse the JSON string into a JSON obj
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultsJson));

                // get the list of location data the API generated from the location name
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;

            }



        } catch (Exception e) {
            e.printStackTrace();
        }

        // couldn't find location
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            // attempt to create a connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // set request method to GET
            conn.setRequestMethod("GET");

            // connect to our API
            conn.connect();
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // failed to create connection
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();

        int i = 0;
        for (Object timeObj : timeList) {
            String time = (String) timeList.get(i);
            if (time.equalsIgnoreCase(currentTime)) {
                return i;
            } else {
                i++;
            }
        }
        return -1;
    }

    private static String getCurrentTime() {
        // get current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();

        // making the format of currentDateTime identical to the API's format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;

    }

    // return weather condition based on weather code
    private static String convertWeatherCode(long weatherCode) {
        String weatherCondtion = "";

        if (weatherCode == 0L) {
            weatherCondtion = "Clear Sky";
        } else if (weatherCode >= 1L && weatherCode <= 3L) {
            weatherCondtion = "Cloudy";
        }
//        else if (weatherCode >= 45L && weatherCode <= 48L) {
//            weatherCondtion = "Foggy";
//        }
        else if (weatherCode >= 51L && weatherCode <= 67L
                || weatherCode >= 80L && weatherCode <= 99L) {
            weatherCondtion = "Rain";
        } else if (weatherCode >= 71L && weatherCode <= 77L) {
            weatherCondtion = "Snow";
        }

        return weatherCondtion;

    }
}
