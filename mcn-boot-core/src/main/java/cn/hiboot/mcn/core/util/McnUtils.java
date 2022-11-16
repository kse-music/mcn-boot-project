package cn.hiboot.mcn.core.util;

import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.tuples.Pair;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * 封装一些常用的工具
 *
 * @author DingHao
 * @since 2018/12/22 13:23
 */
public abstract class McnUtils {

    public static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static LocalDate dateToLocalDate(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
    }

    public static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取一周的第一天和最后一天的日期时间
     * @param week 0:当前周,-1:上一周,1:下一周
     * @return firstDate endDate
     */
    public static Pair<Date,Date> startEndDateTimeInWeek(int week){
        LocalDateTime now = LocalDateTime.now();
        now = (LocalDateTime) with(now,now.getDayOfWeek(),week);
        return Pair.with(Date.from(now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).atZone(ZoneId.systemDefault()).toInstant())
                ,Date.from(now.with(DayOfWeek.SUNDAY).withHour(23).withMinute(59).withSecond(59).atZone(ZoneId.systemDefault()).toInstant()));
    }

    private static Temporal with(Temporal temporal,DayOfWeek dayOfWeek,int week){
        if(week > 0){//下几周的第一天和最后一天
            for (long i = 0; i < week; i++) {
                temporal = temporal.with(TemporalAdjusters.next(dayOfWeek));
            }
        }else {//上几周的第一天和最后一天
            for (long i = 0; i < Math.abs(week); i++) {
                temporal = temporal.with(TemporalAdjusters.previous(dayOfWeek));
            }
        }
        return temporal;
    }

    /**
     * 获取一周的第一天和最后一天的日期
     * @param week 0:当前周,-1:上一周,1:下一周
     * @return firstDate endDate
     */
    public static Pair<Date,Date> startEndDateInWeek(int week){
        LocalDate now = LocalDate.now();
        now = (LocalDate) with(now, now.getDayOfWeek(), week);
        return Pair.with(Date.from(now.with(DayOfWeek.MONDAY).atStartOfDay(ZoneId.systemDefault()).toInstant()),Date.from(now.with(DayOfWeek.SUNDAY).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    public static Date now(){
        return Date.from(Instant.now());
    }

    public static boolean isNullOrEmpty(Object obj){
        if(Objects.isNull(obj)){
            return true;
        }
        if (obj instanceof Optional) {
            return !((Optional<?>) obj).isPresent();
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        }
        if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }
        return false;
    }

    public static boolean isNotNullAndEmpty(Object obj){
        return !isNullOrEmpty(obj);
    }

    public static String dealUrlPath(String path) {
        if(path == null || path.isEmpty()){
            return "";
        }
        while (path.contains("//")){
            path = path.replace("//","/");
        }
        if(!path.startsWith("/")){
            path = "/" + path;
        }
        if(path.endsWith("/")){
            path = path.substring(0, path.lastIndexOf("/"));
        }else if(path.endsWith("/*")){
            path = path.substring(0, path.lastIndexOf("/*"));
        }
        return path;
    }

    public static String getExtName(String fileName){
        if(fileName == null){
            return "";
        }
        int i = fileName.lastIndexOf(".");
        if(i == -1){
            return "";
        }
        return fileName.substring(i +1);
    }

    public static Properties loadProperties(String fileName){
        return loadProperties(fileName,McnUtils.class.getClassLoader());
    }

    /**
     * 从系统环境或者系统属性获取指定key的值，前者优先级高，然后解析(如果是/结尾，则自动拼上fileName)
     * @param fileName 文件名
     * @param key 文件路径key
     * @return Properties
     *
     */
    public static Properties loadProperties(String fileName,String key){
        String value = getValueFromSystemEnvOrProp(key, fileName);
        File file = new File(value);
        if(file.isDirectory()){
            file = new File(value,fileName);
        }
        return loadProperties(file.getAbsolutePath());
    }

    private static InputStream getInputStream(String fileName,ClassLoader cls){
        InputStream in;
        try {
            in = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            in = cls.getResourceAsStream(fileName);
        }
        return in;
    }

    public static Properties loadProperties(String fileName,ClassLoader cls){
        Properties properties = new Properties();
        try(InputStream in = getInputStream(fileName,cls)){
            properties.load(in);
        }catch (Exception e) {
            //ignore not found file
        }
        return properties;
    }

    public static String randomUUID(){
        return UUID.randomUUID().toString();
    }

    public static String simpleUUID(){
        return randomUUID().replace("-","");
    }

    public static String getValueFromSystemEnvOrProp(String key){
        String value = System.getenv(key);
        if(isNullOrEmpty(value)){
            value = System.getProperty(key);
        }
        return value;
    }

    public static String getValueFromSystemEnvOrProp(String key,String defaultValue){
        String value = getValueFromSystemEnvOrProp(key);
        return isNullOrEmpty(value) ? defaultValue : value;
    }

    public static boolean isDigital(String value){
        if(isNullOrEmpty(value)){
            return false;
        }
        for (char c : value.toCharArray()) {
            if(c < 48 || c > 57){
                return false;
            }
        }
        return true;
    }

    public static List<String> readAllLine(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            throw ServiceException.newInstance("read "+filePath+" failed",e);
        }
    }

    public static String readLine(String path) {
        List<String> list = readAllLine(path);
        return list.isEmpty() ? null : list.get(0);
    }

    public static byte[] readAllBytes(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw ServiceException.newInstance("read "+filePath+" failed",e);
        }
    }

    public static long copyFile(InputStream in,Path target) {
        try{
            Path parent = target.getParent();
            if(!Files.exists(parent)){
                Files.createDirectories(parent);
            }
            return Files.copy(in,target);
        }catch (IOException e){
            throw ServiceException.newInstance("copy file failed",e);
        }
    }

    public static String getVersion(Class<?> clazz){
        return clazz.getPackage().getImplementationVersion();
    }

    public static <T> T map2bean(Map<String,Object> map, Class<T> clz) {
        T obj;
        try{
            obj = clz.newInstance();
            BeanInfo b = Introspector.getBeanInfo(clz,Object.class);
            PropertyDescriptor[] pds = b.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                Method setter = pd.getWriteMethod();
                setter.invoke(obj, map.get(pd.getName()));
            }
        }catch (Exception e){
            throw ServiceException.newInstance(e);
        }
        return obj;
    }

    public static Map<String,Object> bean2map(Object bean){
        Map<String,Object> map = new HashMap<>();
        try{
            BeanInfo b = Introspector.getBeanInfo(bean.getClass(),Object.class);
            PropertyDescriptor[] pds = b.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                String propertyName = pd.getName();
                Method m = pd.getReadMethod();
                Object properValue = m.invoke(bean);
                if(properValue == null){
                    continue;
                }
                map.put(propertyName, properValue);
            }
        }catch (Exception e){
            throw ServiceException.newInstance(e);
        }
        return map;
    }

    public static void replaceAnnotationValue(Object proxy, Map<String,Object> map){
        McnAssert.state(proxy instanceof Annotation,"proxy must be an annotationType");
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
        try {
            Field declaredField = invocationHandler.getClass().getDeclaredField("memberValues");
            declaredField.setAccessible(true);
            Map memberValues = (Map) declaredField.get(invocationHandler);
            memberValues.putAll(map);
        } catch (Exception e) {
            //ignore
        }
    }

    public static void replaceAnnotationValue(Object proxy, String name,Object value){
        replaceAnnotationValue(proxy, Collections.singletonMap(name, value));
    }

    public static void loopEnd(BooleanSupplier endCondition){
        while (true){
            if(endCondition.getAsBoolean()){
                return;
            }
        }
    }

    public static void loopContinue(BooleanSupplier continueCondition){
        while (true){
            if(continueCondition.getAsBoolean()){
                continue;
            }
            return;
        }
    }

    public static Map<String,Object> put(Object... keyValues){
        McnAssert.notNull(keyValues,"keyValues must no be null");
        McnAssert.state(keyValues.length % 2 == 0,"The provided key/value array length must be a multiple of two");
        Map<String,Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i = i + 2) {
            map.put(keyValues[i].toString(),keyValues[i+1]);
        }
        return map;
    }

}
