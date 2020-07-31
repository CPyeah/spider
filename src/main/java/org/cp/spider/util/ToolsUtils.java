package org.cp.spider.util;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * 一般工具方法
 * create by CP on 2019/8/14 0014.
 */
public class ToolsUtils {

    private static final DateTimeFormatter baseDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    /**
     * 0h1m23s 或 1时9分40秒 转换成 秒
     * @param time 0h1m23s 或 1时9分40秒
     * @return seconds
     */
    public static Integer getSecondsByTimeString(String time) {
        int indexOfHour = -1, indexOfMin = -1, indexOfSecond = -1;
        int totalSecond = 0;
        if (time.contains("m")) {
            indexOfHour = time.indexOf("h");
            indexOfMin = time.indexOf("m");
            indexOfSecond = time.indexOf("s");
        } else if (time.contains("分")) {
            indexOfHour = time.indexOf("时");
            indexOfMin = time.indexOf("分");
            indexOfSecond = time.indexOf("秒");
        }
        if (indexOfHour > -1) {
            totalSecond += (Integer.parseInt(time.substring(0, indexOfHour))*60*60);
        }
        if (indexOfMin > -1) {
            totalSecond += (Integer.parseInt(time.substring(indexOfHour+1, indexOfMin))*60);
        }
        if (indexOfSecond > -1) {
            totalSecond += Integer.parseInt(time.substring(indexOfMin+1, indexOfSecond));
        }
        return totalSecond;
    }

    /**
     * 秒 转成 时分秒
     * @param seconds 秒数 3904
     * @return 格式化 展示 1h5min4s
     */
    public static String secondsToTimeString(Integer seconds) {
        int h = seconds / 3600;
        int m = seconds % 3600 / 60;
        int s = seconds % 3600 % 60;
        StringBuilder time = new StringBuilder(30);
        if (h > 0) {
            time.append(h).append("h");
        }
        if (h > 0 || m > 0) {
            time.append(m).append("min");
        }
        time.append(s).append("s");
        return time.toString();
    }

    /**
     * 秒 转成 天时分秒
     * @param seconds 秒数 1232022
     * @return 格式化 展示 14day(s)6h13min42s
     */
    public static String secondsToDayTimeString(Integer seconds) {
        int days = seconds / (24*3600);
        int h = seconds % (24*3600) / 3600;
        int m = seconds % (24*3600) % 3600 / 60;
        int s = seconds % (24*3600) % 3600 % 60;
        StringBuilder time = new StringBuilder(30);
        if (days > 0) {
            time.append(days).append("day(s)");
        }
        if (h > 0) {
            time.append(h).append("h");
        }
        if (h > 0 || m > 0) {
            time.append(m).append("min");
        }
        time.append(s).append("s");
        return time.toString();
    }

    /**
     * 时间戳 格式化
     * @param timestamp 1566798855845
     * @return formatDateTime 2019-08-26 13:54:15
     */
    public static String getDateTimeByTimestamp(Long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zoneId);
        return localDateTime.format(baseDateTimeFormatter);
    }


    /**
     * 时间滚动工具方法
     * @param date  原日期时间
     * @param isPlus    是否是添加时间 true-往后退; false-往前推
     * @param amount   时间偏移量
     * @param unit  时间单位 ChronoUnit枚举(秒-ChronoUnit.SECONDS)
     * @return  结果时间
     */
    public static Date rollDateTime(Date date, boolean isPlus, long amount, TemporalUnit unit) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();// 时区 必不可少
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zoneId);
        if (isPlus) {
            localDateTime = localDateTime.plus(amount, unit);
        } else {
            localDateTime = localDateTime.minus(amount, unit);
        }
        instant = localDateTime.atZone(zoneId).toInstant();

        return Date.from(instant);
    }


    /**
     * 判断是否是不同的一天
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isDifferentDay(Date date1, Date date2) {
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(date1);
        calendar2.setTime(date2);
        return !(calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
                && calendar1.get(Calendar.DATE) == calendar2.get(Calendar.DATE));

    }

    /**
     * 判断对象是否是空
     * @param o 对象
     * @return 是否是空
     */
    public static boolean isNullOrEmpty(Object o) {
        if (o == null || o.toString() == null || "".equals(o.toString()) || "null".equalsIgnoreCase(o.toString())) {
            return true;
        }
        return false;
    }

    public static RestTemplate getBaseRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(10000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getMessageConverters().add(new MyMappingJackson2HttpMessageConverter());
        return restTemplate;
    }

    public static RestTemplate getHttpsRestTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        CloseableHttpClient httpClient = org.cp.spider.util.HttpClientUtils.acceptsUntrustedCertsHttpClient();
        HttpComponentsClientHttpRequestFactory httpsFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        httpsFactory.setConnectTimeout(10000);
        httpsFactory.setReadTimeout(10000);
        RestTemplate restTemplate = new RestTemplate(httpsFactory);
        restTemplate.getMessageConverters().add(new MyMappingJackson2HttpMessageConverter());
        return restTemplate;
    }


    /**
     * 从session里面获取语言信息
     * @return lang
     */
    public static String getLang() {
        HttpSession session = getSession();
        String lang = (String) session.getAttribute("lang");
        return lang;
    }

    /**
     * 防止语言信息
     * @param lang zh-CN/en-US...
     */
    public static void setLang(String lang) {
        HttpSession session = getSession();
        session.setAttribute("lang", lang);
    }

    public static HttpSession getSession() {
        ServletRequestAttributes attrs =(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs.getRequest();
        HttpSession session = request.getSession();
        return session;
    }

    public static void main(String[] args) {

    }
}
