package me.imli.newme.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 
 * @author Doots
 *
 */
public class RegexValidateUtils {
	
	/**
	 * 检查帐号
	 * 字母开头的6-14位的英文字母与数字的组合
	 * @param acc
	 * @return
	 */
	public static boolean checkAcc(String acc) {
		if (acc.contains("@")) {
			return checkEmail(acc);
		}
		String check = "^([\u4e00-\u9fa5A-Za-z0-9_]{2,10}$)";
		return check(check, acc);
	}
	
	/**
	 * 检查密码
	 * 6-16位的英文字母与数字的组合
	 * @param pwd
	 * @return
	 */
	public static boolean checkPwd(String pwd) {
		return pwd.length() >= 6 && pwd.length() <= 18;
		
		
//		String check = "^([A-Za-z0-9]{6,18}$)";
//		return check(check, pwd);
	}
	
	/**
	 * 正则Email
	 * 
	 * @param email
	 * @return
	 */
	public static boolean checkEmail(String email) {
		String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		return check(check, email);
	}

	/**
	 * 正则电话号
	 * 
	 * @param mobileNumber
	 * @return
	 */
	public static boolean checkMobileNumber(String mobileNumber) {
		String check = "^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$";
		return check(check, mobileNumber);
	}
	
	/**
	 * 正则手机号
	 * @param num
	 * @return
	 */
	public static boolean checkPhoneNumber(String num) {
		String check = "1[3|4|5|7|8|][0-9]{9}";
		return check(check, num);
	}
	
	/**
	 * 检测是否包含电话号码
	 * @param num
	 * @return
	 */
	public static boolean checkHasMobileNumber(String num) {
		String check = ".{0,}1[3|4|5|7|8|][0-9]{9}.{0,}";
		return check(check, num);
	}

	/**
	 * 正则字符串
	 * 
	 * @param str
	 * @return
	 */
	public static boolean checkChar(String str) {
		String check = "^[A-Za-z]+$";
		return check(check, str);
	}
	
	/**
	 * 正则汉字字符
	 * @param str
	 * @return
	 */
	public static boolean checkText(String str) {
		String check = "^([\u4e00-\u9fa5A-Za-z]{0,}$)";
		return check(check, str);
	}
	
	/**
	 * 获取字符串长度
	 * @param s
	 * @return
	 */
    public static int getWordCount(String s) {
        s = s.replaceAll("[^\\x00-\\xff]", "**");
        int length = s.length();
        return length;
    }
    
    /**
     * 正则检查
     * @param check
     * @param str
     * @return
     */
	public static boolean check(String check, String str) {
		boolean flag = false;
		try {
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(str);
			flag = matcher.matches();
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}
}
