package logger;

import java.io.IOException;
import java.util.logging.*;

public class MeshLogger {

    public enum LogHandler{
        FILE_HANDLER,
        CONSOLE_HANDLER;
    }

    private Logger logger;

    public MeshLogger(String className, LogHandler handler){
        this.logger = Logger.getLogger(className);
        prepareLogger(handler);
    }

    private void prepareLogger(LogHandler handlerType){
        Handler handlerInstance = null;
        logger.setUseParentHandlers(false);
        if(handlerType.equals(LogHandler.CONSOLE_HANDLER)){
            handlerInstance = new ConsoleHandler();
            handlerInstance.setFormatter(new MeshFormatter());
            logger.addHandler(handlerInstance);
        }
        else if(handlerType.equals(LogHandler.FILE_HANDLER)){
            try {
                handlerInstance = new FileHandler("./mesh_log.log"); //, 50000, 10
                handlerInstance.setFormatter(new MeshFormatter());
                logger.addHandler(handlerInstance);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.setLevel(Level.FINE);
    }

    public MeshLogger setLevel(Level level){
        logger.setLevel(level);
        return this;
    }

    public void log(Level level, String text){
        logger.log(level, text);
    }

    public void log(String text){
        logger.log(new LogRecord(logger.getLevel(), text));
    }
}
