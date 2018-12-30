package auchan.view;

import auchan.DBRobot;
import auchan.model.Data;
import auchan.util.SQLThread;
import edu.emory.mathcs.backport.java.util.Arrays;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import org.codehaus.plexus.util.StringUtils;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static auchan.DBRobot.isAdmin;
import static auchan.DBRobot.rootLayout;
import static auchan.util.FX_utils.*;
import static auchan.util.File_utils.readArrayFromFile;
import static auchan.util.File_utils.writeArrayToFile;
import static auchan.util.Ini_utils.*;
import static auchan.util.Log4J.Log;
import static auchan.util.Misc_utils.decode;
import static auchan.util.Misc_utils.save2Excel;


public class MainController {

    private DBRobot mainApp;
    static Map<Integer, Data> excelMap = new TreeMap<>();

    @FXML
    private ProgressBar progressBar;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextArea textArea;
    @FXML
    private Button SQL2Excel;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;

    @FXML
    private ComboBox<String> comboDB;
    @FXML
    private ComboBox<String> comboProfile;
    @FXML
    private ComboBox<String> comboStore;

    @FXML
    private void initialize() {

//        comboBox.setItems(FXCollections.observableList(new ArrayList<>(getIniSection("STORES_METI").values())));

        if (!isAdmin) {
            textArea.setVisible(false);
            textArea.setDisable(true);
            saveButton.setDisable(true);
            deleteButton.setDisable(true);
            comboDB.setDisable(true);
            anchorPane.setMaxWidth(200);
            anchorPane.setMaxHeight(490);
        }

        Ini ini = getIni();
        List<String> DB = new ArrayList<>();
        List<String> profiles = new ArrayList<>();

        for (String secName : ini.keySet()) {
            if (secName.startsWith("CON")) {
                DB.add(secName.substring(4, secName.length()));
            }
            if (secName.startsWith("PROFILE_")) {
                profiles.add(secName.substring(8, secName.length()));
            }
        }

        comboProfile.setItems(FXCollections.observableList(profiles));
        comboDB.setItems(FXCollections.observableList(DB));

    }

    @FXML
    private void onComboDBchange() {
//        showAlert("",comboDB.getSelectionModel().getSelectedItem(), Alert.AlertType.INFORMATION);

        List<String> stores = new ArrayList<>();

        Profile.Section sec = getIniSection(comboDB.getSelectionModel().getSelectedItem() + "_STORES");
        for (String s : sec.keySet()) {
            stores.add(s);
        }

        comboStore.setItems(FXCollections.observableList(stores));
    }

    @FXML
    private void onComboProfileChange() {
//        showAlert("",comboDB.getSelectionModel().getSelectedItem(), Alert.AlertType.INFORMATION);

        String prof = comboProfile.getSelectionModel().getSelectedItem();
        if (StringUtils.isNotEmpty(prof)) {
            comboDB.getSelectionModel().select(getIniConfig("PROFILE_" + prof, "DB"));
            comboStore.getSelectionModel().select(getIniConfig("PROFILE_" + prof, "STORE"));
            textArea.clear();
            if (Files.exists(Paths.get("SQL/" + prof + ".sql"))) {
                textArea.setText(StringUtils.join(readArrayFromFile("SQL/" + prof + ".sql").toArray(), "\n"));

            }

        }
    }

