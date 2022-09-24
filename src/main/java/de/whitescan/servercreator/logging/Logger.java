package de.whitescan.servercreator.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 
 * @author Whitescan
 *
 */
public class Logger {

	public static final DateTimeFormatter GLOBAL_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss");

	public static void info(String message) {
		System.out.println(getFormat("INFO", message));
	}

	private static String getFormat(String level, String message) {
		return "[" + LocalDateTime.now().format(GLOBAL_FORMAT) + " " + level + "]: " + message;
	}

}
