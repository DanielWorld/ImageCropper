package com.danielpark.imagecropper;

/**
 * Utility for String
 * <br><br>
 * Copyright (c) 2014-2016 daniel@bapul.net
 * Created by Daniel Park on 2016-01-23.
 */
public class StringUtil {

	/**
	 If parameter String is null or length == 0 or length without empty space == 0 then return true
	 * @param str
	 * @return
	 */
	protected static boolean isNullorEmpty(String str){
		try {
			if (str == null || str.length() == 0)
				return true;

			if (str.trim().length() == 0)
				return true;

			return false;
		}catch (Exception e){
			return true;
		}
	}
}
