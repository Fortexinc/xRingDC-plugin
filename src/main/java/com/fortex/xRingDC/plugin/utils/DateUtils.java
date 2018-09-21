package com.fortex.xRingDC.plugin.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
	public static String convertToUTC(String localDateStr) {
		String result = null;
		SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
		formater.setTimeZone(TimeZone.getDefault());
		try {
			Date localDate = formater.parse(localDateStr);
			formater.setTimeZone(TimeZone.getTimeZone("UTC"));
			result = formater.format(localDate);
		} catch (ParseException e) {}
		return result;
	}
	
}
