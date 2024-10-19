package com.weatherapp;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.*;
import org.json.JSONObject;

public class WeatherForecast extends JFrame {
    private static final String API_KEY = "YOUR API KEY";// from openweathermap
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    private JTextField cityField;
    private JTextArea resultArea;
    private JList<String> historyList;
    private DefaultListModel<String> historyListModel;
    private JLabel backgroundLabel;

    public WeatherForecast() {
        setTitle("Weather Forecast");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set a background color
        backgroundLabel = new JLabel();
        backgroundLabel.setLayout(new BorderLayout());
        backgroundLabel.setOpaque(true);
        backgroundLabel.setBackground(new Color(173, 216, 230)); // Light blue color
        setContentPane(backgroundLabel);

        // Set font size
        Font font = new Font("Arial", Font.PLAIN, 20);

        // Panel for user input and button
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);  // Make panel transparent

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // Add padding

        cityField = new JTextField(15);
        cityField.setFont(font);
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(cityField, gbc);

        JButton fetchButton = new JButton("Fetch Weather");
        fetchButton.setFont(font);
        gbc.gridx = 1;
        gbc.gridy = 0;
        inputPanel.add(fetchButton, gbc);

        resultArea = new JTextArea(10, 20);
        resultArea.setOpaque(false);  // Make text area transparent
        resultArea.setForeground(Color.BLACK);  // Set text color to black
        resultArea.setFont(font);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);  // Make scroll pane transparent
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        inputPanel.add(scrollPane, gbc);

        // History list
        historyListModel = new DefaultListModel<>();
        historyList = new JList<>(historyListModel);
        historyList.setFont(font);

        JScrollPane historyScrollPane = new JScrollPane(historyList);
        historyScrollPane.setOpaque(false);
        historyScrollPane.getViewport().setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        inputPanel.add(historyScrollPane, gbc);

        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchWeather();
            }
        });

        add(inputPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void fetchWeather() {
        String city = cityField.getText().trim();
        if (city.isEmpty() || city.equalsIgnoreCase("Enter city")) {
            resultArea.setText("Please enter a valid city name.");
            return;
        }

        city = city.replace(" ", "%20");  // Replace spaces with %20 for URL encoding
        String urlString = BASE_URL + city + "&appid=" + API_KEY + "&units=metric";

        System.out.println("Constructed URL: " + urlString);  // Debug URL print

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String formattedData = formatWeatherData(response.toString());
                resultArea.setText(formattedData);

                // Add to history
                historyListModel.addElement(city.replace("%20", " ") + ": " + formattedData);
            } else {
                resultArea.setText("GET request failed with response code: " + responseCode + "\nURL: " + urlString);
                System.out.println("Failed URL: " + urlString);  // Print the URL to debug
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultArea.setText("An error occurred while fetching weather data.");
        }
    }

    private String formatWeatherData(String responseData) {
        JSONObject json = new JSONObject(responseData);
        String cityName = json.getString("name");
        JSONObject main = json.getJSONObject("main");
        double temp = main.getDouble("temp");
        int humidity = main.getInt("humidity");
        double tempMin = main.getDouble("temp_min");
        double tempMax = main.getDouble("temp_max");
        JSONObject weather = json.getJSONArray("weather").getJSONObject(0);
        String description = weather.getString("description");

        return String.format("City: %s\nTemperature: %.2f°C\nHumidity: %d%%\nMin Temperature: %.2f°C\nMax Temperature: %.2f°C\nDescription: %s",
                cityName, temp, humidity, tempMin, tempMax, description);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new WeatherForecast();
            }
        });
    }
}
