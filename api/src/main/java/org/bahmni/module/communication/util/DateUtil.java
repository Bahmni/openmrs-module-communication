package org.bahmni.module.communication.util;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
	
	public enum DateFormatType {
		UTC("yyyy-MM-dd'T'HH:mm:ss.SSS");
		
		private final String dateFormat;
		
		DateFormatType(String dateFormat) {
			this.dateFormat = dateFormat;
		}
		
		public String getDateFormat() {
			return dateFormat;
		}
	}
	
	public static String convertUTCToGivenFormat(Date dateTime, String format, String timeZone) {
		if (dateTime == null || StringUtils.isEmpty(format) || StringUtils.isEmpty(timeZone)) {
			return null;
		}
		DateFormat givenFormat = new SimpleDateFormat(format);
		TimeZone givenTimeZone = TimeZone.getTimeZone(timeZone);
		givenFormat.setTimeZone(givenTimeZone);
		return givenFormat.format(dateTime);
	}
}
