package auchan.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Log4J {

    private LoggerConfig loggerConfig;
    private Configuration config;
    private Appender appender;
    private ConsoleAppender CAppender;
    private LoggerContext ctx;
    private static final String pattern = "[%d{yyyy.MM.dd HH:mm:ss}] [%level]  %msg%n"; //%c{1}:%L
    private static final String LoggerName = "Logger";
    private static final Level LVL = Level.DEBUG;
    public static Logger Log ;
    private static final String extension = ".txt";
    /** Program Usage:
     *
     import static MVC.util.Log4J.Log;
     Log4J.createLogger(true);
     Log.info("=================== PROGRAM START ===================");
     */
    public static void createLogger() {

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        final String filename = "log/java_"+dateFormat.format(date)+extension;

        new Log4J(filename,pattern,LoggerName,LVL,false);
        Log = LogManager.getLogger(LoggerName);
        //Log.info("Logger created: simple Logger");
    }

    public static void createLogger(boolean writeFile) {

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        final String filename = "log/java_"+dateFormat.format(date)+extension;

        new Log4J(filename,pattern,LoggerName,LVL,writeFile);
        Log = LogManager.getLogger(LoggerName);
        //Log.info("Logger created: write file = "+ filename);
    }

    public static void createLogger(String strDate,boolean writeFile) {

        final String filename = "log/java_"+strDate+extension;
        new Log4J(filename,pattern,LoggerName,LVL,writeFile);
        Log = LogManager.getLogger(LoggerName);
        //Log.info("Logger created: write file "+ filename+" | "+strDate);
    }

    private Log4J(String filename,String pattern,String LoggerName,Level LVL,boolean writeFile) {

        this.ctx = (LoggerContext) LogManager.getContext(false);
        if (ctx.getConfiguration().getLoggers().size() > 0) {
            Map<String, LoggerConfig> map = ctx.getConfiguration().getLoggers();
            for (String s : map.keySet())
                ctx.getConfiguration().removeLogger(s);
        }
        this.config = this.ctx.getConfiguration();
        AppenderRef[] refs;

        PatternLayout layout = PatternLayout.newBuilder()
                .withConfiguration(this.config)
                .withPattern(pattern)
                .build();

        this.CAppender = ConsoleAppender.newBuilder()
                .setConfiguration(config)
                .withName("programmaticConsoleAppender")
                .withLayout(layout)
                .build();
        this.CAppender.start();
        this.config.addAppender(CAppender);
        AppenderRef ref1 = AppenderRef.createAppenderRef("programmaticConsoleAppender", null, null);


        if (writeFile) {
            this.appender = FileAppender.newBuilder()
                    .setConfiguration(config)
                    .withName("programmaticFileAppender")
                    .withLayout(layout)
                    .withFileName(filename)
                    .build();
            this.appender.start();
            this.config.addAppender(appender);
            AppenderRef ref = AppenderRef.createAppenderRef("programmaticFileAppender", null, null);
            refs = new AppenderRef[]{ref,ref1};
        }
        else { refs = new AppenderRef[]{ref1}; }

        this.loggerConfig = LoggerConfig
                .createLogger(false, LVL, LoggerName, "true", refs, null, config, null);
        if (writeFile) this.loggerConfig.addAppender(this.appender, null, null);
        this.loggerConfig.addAppender(this.CAppender, null, null);
        this.config.addLogger("Logger", this.loggerConfig);
        this.ctx.updateLoggers();
    }
}
