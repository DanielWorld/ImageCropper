/*
 * Copyright (c) 2016 DanielWorld.
 * @Author Namgyu Park
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.danielworld.imagecropper.util;

/**
 * Utility for String
 * <br><br>
 * Created by Namgyu Park on 2016-01-23.
 */
public class StringUtil {

	/**
	 If parameter String is null or length == 0 or length without empty space == 0 then return true
	 * @param str
	 * @return
	 */
	public static boolean isNullorEmpty(String str){
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
