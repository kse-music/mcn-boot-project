package cn.hiboot.mcn.core.util;

import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.tuples.Pair;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * 封装一些常用的工具
 *
 * @author DingHao
 * @since 2018/12/22 13:23
 */
public abstract class McnUtils {

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

    public static boolean isNullOrEmpty(String value){
        return Objects.isNull(value) || value.isEmpty();
    }

    public static boolean isNotNullAndEmpty(String value){
        return !isNullOrEmpty(value);
    }

    public static boolean isNullOrEmpty(Collection<?> value){
        return Objects.isNull(value) || value.isEmpty();
    }

    public static boolean isNotNullAndEmpty(Collection<?> value){
        return !isNullOrEmpty(value);
    }

    public static boolean isNullOrEmpty(Map<?,?> value){
        return Objects.isNull(value) || value.isEmpty();
    }

    public static boolean isNotNullAndEmpty(Map<?,?> value){
        return !isNullOrEmpty(value);
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
        return fileName.substring(fileName.lastIndexOf(".")+1);
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
            File file = new File(fileName);
            in = cls.getResourceAsStream(file.getName());
        }
        return in;
    }

    private static Properties loadProperties(String fileName,ClassLoader cls){
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

    public static List<String> readAllLine(String filePath) throws IOException {
        return Files.readAllLines(buildPath(filePath));
    }

    public static String readLine(String path) throws IOException {
        return readAllLine(path).get(0);
    }

    public static byte[] readAllBytes(String filePath) throws IOException {
        return Files.readAllBytes(buildPath(filePath));
    }

    public static long copyFile(String source,String target) throws IOException {
        checkTarget(target);
        return Files.copy(buildPath(source),Files.newOutputStream(buildPath(target)));
    }

    public static long copyFile(File source,File target) throws IOException {
        return copyFile(source.getAbsolutePath(),target.getAbsolutePath());
    }

    public static long copyFile(InputStream in,String target) throws IOException {
        checkTarget(target);
        return Files.copy(in,buildPath(target));
    }

    public static long copyFile(InputStream in,File target) throws IOException {
        return copyFile(in,target.getAbsolutePath());
    }

    private static void checkTarget(String target){
        File d = new File(target).getParentFile();
        if(!d.exists()){
            d.mkdirs();
        }
    }

    private static Path buildPath(String filePath){
        return Paths.get(filePath);
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
}
