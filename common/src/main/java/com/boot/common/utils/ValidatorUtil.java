package com.boot.common.utils;

import java.util.regex.Pattern;

/**
 * @author Yarns
 */
public class ValidatorUtil {

    /**
     * 判断是否为中文表达式
     */
    public static String CONTAIN_CHINESE_STRING = "[\u4e00-\u9fa5]";
    /**
     * 是否数字
     */
    private static final Pattern PATTERN_NUMERIC = Pattern.compile("^(-?\\d+)(\\.\\d+)?$");
    /**
     * 是否邮箱
     */
    private static final Pattern PATTERN_EMAIL = Pattern
            .compile("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$");
    /**
     * 是否为手机号
     */
    private static final Pattern PATTERN_MOBILE_PHONE = Pattern.compile("^(1)\\d{10}$");

    /**
     * 判断是否为浮点数或者整数
     *
     * @param value 字符串
     * @return true Or false
     */
    public static boolean isNumeric(String value) {
        return ValidatorUtil.PATTERN_NUMERIC.matcher(value).matches();
    }

    /**
     * 判断是否为数字
     *
     * @param value 字符串
     * @return true Or false
     */
    public static boolean isNumber(String value) {
        try {
            Integer.parseInt(value);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否为正确的邮件格式
     *
     * @param value 字符串
     * @return true Or false
     */
    public static boolean isEmail(String value) {
        return ValidatorUtil.PATTERN_EMAIL.matcher(value).matches();
    }

    /**
     * 判断字符串是否为合法手机号
     *
     * @param value 字符串
     * @return true Or false
     */
    public static boolean isMobile(String value) {
        return ValidatorUtil.PATTERN_MOBILE_PHONE.matcher(value).matches();
    }
}
