package auchan;

import auchan.util.Log4J;
import auchan.view.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;

import static auchan.util.FX_utils.showError;
import static auchan.util.FX_utils.userDialog;
import static auchan.util.Ini_utils.*;
import static auchan.util.Log4J.Log;
import static auchan.util.Log4J.createLogger;


public class DBRobot extends Application {

    private Stage primaryStage;
    public static BorderPane rootLayout;
    private static Properties p;
    public static boolean isAdmin = false;

    private static LocalDateTime curr_day = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    public static String date_BIEE;

    private static void staticVariablesInit() {
        createLogger();
        loadIni();
        checkIniFile();
        Log4J.createLogger(Boolean.valueOf(getIniConfig("ROOT", "log")));

        File directory = new File("SQL");
        if (!directory.exists()) {
            directory.mkdir();
        }

        date_BIEE = String.format("%02d", curr_day.getDayOfMonth()) + "."
                + String.format("%02d", curr_day.getMonthValue()) + "."
                + String.format("%04d", curr_day.getYear());
        p = readProperties();
    }

    @Override
    public void start(Stage primaryStage) {
        staticVariablesInit();

        String user_res = userDialog(false);

        if (user_res.equals("Reopen")) {
            while (user_res.equals("Reopen")) {
                user_res = userDialog(true);
            }
        }

        if (user_res.equals("Admin")) {
            isAdmin = true;
        }
        if (user_res.equals("User")) {
            isAdmin = false;
        }
        if (user_res.equals("Close")) {
            Platform.exit();
        }

        this.primaryStage = primaryStage;
        if (!isAdmin) this.primaryStage.setResizable(false);
        this.primaryStage.setTitle("DB2Excel v" + p.getProperty("version"));
        this.primaryStage.getIcons().add(new Image("images/favicon.png"));
        initRootLayout();
        showMainOverview();
    }

    private void initRootLayout() {
        try {
            // Загружаем корневой макет из fxml файла.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(DBRobot.class.getResource("/view/root.fxml")); //PROD
//            loader.setLocation(new URL("file://" + System.getProperty("user.dir") + "/src/main/java/auchan/view/root.fxml")); //DEV
            rootLayout = loader.load();

            // Отображаем сцену, содержащую корневой макет.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            Log.error(e);
        }
    }

    private void showMainOverview() {

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(DBRobot.class.getResource("/view/main.fxml")); //PROD
//            loader.setLocation(new URL("file://" + System.getProperty("user.dir") + "/src/main/java/auchan/view/main.fxml")); //DEV
            AnchorPane mainOverview = loader.load();
            rootLayout.setCenter(mainOverview);

            // Даём контроллеру доступ к главному приложению.
            MainController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            Log.error(e);
        }
    }
    //___________________ READ MAVEN PROPERTIES ___________________

    private static Properties readProperties() {
        InputStream is = DBRobot.class.getResourceAsStream("/my.properties");
        Properties p = new Properties();
        try {
            p.load(is);
        } catch (Exception e) {
            Log.error("Exception error", e);
            showError("Exception :", e);
        }
        return p;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
