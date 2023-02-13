package org.dsvtojsonlconverter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;


public class DSVToJSONLConverter {

    // Date formats to be used for parsing, as of now only these formats are supported
    final static SimpleDateFormat[] dateFormats =
            new SimpleDateFormat[]{new SimpleDateFormat("MM-dd-yyyy"),
                    new SimpleDateFormat("MM/dd/yyyy"),
                    new SimpleDateFormat("yyyy/MM/dd"),
                    new SimpleDateFormat("yyyy-MM-dd")};

    public static void main(String[] args) throws Exception {

        if(args.length != 3) {
            System.out.println("Invalid number of arguments, please provide input file path, delimiter and output file path");
            return;
        }

        // Parse command line arguments
        String inputFilePath = args[0];
        String delimiter = args[1];
        String delimiterInHeader = "\\" + delimiter;
        String outputFilePath = args[2];

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             FileWriter writer = new FileWriter(outputFilePath)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("Input file is empty");
                return;
            }

            List<String> headers = new ArrayList<>();
            try {
                headers = Arrays.stream(headerLine.split(delimiterInHeader)).collect(Collectors.toList());
            } catch (Exception e) {
                System.out.println("Invalid delimiter, please provide a valid delimiter");
                return;
            }
            String line;
            // Read input file line by line and convert to JSONL,
            while ((line = reader.readLine()) != null) {
                JSONObject jsonObject = new JSONObject();
                //setting the jsonObject map to a new LinkedHashMap so that order of keys remains intact
                Field map = jsonObject.getClass().getDeclaredField("map");
                map.setAccessible(true);
                map.set(jsonObject, new LinkedHashMap<>());
                map.setAccessible(false);

                String[] values = splitLine(line, delimiter);
                for (int i = 0; i < headers.size(); i++) {
                    String header = headers.get(i);
                    String value = i < values.length ? values[i] : null;
                    if (value == null || value.isEmpty()) {
                        continue;
                    }

                    try {
                        if (!isDate(value)) {
                            if (isInteger(value))
                                jsonObject.put(header, Integer.parseInt(value));
                            else if (isDouble(value))
                                jsonObject.put(header, Double.parseDouble(value));
                            else
                                jsonObject.put(header, value);
                        } else {
                            convertToDate(header, value, jsonObject);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                writer.write(jsonObject.toString());
                writer.write("\n");
            }

            System.out.println("File converted successfully!!!\uD83D\uDE01");
        } catch (IOException e) {
            System.out.println("An error occurred while reading or writing the file: " + e.getMessage());
        }

    }

    //converting the date to yyyy-MM-dd format
    public static void convertToDate(String header, String value, JSONObject jsonObject) {
        if (value.contains("/")) {
            value = value.replace("/", "-");
        }
        if (value.split("-").length == 3) {
            String[] date = value.split("-");
            if (date[2].length() == 4) {
                jsonObject.put(header, date[2] + "-" + date[1] + "-" + date[0]);
            } else {
                jsonObject.put(header, date[0] + "-" + date[1] + "-" + date[2]);
            }
        }
    }

    //splitting the line based on delimiter, also taking care of quotes
    public static String[] splitLine(String line, String delimiter) {
        List<String> result = new ArrayList<>();
        boolean insideQuotes = false;
        StringBuilder currentToken = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);
            if (currentChar == '"') {
                insideQuotes = !insideQuotes;
            } else if (String.valueOf(currentChar).equals(delimiter) && !insideQuotes) {
                result.add(currentToken.toString());
                currentToken = new StringBuilder();
            } else {
                currentToken.append(currentChar);
            }
        }
        result.add(currentToken.toString());

        //convert list to array
        return result.toArray(new String[0]);
    }

    //checking if the value is a date, for now only checking for 4 formats
    public static boolean isDate(String dateToValidate) {
        for (int j = 0; j < dateFormats.length; j++) {
            try {
                dateFormats[j].parse(dateToValidate);
                return true;
            } catch (ParseException e) {
                continue;
            }
        }
        return false;
    }

    //checking if the value is an integer
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    //checking if the value is a double
    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}

