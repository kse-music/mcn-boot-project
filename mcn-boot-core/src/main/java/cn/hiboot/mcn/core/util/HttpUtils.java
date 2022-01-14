package cn.hiboot.mcn.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 建立在URLConnection的http
 * @author DingHao
 * @since 2018年9月4日14:49:53
 */
public final class HttpUtils {

	private static URLConnection openConnection(String url) throws IOException {
        URL realUrl = new URL(url);
        URLConnection conn = realUrl.openConnection();
        // 设置通用的请求属性
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        return conn;
    }

	private static String parseParam(Map<String,String> param){
        List<String> list = new ArrayList<>();
        param.forEach((k,v) -> {
            list.add(k+"="+v);
        });
	    StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if(i > 0){
                sb.append("&");
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    private static String dealGetParam(String url, Map<String,String> getParam){
        String f = "?";
        if(url.contains("?")){
            f = "&";
        }
        url = url + f + parseParam(getParam);
        return url;
    }

    public static String sendGet(String url) {
	    return sendGet(url,new HashMap<>());
    }

    public static String sendGet(String url, Map<String,String> getParam) {
	    return sendGet(dealGetParam(url,getParam),parseParam(getParam));
    }

    public static String sendPost(String url, Map<String,String> postParam) {
	    return sendPost(url,parseParam(postParam));
    }

    public static String sendPost(String url, Map<String,String> getParam, Map<String,String> postParam) {
	    return sendPost(dealGetParam(url,getParam),parseParam(postParam));
    }

    /**
     * 向指定URL发送GET方法的请求
     * @param url 发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    private static String sendGet(String url, String param) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try {
            // 打开和URL之间的连接
            URLConnection connection = openConnection(url);
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
//			Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
//			for (String key : map.keySet()) {
//				System.out.println(key + "--->" + map.get(key));
//			}
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            //
        }finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                //
            }
        }
        return result.toString();
    }

	/**
	 * 向指定 URL 发送POST方法的请求
	 * @param url 发送请求的 URL
	 * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	private static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		StringBuilder result = new StringBuilder();
		try {
			// 打开和URL之间的连接
            URLConnection conn = openConnection(url);
            // 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
                result.append(line);
			}
		} catch (Exception e) {
            //
        }finally{
			try{
				if(out!=null){
					out.close();
				}
				if(in!=null){
					in.close();
				}
			}
			catch(IOException ex){
                //
            }
		}
		return result.toString();
	}

}

