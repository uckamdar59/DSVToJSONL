package org.dsvtojsonlconverter;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;


class DSVToJSONLConverterTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void testSplitLine() {
        String line = "value1,value2,\"value,3\",value4";
        String delimiter = ",";
        String[] expectedResult = {"value1", "value2", "value,3", "value4"};

        String[] result = DSVToJSONLConverter.splitLine(line, delimiter);
        assertArrayEquals(expectedResult, result);
    }

    @Test
    void testSplitLineFail() {
        String line = "value1,value2,\"value,3\",value4";
        String delimiter = ",";
        String[] expectedResult = {"value1", "value2", "value3", "value4"};

        String[] result = DSVToJSONLConverter.splitLine(line, delimiter);
        assertNotEquals(expectedResult[2], result[2]);
    }

    @Test
    void testConvertToDate() throws Exception {
        JSONObject jsonObject = new JSONObject();
        String header = "date";
        String value = "01/02/2021";
        DSVToJSONLConverter.convertToDate(header, value, jsonObject);

        assertEquals("2021-02-01", jsonObject.get(header));
    }

    @Test
    void testIsDate() {
        String value = "01-02-2021";
        boolean result = DSVToJSONLConverter.isDate(value);
        assertEquals(true, result);
    }

    @Test
    void testIsDouble() {
        String value = "1.5";
        boolean result = DSVToJSONLConverter.isDouble(value);
        assertEquals(true, result);
    }

    @Test
    void testIsInteger() {
        String value = "100";
        boolean result = DSVToJSONLConverter.isInteger(value);
        assertEquals(true, result);
    }

    @Test
    void testIsDoubleFail() {
        String value = "1.5a";
        boolean result = DSVToJSONLConverter.isDouble(value);
        assertEquals(false, result);
    }

    @Test
    void testIsIntegerFail() {
        String value = "100a";
        boolean result = DSVToJSONLConverter.isInteger(value);
        assertEquals(false, result);
    }

    @Test
    void testMainMethodWithValidArguments() throws Exception {
        String baseDirectory = System.getProperty("user.dir");
        String inputFile = baseDirectory + "/src/test/resources/data/files/input.csv";
        String delimiter = ",";
        String outputFile = baseDirectory + "/src/test/resources/data/files/output.jsonl";
        String headerLine = "header1,header2,header3,header4\n";
        String line1 = "value1,value2,value3,value4\n";
        String line2 = "value5,value6,value7,value8\n";
        Files.write(Paths.get(inputFile), (headerLine + line1 + line2).getBytes());

        DSVToJSONLConverter.main(new String[]{inputFile, delimiter, outputFile});

        Path path = Paths.get(outputFile);
        String output = new String(Files.readAllBytes(path));
        String expectedOutput = "{\"header1\":\"value1\",\"header2\":\"value2\",\"header3\":\"value3\",\"header4\":\"value4\"}\n" +
                "{\"header1\":\"value5\",\"header2\":\"value6\",\"header3\":\"value7\",\"header4\":\"value8\"}\n";
        assertEquals(expectedOutput, output);
    }

    @Test
    void testMainMethodWithLessNumberOfArguments() throws Exception {
        DSVToJSONLConverter.main(new String[]{});
        assertEquals("Invalid number of arguments, please provide input file path, delimiter and output file path\n", outContent.toString());
    }

}
