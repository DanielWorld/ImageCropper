package com.danielpark.imagecroppersample.util;

/**
 * String 관련 유틸리티
 * <br><br>
 * Copyright (c) 2014-2016 daniel@bapul.net
 * Created by Daniel Park on 2016-01-23.
 */
public class StringUtil {

	/**
	 * 해당 str 이 null 또는 length 가 0 또는 빈 공간을 제외한 length 가 0 일 경우 true 리턴
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str){
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

	/**
	 * 해당 str 이 null 또는 length 가 0 또는 빈 공간을 제외한 length 가 0 일 경우 true 리턴 <br>
	 *     str 이 대소문자 상관없이 "null" 단어만 존재해도 true 리턴
	 * @param str
	 * @return
	 */
	public static boolean isNullorEmpty(String str){
		try {
			if (str == null || str.length() == 0)
				return true;

			if (str.trim().length() == 0)
				return true;

			if(str.trim().toUpperCase().equals("NULL"))
				return true;

			return false;
		}catch (Exception e){
			return true;
		}
	}
}
