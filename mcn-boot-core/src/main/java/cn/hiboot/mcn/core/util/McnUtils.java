package cn.hiboot.mcn.core.util;

import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.tuples.Pair;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 封装一些常用的工具
 *
 * @author DingHao
 * @since 2018/12/22 13:23
 */
@SuppressWarnings({"rawtypes","unchecked"})
public abstract class McnUtils {
    private static final int BUFFER_SIZE = 4096;

    private static final String DOT = ".";
    private static final DateTimeFormatter PATTERN_1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter PATTERN_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String formatNowDate() {
        return localDateToString(LocalDate.now());
    }

    public static String formatNowDateTime() {
        return localDateTimeToString(LocalDateTime.now());
    }

    public static String localDateToString(LocalDate date) {
        return formatToString(date,2);
    }

    public static String localDateTimeToString(LocalDateTime dateTime) {
        return formatToString(dateTime,1);
    }

    public static String localDateTimeToString(LocalDateTime dateTime,int mod) {
        return formatToString(dateTime,mod);
    }

    private static String formatToString(TemporalAccessor date,int mod) {
        return (mod == 2 ? PATTERN_2 :PATTERN_1).format(date);
    }

    public static LocalDate stringToLocalDate(String dateString) {
        return LocalDate.from(PATTERN_2.parse(dateString));
    }

    public static LocalDateTime stringToLocalDateTime(String dateTimeString) {
        return LocalDateTime.from(PATTERN_1.parse(dateTimeString));
    }

    public static long localDateToEpochMilli(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static long localDateTimeToEpochMilli(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static String millToString(Long mill){
        return millToString(mill,1);
    }

    public static String millToString(Long mill,int mod){
        return formatToString(LocalDateTime.ofInstant(Instant.ofEpochMilli(mill), ZoneId.systemDefault()),mod);
    }

    public static String dateToString(Date date){
        return dateToString(date,1);
    }

    public static String dateToString(Date date,int mod){
        return millToString(date.getTime(),mod);
    }

    public static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static LocalDate dateToLocalDate(Date date) {
        return dateToLocalDateTime(date).toLocalDate();
    }

    public static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取指定日期的开始时间与结束时间
     * @param specifiedDate 1
     * @return start ms end ms
     */
    public static Pair<Long,Long> dayInMs(LocalDate specifiedDate){
        LocalDateTime startOfDay = specifiedDate.atStartOfDay();
        LocalDateTime endOfDay = specifiedDate.atTime(LocalTime.MAX);
        long start = startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end = endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return Pair.with(start,end);
    }

    /**
     * 近几天
     * @param days 1
     * @return firstDate endDate
     */
    public static Pair<Date,Date> lastDay(int days){
        return lastDay(LocalDateTime.now(),days);
    }

    public static Pair<Date,Date> lastDay(LocalDateTime specifiedDate,int days){
        return Pair.with(Date.from(specifiedDate.withHour(0).withMinute(0).withSecond(0).minusDays(days).atZone(ZoneId.systemDefault()).toInstant())
                ,Date.from(specifiedDate.withHour(23).withMinute(59).withSecond(59).atZone(ZoneId.systemDefault()).toInstant()));
    }

    /**
     * 未来几天
     * @param days 1
     * @return firstDate endDate
     */
    public static Pair<Date,Date> nextDay(int days){
        return nextDay(LocalDateTime.now(),days);
    }

    public static Pair<Date,Date> nextDay(LocalDateTime specifiedDate,int days){
        return Pair.with(Date.from(specifiedDate.withHour(0).withMinute(0).withSecond(0).atZone(ZoneId.systemDefault()).toInstant())
                ,Date.from(specifiedDate.withHour(23).withMinute(59).withSecond(59).plusDays(days).atZone(ZoneId.systemDefault()).toInstant()));
    }

    /**
     * 获取一周的第一天和最后一天的日期时间
     * @param week 0:当前周,-1:上一周,1:下一周
     * @return firstDate endDate
     */
    public static Pair<Date,Date> startEndDateTimeInWeek(int week){
        return startEndDateTimeInWeek(LocalDateTime.now(),week);
    }

    public static Pair<Date,Date> startEndDateTimeInWeek(LocalDateTime specifiedDate,int week){
        LocalDateTime now = specifiedDate;
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
        if (obj instanceof Optional<?> op) {
            return op.isEmpty();
        }
        if (obj instanceof CharSequence cs) {
            return cs.isEmpty();
        }
        if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        }
        if (obj instanceof Collection<?> col) {
            return col.isEmpty();
        }
        if (obj instanceof Map<?, ?> map) {
            return map.isEmpty();
        }
        return false;
    }

    public static boolean isNotNullAndEmpty(Object obj){
        return !isNullOrEmpty(obj);
    }

    public static String getExtName(String fileName){
        if(isNullOrEmpty(fileName)){
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
        }catch (Exception ignored) {
        }
        return properties;
    }

    public static Properties loadProperties(String fileName,Class<?> clazz){
        if(clazz == null){
            return loadProperties(fileName);
        }
        Properties properties = new Properties();
        try(InputStream in = clazz.getResourceAsStream(fileName)){
            properties.load(in);
        }catch (Exception ignored) {
        }
        return properties;
    }

    public static String randomUUID(){
        return UUID.randomUUID().toString();
    }

    public static String simpleUUID(){
        return randomUUID().replace("-","");
    }

    public static long snowflakeId(){
        return SnowflakeIdWorker.getInstance().nextId();
    }

    public static String snowflakeIdString(){
        return SnowflakeIdWorker.getInstance().nextIdString();
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

    public static String readLine(String filePath) {
        return new String(readAllBytes(filePath), StandardCharsets.UTF_8);
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
        }finally {
            close(in);
        }
    }

    public static long copyFile(byte[] bytes,Path target) {
        return copyFile(new ByteArrayInputStream(bytes),target);
    }

    public static byte[] copyToByteArray(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        try{
            copy(in, out);
            return out.toByteArray();
        }finally {
            close(in);
            close(out);
        }
    }

    private static void close(Closeable closeable) {
        try {
            closeable.close();
        }catch (IOException ex) {
            // ignore
        }
    }

    public static int copy(InputStream in, OutputStream out) throws IOException {
        int byteCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            byteCount += bytesRead;
        }
        out.flush();
        return byteCount;
    }


