package auchan.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static auchan.util.Log4J.Log;

public class File_utils {

    //-----------------------------------------------------------------------------
    public static void writeArrayToFile(String fileName, List<String> list) {
        try {
            Files.write(Paths.get(fileName), list, Charset.defaultCharset());
        } catch (FileNotFoundException e) {
            Log.error("FileNotFoundException error", e);
        } catch (IOException e) {
            Log.error("IOException error", e);
        } catch (Exception e) {
            Log.error("Exception error", e);
        }
    }

    //-----------------------------------------------------------------------------
    public static List<String> readArrayFromFile(String fileName) {
        try (Stream<String> stream = Files.lines(Paths.get(fileName),Charset.defaultCharset())) {
            return stream.collect(Collectors.toList());
        } catch (FileNotFoundException e) {
            Log.error("FileNotFoundException error", e);
        } catch (IOException e) {
            Log.error("IOException error", e);
        } catch (Exception e) {
            Log.error("Exception error", e);
        }
        return null;
    }

    //-----------------------------------------------------------------------------
    public static void writeArrayToFile_old(String fileName, List<String> list) {
        File FileStatus;
        try {
            //Specify the file path here
            FileStatus = new File(fileName);
            if (!FileStatus.exists()) {
                FileStatus.createNewFile();
            }
            PrintWriter outFile = new PrintWriter(FileStatus);
            for (String s : list) {
                outFile.println(s);
            }
            outFile.close();
        } catch (FileNotFoundException e) {
            Log.error("FileNotFoundException error", e);
        } catch (IOException e) {
            Log.error("IOException error", e);
        } catch (Exception e) {
            Log.error("Exception error", e);
        }

    }
}
