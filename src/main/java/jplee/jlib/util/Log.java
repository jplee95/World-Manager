package jplee.jlib.util;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.FMLLog;

public class Log {

	private static boolean showDefaultDebugLog = false;
	private static boolean showDebugLog = false;
	
	private Logger logger;
	private static Logger defaultLogger = FMLLog.getLogger();

	public static void defaultInfo(String message, Object...args) {
		defaultLogger.info(String.format(message, args));
	}
	
	public static void defaultInfo(String message) {
		defaultLogger.info(message);
	}

	public static void defaultWarning(String message, Object...args) {
		defaultLogger.warn(String.format(message, args));
	}

	public static void defaultWarning(String message) {
		defaultLogger.warn(message);
	}
	

	public static void defaultDebug(String message, Object...args) {
		if(defaultLogger.isDebugEnabled() && showDefaultDebugLog) {
			defaultLogger.debug(String.format(message, args));
		}
	}

	public static void defaultDebug(String message) {
		if(defaultLogger.isDebugEnabled() && showDefaultDebugLog) {
			defaultLogger.debug(message);
		}
	}
	
	public static void defaultError(String message, Object...args) {
		defaultLogger.error(String.format(message, args));
	}
	
	public static void defaultError(String message) {
		defaultLogger.error(message);
	}

	public void info(String message, Object...args) {
		if(logger != null)
			logger.info(String.format(message, args));
		else
			Log.defaultInfo(message, args);
	}
	
	public void info(String message) {
		if(logger != null)
			logger.info(message);
		else 
			Log.defaultInfo(message);
	}

	public void warning(String message, Object...args) {
		if(logger != null)
			logger.warn(String.format(message, args));
		else
			Log.defaultWarning(message, args);
	}

	public void warning(String message) {
		if(logger != null)
			logger.warn(message);
		else
			Log.defaultWarning(message);
	}
	

	public void debug(String message, Object...args) {
		if(logger != null) {
			if(logger.isDebugEnabled() && showDebugLog) {
				logger.debug(String.format(message, args));
			}
		} else
			Log.defaultDebug(message, args);
	}

	public void debug(String message) {
		if(logger != null) {
			if(logger.isDebugEnabled() && showDebugLog) {
				logger.debug(message);
			}
		} else
			Log.defaultDebug(message);
				
	}
	
	public void error(String message, Object...args) {
		if(logger != null)
			logger.error(String.format(message, args));
		else
			Log.defaultError(message, args);
	}
	
	public void error(String message) {
		if(logger != null)
			logger.error(message);
		else
			Log.defaultError(message);
	}

	public void attachLogger(Logger logger) {
		if(logger == null)
			this.logger = logger;
	}
	
	public static void showDebugLog(boolean show) {
		showDebugLog = show;
	}
	
	public static boolean isDebugLogShowing() {
		return showDebugLog;
	}
	
	public static void showDefaultDebugLog(boolean show) {
		showDefaultDebugLog = show;
	}
	
	public static boolean isDefaultDebugLogShowing() {
		return showDefaultDebugLog;
	}
}
