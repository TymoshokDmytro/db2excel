package auchan.util;

import auchan.model.Data;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.codehaus.plexus.util.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static auchan.util.FX_utils.showAlert;
import static auchan.util.FX_utils.showError;
import static auchan.util.Log4J.Log;

/* ---------------- Useage of class ---------------
// Need the ojdbc6.jar in modules connected
final String sql = "select * from test_data";
        SQLThread SQLThread_1  = new SQLThread();
        String postgreClassName = "org.postgresql.Driver";

        String url = "jdbc:postgresql://localhost:5432/test";
        String user = "me";
        String pass = "123";

        try {
            Class.forName(postgreClassName);
            SQLThread_1.SetParams(sql, url, user, pass);
            SQLThread_1.start();
            SQLThread_1.join();

        } catch (InterruptedException e) {
            Log.error("InterruptedException error",e);
        } catch (ClassNotFoundException e) {
            Log.error("ClassNotFoundException error", e);
        }

        SQLThread_1.getResult();
        SQLThread_1.CloseConnection();
---------------------------------------------------*/

public class SQLThread extends Thread {
    private ResultSet result;
    private String url;
    private String driver;
    private String name;
    private String password;
    private String sql;
    private String schemaSql;
    private Connection conn;

    public SQLThread(String sql, String url, String name, String password) {
        this.sql = sql;
        this.url = url;
        this.name = name;
        this.password = password;
    }

    public SQLThread(String sql, String driver, String url, String name, String password,String threadName) {
        this(sql, url, name, password);
        this.driver = driver;
        this.setName(threadName);
    }

    public SQLThread(String sql, String url, String name, String password, String threadName) {
        this(sql, url, name, password);
        this.setName(threadName);
    }

    private SQLThread() {
        this(null, null, null, null);
    }

    private void setParams(String sql, String url, String name, String password) {
        this.url = url;
        this.name = name;
        this.password = password;
        this.sql = sql;
    }


    public void setParams(String sql, String driver, String url, String name, String password) {
        setParams(sql,url,name,password);
        this.driver = driver;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setSchemaSql(String schemaSql) {
        this.schemaSql = schemaSql;
    }

    public void CloseConnection() {
        try {
            if (conn != null) conn.close();
            if (result != null) result.close();
            //System.out.println("Connection "+this.getName()+" closed");
        } catch (SQLException e) {
            Log.error("Connection " + this.getName() + " already closed");
        }
    }

    public ResultSet getResult() {
        return result;
    }

    @Override
    public void run() {
        try {
            conn = DriverManager.getConnection(url, name, password);
            System.out.println("Connection established for " + this.getName() + " thread");
            PreparedStatement preparedStatement;
            if (StringUtils.isNotEmpty(this.schemaSql)) {
                preparedStatement = conn.prepareStatement(schemaSql);
                preparedStatement.executeQuery();
                setSchemaSql(null);
            }
            preparedStatement = conn.prepareStatement(sql);
            result = preparedStatement.executeQuery();
            System.out.println("Done " + this.getName() + " thread");
        }   catch (SQLException e) {
            Log.error("Thread " +this.getName() + " ERROR:", e);
            Platform.runLater(() -> showError("SQLException ERROR in thread "+this.getName(),e));
            this.CloseConnection();
        } catch (Exception e) {
            Log.error("Exception " + this.getName() + " ", e);
            Platform.runLater(() -> showError("Exception ERROR in thread "+this.getName(), e));
            this.CloseConnection();
        }
    }
    //----------------------------------------------------------------
    public List<Data> getDataFromRs() {
        List<Data> data = new ArrayList<>();
        ResultSet rs = this.result;
        try {
            if (rs != null) {
                ResultSetMetaData metaData = rs.getMetaData();
                Data d = new Data(metaData.getColumnCount());
                for (int i = 0; i < metaData.getColumnCount(); i++) {

                    d.set(i, metaData.getColumnName(i + 1));
                }
                data.add(d);
                if (rs.next()) {
                    do {
                        d = new Data(metaData.getColumnCount());
                        for (int i = 0; i < metaData.getColumnCount(); i++) {
                            d.set(i, rs.getObject(i + 1));
                        }
                        data.add(d);
                    } while (rs.next());
                }
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            Log.error("SQLException error", e);
        }
        return data;
    }

    //----------------------------------------------------------------
    public List<Data> executeSQLAndClose() {
        SQLThread SQLThread_1 = new SQLThread();
        List<Data> data = new ArrayList<>();
        try {
            Class.forName(this.driver);
            SQLThread_1.start();
            SQLThread_1.join();

            ResultSet rs = SQLThread_1.getResult();
            if (rs != null) {
                rs.next();
                ResultSetMetaData metaData = rs.getMetaData();
                Data d = new Data(metaData.getColumnCount());
                for (int i = 0; i < metaData.getColumnCount(); i++) {

                    d.set(i, metaData.getColumnName(i + 1));
                }
                data.add(d);
                do {
                    d = new Data(metaData.getColumnCount());
                    for (int i = 0; i < metaData.getColumnCount(); i++) {
                        d.set(i, rs.getObject(i + 1));
                    }
                    data.add(d);
                } while (rs.next());
            }
            if (rs != null) {
                rs.close();
            }

        } catch (InterruptedException e) {
            Log.error("InterruptedException error", e);
        } catch (ClassNotFoundException e) {
            Log.error("ClassNotFoundException error", e);
        } catch (SQLException e) {
            Log.error("SQLException error", e);
        } finally {
            SQLThread_1.CloseConnection();
        }
        return data;
    }
    //---------------------------------------------------------------
    public HashMap<Object, Object> getMapFromRS(int keyColumn, int valueColumn) throws SQLException {
        HashMap<Object, Object> res = new HashMap<>();
        if (result.next()) {
            do {
                res.put(result.getObject(keyColumn), result.getObject(valueColumn));
            } while (result.next());
            return res;

        } else {
            throw new SQLException("ResultSet is empty");
        }
    }
}

