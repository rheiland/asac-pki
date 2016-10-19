package base;
import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * This class is used to log simulations to log files. The log level is controlled in this class.
 *
 */
public class SimLogger {
//	public final static Level LOGLEVEL = Level.ERROR;    // Log.e  (see below: Android-like)
//	public final static Level LOGLEVEL = Level.WARNING;  // Log.w
//	public final static Level LOGLEVEL = Level.INFO;     // Log.i
//	public final static Level LOGLEVEL = Level.FINE;     // Log.???
//	public final static Level LOGLEVEL = Level.FINER;    // Log.d
	public final static Level LOGLEVEL = Level.FINEST;   // Log.v

	private static SimLogger handle;
	private Logger logger;
	private FileHandler logFile;
	private Formatter format;

	/**
	 * This method do not need to be called. All logging activities can be done with
	 * static SimLogger.log method.
	 * @return the handle of this logger object
	 */
	private synchronized static SimLogger getHandle() {
		if(handle == null) {
			handle = new SimLogger();
		}
		return handle;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			logFile.flush();
			logFile.close();
		} catch(Exception e) {

		}
		super.finalize();
	}

	/**
	 * Do not call this method.
	 * @deprecated
	 */
	public SimLogger() {
		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		logger.setLevel(LOGLEVEL);
		logger.setUseParentHandlers(false);
		try {
			String filename = "Logging.txt";
			int i = 0;
			while(i < 100) {
				File file = new File(filename);
				if(file.exists()) {
					i++;
					filename = "Logging" + i + ".txt";
				} else break;
			}
			java.lang.System.out.println("Writing to log file " + filename);
			logFile = new FileHandler(filename);
			format = new SimpleFormatter();
			logFile.setFormatter(format);
			logger.addHandler(logFile);

			logger.log(Level.INFO, "New Logging Service Started");
		} catch(SecurityException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes to the log file and flush.
	 * @param logLevel the log level of the message. Any logs that's finer than the
	 * constant LOGLEVEL will be discarded.
	 * @param msg the actual log message to be written.
	 */
	private void logPrivate(Level logLevel, String msg) {
		logger.log(logLevel, msg);
		logFile.flush();
	}

	/**
	 * Simply delegates the parameters to handle.logPrivate()
	 * if handle do not exist, getHandle() will create it automatically.
	 * @param logLevel the log level of the message. Any logs that's finer than the
	 * constant LOGLEVEL will be discarded.
	 * @param msg the actual log message to be written.
	 */
	public static synchronized void log(Level logLevel, String msg) {
		SimLogger slog = getHandle();
		slog.logPrivate(logLevel, msg);
	}

	/**
	 * An adapter for Android-like logging
	 */
	public static class Log {
		public static void e(String message) {
			SimLogger.log(Level.SEVERE, message);
		}

		public static void w(String message) {
			SimLogger.log(Level.WARNING, message);
		}

		public static void i(String message) {
			SimLogger.log(Level.INFO, message);
		}

		public static void d(String message) {
			SimLogger.log(Level.FINER, message);
		}

		public static void v(String message) {
			SimLogger.log(Level.FINEST, message);
		}
	}
}
