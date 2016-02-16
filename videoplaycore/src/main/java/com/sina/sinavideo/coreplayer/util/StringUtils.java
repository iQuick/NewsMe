package com.sina.sinavideo.coreplayer.util;

public class StringUtils {

	public static int convertToInt(String str) throws NumberFormatException {
		int s, e;
		for (s = 0; s < str.length(); s++)
			if (Character.isDigit(str.charAt(s)))
				break;
		for (e = str.length(); e > 0; e--)
			if (Character.isDigit(str.charAt(e - 1)))
				break;
		if (e > s) {
			try {
				return Integer.parseInt(str.substring(s, e));
			} catch (NumberFormatException ex) {
				LogS.e("convertToInt", ex.toString());
				throw new NumberFormatException();
			}
		} else {
			throw new NumberFormatException();
		}
	}
}
