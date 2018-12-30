package auchan.util;

import auchan.DBRobot;
import auchan.model.Data;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.format.CellFormatType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static auchan.DBRobot.date_BIEE;
import static auchan.util.FX_utils.showError;
import static auchan.util.Ini_utils.getIniConfig;
import static auchan.util.Ini_utils.iniFileName;
import static auchan.util.Log4J.Log;

public class Misc_utils {

    public static Map<String, String> replaceMap = new HashMap<>();
    private static Map<String, CellStyle> styleMap = new HashMap<>();

    //--------------------------------------------------------------------------------------
    public static Properties readProperties() {
        InputStream is = auchan.DBRobot.class.getResourceAsStream("/my.properties");
        Properties p = new Properties();
        try {
            p.load(is);
        } catch (Exception e) {
            Log.error("Exception error", e);
        }
        return p;
    }

    //--------------------------------------------------------------------------------------
    public static void checkNcopyResFile(String filename) {
        //---------------- LOAD INI FILE ------------------
        File file;
        if (!Files.exists(Paths.get(filename))) {
            try (FileOutputStream outf = new FileOutputStream(filename);
                 InputStream in = DBRobot.class.getClassLoader().getResourceAsStream(filename)) {

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
    }

    //----------------------------------------------------------------
    public static List<String> getResFileAsList(String path) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
             BufferedReader out = new BufferedReader(new InputStreamReader(in))
        ) {
            return out.lines().collect(Collectors.toList());
        } catch (IOException e) {
            Log.error("Exception:", e);
            return null;
        }
    }

    //----------------------------------------------------------------
    static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String value) {
        byte[] decodedValue = Base64.getDecoder().decode(value);  // Basic Base64 decoding
        return new String(decodedValue, StandardCharsets.UTF_8);
    }

    //-----------------------------------------------------------------
    public static String GetSQLfromFile(String filepath) {
        StringBuilder str = new StringBuilder();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filepath))) {
            for (String s : br.lines().collect(Collectors.toList())) {
                str.append(s).append(" ");
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            Log.error("IOException. File not accessible");
        }
        if (str.length() > 0) {
            //return ;
            String rs = str.toString();
            for (HashMap.Entry<String, String> entry : replaceMap.entrySet()) {
                if (entry.getValue() == null) {
                    Log.error("Value " + entry.getKey() + "cannot be replacedMap because it is NULL");
                    System.exit(0);
                }
                rs = rs.replace(entry.getKey(), entry.getValue());
            }
            return rs;
        } else {
            Log.error("The SQL file " + filepath + " is not found or not accessible");
            System.exit(0);
            return "Nothing";
        }
    }

    //---------------------------------------------------------------------
    public static void checkSQLFolder(List<String> fList) {
        //---------------- LOAD INI FILE ------------------
        if (fList.size() != 0) {
            File directory = new File("SQL");
            if (!directory.exists()) {
                directory.mkdir();
            }

            for (String fName : fList) {
                if (!Files.exists(Paths.get("SQL/" + fName).getParent())) {
                    try {
                        Files.createDirectories(Paths.get("SQL/" + fName).getParent());
                    } catch (IOException e) {
                        Log.error("IOException error", e);
                        System.exit(0);
                    }
                }
                if (!Files.exists(Paths.get("SQL/" + fName))) {
                    try (FileOutputStream outf = new FileOutputStream("SQL/" + fName);
                         InputStream in = auchan.DBRobot.class.getClassLoader().getResourceAsStream("SQL/" + fName)) {

                        int readBytes;
                        byte[] buffer = new byte[4096];
                        while ((readBytes = in.read(buffer)) > 0) {
                            outf.write(buffer, 0, readBytes);
                        }

                    } catch (IOException e) {
                        Log.error("IOException error", e);
                        System.exit(0);
                    } catch (Exception e) {
                        Log.error("Exception error", e);
                        System.exit(0);
                    }
                }
            }
        } else {
            Log.error("The input SQL resource file list is empty");
            System.exit(0);
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------
    private static void fillStyleMap(Workbook wb) {
        CellStyle cellStyleYellow = wb.createCellStyle();
        cellStyleYellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        cellStyleYellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle cellStyleRed = wb.createCellStyle();
        cellStyleRed.setFillForegroundColor(IndexedColors.RED.getIndex());
        cellStyleRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle cellStyleCoral = wb.createCellStyle();
        cellStyleCoral.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        cellStyleCoral.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle cellStyleOddCell = wb.createCellStyle();
        cellStyleOddCell.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        cellStyleOddCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle cellStyleHeader = wb.createCellStyle();
        cellStyleHeader.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        cellStyleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = wb.createFont();
        font.setBold(true);
        cellStyleHeader.setFont(font);

        styleMap.put("Yellow", cellStyleYellow);
        styleMap.put("Red", cellStyleRed);
        styleMap.put("Header", cellStyleHeader);
        styleMap.put("OddCell", cellStyleOddCell);
        styleMap.put("Coral", cellStyleCoral);
    }

    //----------------------------------------------------------------------------------------------------------------
    public static void save2Excel(String fileName, Map<String, List<Data>> excelMap) {

        if (excelMap.size() > 0) {
            Workbook wb = new XSSFWorkbook();
            fillStyleMap(wb);

            boolean writeToFile = false;
            try {
                Files.deleteIfExists(Paths.get(new File("").getAbsolutePath().substring(0, new File("").getAbsolutePath().length()) + "\\" + fileName));
                int tableNum = 0;
                for (String s : excelMap.keySet()) {
                    writeToFile = true;
                    tableNum++;
                    List<Data> tempList = new ArrayList<>(excelMap.get(s));
                    int maxCol = tempList.get(0).size();
                    int maxRow = tempList.size();
                    XSSFSheet sheet = (XSSFSheet) wb.createSheet(s);

                    Data td = tempList.get(0);
                    XSSFRow trow = sheet.createRow(0);
                    for (int j = 0; j < td.size(); j++) {
                        XSSFCell cell = trow.createCell(j);
                        cell.setCellValue(td.get(j).toString());
                        cell.setCellStyle(styleMap.get("Header"));
                    }

                    for (int i = 1; i < maxRow; i++) {
                        XSSFRow row = sheet.createRow(i);
                        Data d = tempList.get(i);
                        for (int j = 0; j < d.size(); j++) {
                            XSSFCell cell = row.createCell(j);
                            cell.setCellValue(d.get(j).toString());
                        }
                    }
                    sheet.setAutoFilter(new CellRangeAddress(0, maxRow - 1, 0, maxCol - 1));
                    // ------ Sheet customising ---------
                    for (int i = 0; i < maxCol; i++) {
                        sheet.autoSizeColumn(i);
                        sheet.setColumnWidth(i, (sheet.getColumnWidth(i) + 500 > 6000) ? 6000 : sheet.getColumnWidth(i) + 500);
                    }

                }
                if (writeToFile) {
                    FileOutputStream out = new FileOutputStream(fileName);
                    wb.write(out);
                    out.close();
                }
                wb.close();
            } catch (Exception e) {
                showError(e.getClass().getSimpleName(),e);
            }
        }
    }
//-----------------------------------------------------------------------------------------------------------------------------
}
