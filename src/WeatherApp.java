import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

// retrieve latest weather data from external API which will be displayed by the GUI to the user.
public class WeatherApp {
    // fetch weather date for searched location
    public static JSONObject getWeatherData(String locationName) {
        // get location using the geolocation API
        JSONArray locationData = getLocationData(locationName);

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

    public static HttpURLConnection fetchApiResponse(String urlString) {
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
}