    @FXML
    private void onSaveProfile() {
        if (StringUtils.isNotEmpty(comboStore.getSelectionModel().getSelectedItem()) &&
                StringUtils.isNotEmpty(comboDB.getSelectionModel().getSelectedItem())
        ) {
            String profile = getTextInputDialog(comboProfile.getSelectionModel().getSelectedItem(),
                    "New profile",
                    "Enter your new profile name:");
            if (StringUtils.isNotEmpty(profile)) {
                if (!getIni().containsKey("PROFILE_" + profile)) {
                    comboProfile.getItems().add(profile);
                }
                setIniSec("PROFILE_" + profile);
                setIniParam("PROFILE_" + profile, "DB", comboDB.getSelectionModel().getSelectedItem());
                setIniParam("PROFILE_" + profile, "STORE", comboStore.getSelectionModel().getSelectedItem());
                writeArrayToFile("SQL/" + profile + ".sql", Arrays.asList(textArea.getText().split("\n")));
                //---------------------------------
                comboProfile.getSelectionModel().select(profile);

            }
        } else {
            showAlert("Profile creation error", "Database and Store fields shouldn't be empty", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onDeleteProfile() {
        String profile = comboProfile.getSelectionModel().getSelectedItem();
        if (StringUtils.isNotEmpty(profile)) {
            if (getIni().containsKey("PROFILE_" + profile)) {
                if (showAlertConf("Delete profile", "Are you really want to delete profile \"" + profile + "\"?", Alert.AlertType.CONFIRMATION) == ButtonType.OK) {
                    getIni().remove("PROFILE_" + profile);
                    saveIniFile();
                    textArea.clear();
                    comboProfile.getItems().remove(profile);
                }
            } else {
                showAlert("Profile deletion error", "No such profile: " + profile, Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Profile deletion error", "Selected profile shouldn't be empty ", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onSQL2Excel() {
        String selectedDB = comboDB.getSelectionModel().getSelectedItem();
        String selectedStore = comboStore.getSelectionModel().getSelectedItem();
        if (StringUtils.isNotEmpty(selectedDB) && StringUtils.isNotEmpty(selectedStore)) {
            String conn_sel = "CON_" + selectedDB;
            String store_sel = selectedDB + "_STORES";
            final String driver = getIniConfig(conn_sel, "Driver");
            final String user = getIniConfig(conn_sel, "Name");
            final String pass = decode(getIniConfig(conn_sel, "Pass"));
            if (selectedDB.contains("METI")) {
                new Thread(() -> {
                    showProgress(true);
                    final String url = getIniConfig(conn_sel, "Url");
                    Map<String, List<Data>> excelMap = new TreeMap<>();
                    int queryCount = 1;
                    List<String> textList = new ArrayList<>(getSqlList(textArea.getText()));
                    double progressAddValue = 1.0/textList.size();
                    progressBar.setProgress(0);
                    for (String sql : textList) {
                        if (StringUtils.isNotEmpty(sql)) {
                            SQLThread SQLThread_1 = new SQLThread(sql, url, user, pass);
                            final String alterSchemaSql = "ALTER SESSION SET CURRENT_SCHEMA=" + getIniConfig(store_sel, selectedStore);
                            SQLThread_1.setSchemaSql(alterSchemaSql);
                            try {
                                Class.forName(driver);

                                SQLThread_1.start();
                                SQLThread_1.join();

                                excelMap.put("Query_" + queryCount, SQLThread_1.getDataFromRs());
                            } catch (InterruptedException e) {
                                Log.error("InterruptedException error", e);
                                Platform.runLater(() -> showError("InterruptedException ERROR",e));
                                SQL2Excel.setDisable(false);
                            } catch (ClassNotFoundException e) {
                                Log.error("ClassNotFoundException error", e);
                                Platform.runLater(() -> showError("ClassNotFoundException ERROR",e));
                                SQL2Excel.setDisable(false);
                            } finally {
                                SQLThread_1.CloseConnection();
                            }
                        }
                        queryCount += 1;
                        progressBar.setProgress(progressBar.getProgress() + progressAddValue);
                    }
                    showProgress(false);
                    save2Excel(selectedStore + ".xlsx", excelMap);
                    Platform.runLater(()->showAlert("Great news", "Extraction save in " + selectedStore + ".xlsx", Alert.AlertType.INFORMATION));
                }).start();
            } else if (selectedDB.contains("GIMA")) {
                new Thread(() -> {
                    showProgress(true);
                    Map<String, List<Data>> excelMap = new TreeMap<>();
                    int queryCount = 1;
                    final String url = getIniConfig(store_sel, selectedStore);
                    List<String> textList = new ArrayList<>(getSqlList(textArea.getText()));
                    double progressAddValue = 1.0/textList.size();
                    progressBar.setProgress(0);
                    for (String sql : textList) {
                        if (StringUtils.isNotEmpty(sql)) {
                            SQLThread SQLThread_1 = new SQLThread(sql, url, user, pass);
                            try {
                                Class.forName(driver);

                                SQLThread_1.start();
                                SQLThread_1.join();

                                excelMap.put("Query_" + queryCount, SQLThread_1.getDataFromRs());
                            } catch (InterruptedException e) {
                                Log.error("InterruptedException error", e);
                                Platform.runLater(() -> showError("InterruptedException ERROR",e));
                                SQL2Excel.setDisable(false);
                            } catch (ClassNotFoundException e) {
                                Log.error("ClassNotFoundException error", e);
                                Platform.runLater(() -> showError("ClassNotFoundException ERROR",e));
                                SQL2Excel.setDisable(false);
                            } finally {
                                SQLThread_1.CloseConnection();
                            }
                        }
                        queryCount += 1;
                        progressBar.setProgress(progressBar.getProgress() + progressAddValue);
                    }
//                    SQL2Excel.setDisable(false);
                    showProgress(false);
                    save2Excel(selectedStore + ".xlsx", excelMap);
                    Platform.runLater(()->showAlert("Great news", "Extraction save in " + selectedStore + ".xlsx", Alert.AlertType.INFORMATION));
                }).start();
            } else {
                final String url = getIniConfig(conn_sel, "Url");
                new Thread(() -> {
                    showProgress(true);
                    Map<String, List<Data>> excelMap = new TreeMap<>();
                    int queryCount = 1;
                    List<String> sqlList = new ArrayList<>(getSqlList(textArea.getText()));
                    double progressAddValue = 1.0/sqlList.size();
                    progressBar.setProgress(0);
                    for (String sql : sqlList) {
                        if (StringUtils.isNotEmpty(sql)) {
                            SQLThread SQLThread_1 = new SQLThread(sql, url, user, pass);
                            try {
                                Class.forName(driver);

                                SQLThread_1.start();
                                SQLThread_1.join();

                                excelMap.put("Query_" + queryCount, SQLThread_1.getDataFromRs());
                            } catch (InterruptedException e) {
                                Log.error("InterruptedException error", e);
                                Platform.runLater(() -> showError("InterruptedException ERROR",e));
                                SQL2Excel.setDisable(false);
                            } catch (ClassNotFoundException e) {
                                Log.error("ClassNotFoundException error", e);
                                Platform.runLater(() -> showError("ClassNotFoundException ERROR",e));
                                SQL2Excel.setDisable(false);
                            } finally {
                                SQLThread_1.CloseConnection();
                            }
                        }
                        queryCount += 1;
                        progressBar.setProgress(progressBar.getProgress() + progressAddValue);
                    }
                    showProgress(false);
                    save2Excel(selectedStore + ".xlsx", excelMap);
                    Platform.runLater(()->showAlert("Great news", "Extraction save in " + selectedStore + ".xlsx", Alert.AlertType.INFORMATION));
                }).start();
            }
        } else {
            showAlert("Selection error", "No database selected", Alert.AlertType.ERROR);
        }
    }

    public void setMainApp(DBRobot mainApp) {
        this.mainApp = mainApp;
    }

    private static void print(String s) {
        System.out.println(s);
    }

    private List<String> getSqlList(String areaText) {

        List<String> l = new ArrayList<>(Arrays.asList(areaText.split("\n")));

        int it = 0;
        while (it < l.size()) {
            if (l.get(it).contains("--")) {
                l.remove(it);
            } else {
                it++;
            }

        }

        String s = String.join("\n",l).replace("\n"," ");
        return java.util.Arrays.stream(s.split(";")).collect(Collectors.toList());
    }

    private void showProgress(boolean ch){
        Platform.runLater(() -> {
            SQL2Excel.setDisable(ch);
            progressBar.setVisible(ch);
            progressIndicator.setVisible(ch);
        });

    }
}
