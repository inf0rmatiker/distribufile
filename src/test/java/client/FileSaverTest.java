package client;

import org.junit.jupiter.api.Test;

import client.FileSaver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class FileSaverTest {

    private static String getTestResourceDirectory() {
        String path = "src/test/resources";
        File file = new File(path);
        return file.getAbsolutePath();
    }

    @Test
    public void testConstructorFileIsADirectory() {
        String testDirectory = String.format("%s/%s", getTestResourceDirectory(), "test_directory");
        assertThrows(FileNotFoundException.class, () -> new FileSaver(testDirectory));
    }

    @Test
    public void testWriteSimple() {
        String testData = "This is a simple test";
        String testOutputFile = String.format("%s/%s", getTestResourceDirectory(), "filesaver_test.data");

        // Write data
        try {
            FileSaver fileSaver = new FileSaver(testOutputFile);
            fileSaver.writeChunk(testData.getBytes());
            fileSaver.close();
        } catch (FileNotFoundException e) {
            fail("Caught FileNotFoundException during write!");
        } catch (IOException e) {
            fail("Caught IOException!");
        }

        // Read data and assert match
        try {
            File testFile = new File(testOutputFile);
            String fileContents = "";
            Scanner scan = new Scanner(testFile);
            while (scan.hasNextLine()) {
                fileContents += scan.nextLine();
            }
            scan.close();
            assertEquals(testData, fileContents);

            // Clean up
            if (!testFile.delete()) {
                fail("Unable to delete test file " + testOutputFile);
            }
        } catch (FileNotFoundException e) {
            fail("Caught FileNotFoundException during read!");
        }
    }

    @Test
    public void testMultipleWritesSimple() {
        String testData1 = "This is the first chunk";
        String testData2 = "This is the second chunk";
        String testOutputFile = String.format("%s/%s", getTestResourceDirectory(), "filesaver_test.data");

        // Write data
        try {
            FileSaver fileSaver = new FileSaver(testOutputFile);
            fileSaver.writeChunk(testData1.getBytes());
            fileSaver.writeChunk(testData2.getBytes());
            fileSaver.close();
        } catch (FileNotFoundException e) {
            fail("Caught FileNotFoundException during write!");
        } catch (IOException e) {
            fail("Caught IOException!");
        }

        // Read data and assert match
        String expected = testData1 + testData2;
        try {
            File testFile = new File(testOutputFile);
            String fileContents = "";
            Scanner scan = new Scanner(testFile);
            while (scan.hasNextLine()) {
                fileContents += scan.nextLine();
            }
            scan.close();
            assertEquals(expected, fileContents);

            // Clean up
            if (!testFile.delete()) {
                fail("Unable to delete test file " + testOutputFile);
            }
        } catch (FileNotFoundException e) {
            fail("Caught FileNotFoundException during read!");
        }
    }

}
