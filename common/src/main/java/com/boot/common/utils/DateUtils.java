package com.boot.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具类
 * @author Yarns
 */
public class DateUtils {
    public static final String FULL_TIME_PATTERN = "yyyyMMddHHmmss";

    public static final String FULL_TIME_SPLIT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String CST_TIME_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";

    public static final String DATE_EIGHT_PATTERN = "yyyyMMdd";

    public static final String YEAR = "yyyy";

    public static final String DATE_EIGHT_PATTERN_SPLIT = "yyyy-MM-dd";

    public static String getNow(){
        return getDateFormat(new Date(),FULL_TIME_SPLIT_PATTERN);
    }


    /**
     * 格式化时间，格式为 yyyyMMddHHmmss
     *
     * @param localDateTime LocalDateTime
     * @return 格式化后的字符串
     */
    public static String formatFullTime(LocalDateTime localDateTime) {
        return formatFullTime(localDateTime, FULL_TIME_PATTERN);
    }

    public static String nowFormatFullTime(){
        return formatFullTime(LocalDateTime.now());
    }

    /**
     * 根据传入的格式，格式化时间
     *
     * @param localDateTime LocalDateTime
     * @param format        格式
     * @return 格式化后的字符串
     */
    public static String formatFullTime(LocalDateTime localDateTime, String format) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(dateTimeFormatter);
    }

    /**
     * 根据传入的格式，格式化时间
     *
     * @param date   Date
     * @param format 格式
     * @return 格式化后的字符串
     */
    public static String getDateFormat(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.CHINA);
        return simpleDateFormat.format(date);
    }

    /**
     * 格式化 CST类型的时间字符串
     *
     * @param date   CST类型的时间字符串
     * @param format 格式
     * @return 格式化后的字符串
     * @throws ParseException 异常
     */
    public static String formatCSTTime(String date, String format) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CST_TIME_PATTERN, Locale.US);
        Date usDate = simpleDateFormat.parse(date);
        return getDateFormat(usDate, format);
    }

    /**
     * 格式化 Instant
     *
     * @param instant Instant
     * @param format  格式
     * @return 格式化后的字符串
     */
    public static String formatInstant(Instant instant, String format) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 判断当前时间是否在指定时间范围
     *
     * @param from 开始时间
     * @param to   结束时间
     * @return 结果
     */
    public static boolean between(LocalTime from, LocalTime to) {
        LocalTime now = LocalTime.now();
        return now.isAfter(from) && now.isBefore(to);
    }


    /**
     * 获取八位时间用-分隔
     * @param dateStr
     * @return
     */
    public static LocalDate getLocalDate(String dateStr){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_EIGHT_PATTERN_SPLIT);
        return LocalDate.parse(dateStr, formatter);
    }


    /**
     * 获取指定时间格式的时间
     * @param dateStr
     * @param pattern
     * @return
     */
    public static LocalDate getLocalDate(String dateStr, String pattern){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(dateStr, formatter);
    }


    /**
     * 获取八位时间用-分隔
     * @param dateStr
     * @return
     */
    public static LocalDateTime getLocalDateTime(String dateStr){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FULL_TIME_SPLIT_PATTERN);
        return LocalDateTime.parse(dateStr, formatter);
    }


    /**
     * 获取指定时间格式的时间
     * @param dateStr
     * @param pattern
     * @return
     */
    public static LocalDateTime getLocalDateTime(String dateStr, String pattern){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateStr, formatter);
    }

    /**
     * 获取4位整型年份
     * @return
     */
    public static Integer getYearInteger(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YEAR);
        String format = now.format(formatter);
        return Integer.parseInt(format);
    }
}