    public static void deleteFile(String filePath){
        deleteDirectory(filePath);
    }

    public static void deleteDirectory(String dirPath){
        try {
            Files.walkFileTree(Paths.get(dirPath),new SimpleFileVisitor<Path>(){
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult postVisitDirectory(Path dir,IOException exc) throws IOException{
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {
        }
    }

    public static String getVersion(Class<?> clazz){
        return clazz.getPackage().getImplementationVersion();
    }

    public static <T> T map2bean(Map<String,Object> map, Class<T> clz) {
        T obj;
        try{
            obj = clz.getDeclaredConstructor().newInstance();
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
        } catch (Exception ignored) {
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

    public static void unzip(String zipFilePath) {
        unzip(zipFilePath,null,false);
    }

    public static void unzip(String zipFilePath,boolean isDelete) {
        unzip(zipFilePath,null,isDelete);
    }

    public static void unzip(String zipFilePath,String targetDirName) {
        unzip(zipFilePath,targetDirName,false);
    }

    public static void unzip(String zipFilePath,String targetDirName,boolean deleteZipFile){
        File file = new File(zipFilePath);
        try(ZipFile zipFile = new ZipFile(file,deleteZipFile ? ZipFile.OPEN_READ | ZipFile.OPEN_DELETE : ZipFile.OPEN_READ)){
            Path targetDir = file.toPath().getParent();
            if(targetDirName != null){
                targetDir = targetDir.resolve(targetDirName);
                if(!Files.exists(targetDir)){
                    Files.createDirectories(targetDir);
                }
            }
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path path = targetDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    if(!Files.exists(path)){
                        Files.createDirectories(path);
                    }
                } else {
                    if(!Files.exists(path.getParent())){
                        Files.createDirectories(path.getParent());
                    }
                    Files.copy(zipFile.getInputStream(entry), path, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> loadFile(String dir, String suffix, Function<Path,T> converter) {
        McnAssert.notNull(dir, "dir is null !");
        McnAssert.notNull(suffix, "suffix is null !");
        if(suffix.lastIndexOf(DOT) == -1){
            suffix = DOT + suffix;
        }
        try{
            String finalSuffix = suffix;
            try(Stream<Path> stream = Files.walk(Paths.get(dir)).filter(f -> f.toString().toLowerCase().endsWith(finalSuffix))){
                return stream.map(converter).collect(Collectors.toList());
            }
        }catch (IOException e){
            return Collections.emptyList();
        }
    }

    public static URL getURL(Path path) {
        return getURL(path.toFile());
    }

    public static URL getURL(File file) {
        McnAssert.notNull(file, "File is null !");
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw ServiceException.newInstance("Error occur when get URL!", e);
        }
    }

    public static void addURLToClasspath(ClassLoader classLoader, String... jarPaths) {
        URL[] urls = Arrays.stream(jarPaths).flatMap(jarPath -> loadFile(jarPath, ".jar", McnUtils::getURL).stream()).toArray(URL[]::new);
        if (urls.length == 0) {
            return;
        }
        Thread.currentThread().setContextClassLoader(new URLClassLoader(urls, classLoader));
    }

    public static String substring(String str,int maxLength) {
        if(str == null){
            return null;
        }
        return str.length() < maxLength ? str : str.substring(0,maxLength);
    }

    public static <T> List<T> sublist(List<T> list,int maxLength) {
        if(list == null){
            return null;
        }
        return list.size() < maxLength ? list : list.subList(0,maxLength);
    }

    public static <S> List<S> list(Iterable<S> iterable){
        List<S> rs = new ArrayList<>();
        iterable.forEach(rs::add);
        return rs;
    }

    public static <S> Set<S> set(Iterable<S> iterable){
        Set<S> rs = new HashSet<>();
        iterable.forEach(rs::add);
        return rs;
    }

    public static boolean isFieldAllNull(Object obj) {
        if (obj != null) {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object fieldObj;
                try {
                    fieldObj = field.get(obj);
                } catch (IllegalAccessException e) {
                    continue;
                }
                if (Objects.nonNull(fieldObj)) {
                    return false;
                }
            }
        }
        return true;
    }

}