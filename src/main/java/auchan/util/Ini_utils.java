package auchan.util;

import auchan.DBRobot;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static auchan.util.Log4J.Log;

public class Ini_utils {

    private static Ini ini;
    static String iniFileName = "Config.ini";

    //--------------------------------------------------------------------------------------
    public static void loadIni() {
        //---------------- LOAD INI FILE ------------------
        File file;

        if (!Files.exists(Paths.get(iniFileName))) {
            try (FileOutputStream outf = new FileOutputStream(iniFileName);
                 InputStream in = DBRobot.class.getClassLoader().getResourceAsStream(iniFileName)) {

                int readBytes;
                byte[] buffer = new byte[4096];
                while ((readBytes = in.read(buffer)) > 0) {
                    outf.write(buffer, 0, readBytes);
                }
            } catch (FileNotFoundException e) {
                Log.error("FileNotFoundException error (Configured ini file " + iniFileName + " not found in root directory)", e);
            } catch (IOException e) {
                Log.error("IOException error", e);
            } catch (Exception e) {
                Log.error("Exception error", e);
            }
        }
        file = new File(iniFileName);
        try {
            ini = new Ini(file);
        } catch (Exception e) {
            Log.error("Exception ", e);
        }
    }

    //--------------------------------------------------------------------------------------
    public static String getIniConfig(String key, String secName) {
        try {
            if (ini.containsKey(key)) {
                Profile.Section sec = ini.get(key);
                if (sec.containsKey(secName)) {
                    return sec.get(secName);
                } else {
                    throw new NullPointerException("No such parameter \"" + secName + "\" in \"" + key + "\" key");
                }
            } else {
                throw new NullPointerException("No such key \"" + key + "\"");
            }
        } catch (NullPointerException e) {
            Log.error("NullPointerException. ", e);
            System.exit(0);
            return null;
        } catch (Exception e) {
            Log.error("Exception. ", e);
            System.exit(0);
            return null;
        }
    }

    //--------------------------------------------------------------------------------------
    public static Ini getIni() {
        if (!ini.isEmpty()) {
            return ini;
        } else {
            Log.error("Ini file was not initialised and load");
            return null;
        }
    }
    //--------------------------------------------------------------------------------------
    public static Profile.Section getIniSection(String secName) {
        if (!ini.isEmpty()) {
            if (ini.containsKey(secName)) {
                return ini.get(secName);
            } else {
                Log.error("No such section: " + secName);
                return null;
            }
        } else {
            Log.error("Ini file was not initialised and load");
            return null;
        }
    }

    //--------------------------------------------------------------------------------------
    public static void setIniParam(String section, String param, String value) {
        Profile.Section sec = ini.get(section);
        sec.put(param, value);
        saveIniFile();
    }

    //--------------------------------------------------------------------------------------
    public static void setIniSec(String section) {
        if (!getIni().containsKey(section)) {
            ini.add(section);
        }
    }

    //--------------------------------------------------------------------------------------
    public static void saveIniFile() {
        try {
            ini.store();
        } catch (IOException e) {
            Log.error("IOException", e);
        } catch (NullPointerException e) {
            Log.error("NullPointerException", e);
        } catch (Exception e) {
            Log.error("Exception", e);
        }

    }

    //--------------------------------------------------------------------------------------
    public static Map<String, String> getIniSecAsMap(String section) {
        Map<String, String> storeMap = new TreeMap<>();
        Profile.Section sec = getIniSection(section);
        for (String key : Objects.requireNonNull(sec).keySet()) {
            storeMap.put(key, sec.get(key));
        }
        return storeMap;
    }

    //---------------------------------------------------------------------
    public static List<String> getIniArrayAsList(String key, String section) {
        return Arrays.asList(Objects.requireNonNull(getIniConfig(key, section)).split(","));
    }

    // ------------------- GET STORES FUNC---------------------------------
    public static int[] getIniArrayAsArray(String key, String value) {
        String stores = getIniConfig(key, value);
        String stores_split[] = Objects.requireNonNull(stores).split(",");
        int anumStores[] = new int[stores_split.length];

        for (int i = 0; i < stores_split.length; i++) {
            anumStores[i] = Integer.parseInt(stores_split[i]);
        }
        return anumStores;
    }

    //--------------------------------------------------------------------------------------
    public static void checkIniFile() {
        List<String> root = getResFileAsList(iniFileName);

        String section = "";
        for (String s : Objects.requireNonNull(root)) {
            if (s.equals("") || s.charAt(0) == ';') continue;
            if (s.charAt(0) == '[' && s.charAt(s.length() - 1) == ']') {
                section = s.substring(1, s.length() - 1);
                if (getIniSection(section) == null) {
                    setIniSec(section);
                }

                continue;
            }
            if (!Objects.requireNonNull(getIniSection(section)).containsKey(s.substring(0, s.indexOf('=')).trim())) {
                StringBuilder alertList = new StringBuilder();
                String key = s.substring(0, s.indexOf('=')).trim();
                String param = s.substring(s.indexOf('=') + 1, s.length()).trim();
                alertList.append("Parameter \"").append(key).append("\" was missing in section \"").append(section).append("\" in " + iniFileName + " and was added to it with default value = \"").append(param).append("\"\n");
                //Be careful with / characters. / = /// in file
                setIniParam(section, key, param);
                System.out.println(alertList.toString());
            }
        }
    }

    private static List<String> getResFileAsList(String path) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
             BufferedReader out = new BufferedReader(new InputStreamReader(in))
        ) {
            return out.lines().collect(Collectors.toList());
        } catch (IOException e) {
            Log.error("Exception:", e);
            return null;
        }
    }

}
