package nc.edb.EDBHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class EdbLib {
	 
    
     private String dbhost = "edb_a82980";// 主账号(正式时让EDB分配)
     private String appkey = "f0147944";// (正式时让EDB分配)
     private String appscret = "05bf522bda7741c88b27099dec3dade6";// (正式时让EDB分配)
     private String token = "f100e7ac9f1246a3994613877bb4e21f";// (正式时让EDB分配)        
     private String serviceUrl = "http://59.111.98.152/rest/index.aspx";// 服务器地址(正式时让EDB分配)
	//访问E店宝接口用到的信息如何获取？ --> http://vip13.edb08.com.cn/mongolog/EAOPHandler.ashx?_id=94b0473980374bfb9c8d395aec73dc67
	/**
	 * 获取公共参数
	 * 
	 * @param method
	 *            接口名称
	 * @return 公共参
	 */
	public Map<String, String> edbGetCommonParams(String method) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("method", method);// 接口名称
		map.put("dbhost", this.dbhost);
		map.put("appkey", this.appkey);
//		map.put("format", "JSON");// 返回的数据格式
		map.put("format", "XML");// 返回的数据格式
		map.put("timestamp", new SimpleDateFormat("yyyyMMddHHmm").format(new Date()));// timestamp
//		map.put("timestamp", "201612251416");// timestamp
																						// 全小写
		map.put("v", "2.0");// 版本号
		map.put("slencry", "1");//
		map.put("ip", "192.168.1.153");// 本机ip
		return map;
	}

	/**
	 * 开始请求
	 * 
	 * @param params
	 *            参数(不要包含appscret和token)
	 * @return 服务回应
	 */
	public String edbRequstPost(Map<String, String> params) {		
		HttpURLConnection connection = null;
		try {
			StringBuilder builder = new StringBuilder();
			for (String key : params.keySet()) {
				String val="xmlValues".equalsIgnoreCase(key)?URLEncoder.encode(params.get(key), "UTF-8"):params.get(key);
				builder.append(String.format("%1$s=%2$s&", key,  URLEncoder.encode(val, "UTF-8")));
			}
			builder.append("sign=" + edbSignature(params));

			System.out.println("服务地址 :" + this.serviceUrl);
			System.out.println("请求数据 :" + URLDecoder.decode(URLDecoder.decode(builder.toString(), "utf-8"), "utf-8"));
			System.out.println("实际发送 :" + builder);
			connection = (HttpURLConnection) new URL(this.serviceUrl).openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			connection.connect();
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
			out.write(builder.toString());
			out.flush();
			out.close();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
			builder.setLength(0);
			String line = "";
			while ((line = reader.readLine()) != null)
				builder.append(line);
			reader.close();
			System.out.println("回应数据 :" + builder);
			return builder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		} finally {
			if (connection != null)
				connection.disconnect();
		}
	}

	
	/**
	 * 签名
	 * 
	 * @param params
	 *            参数
	 * @return 签名结果
	 */
	private String edbSignature(Map<String, String> params) {
		Map<String, String> treeMap = new TreeMap<String, String>(comparator);
		treeMap.putAll(params);
		treeMap.put("appscret", this.appscret);
		treeMap.put("token", this.token);
		// 拼接要签名的字符串
		StringBuilder builder = new StringBuilder(this.appkey);
		for (String key : treeMap.keySet()) {
			if ("".equals(key) || "".equals(treeMap.get(key)))
				continue;
			builder.append(key).append(treeMap.get(key));
		}
		System.out.println("签名明文:" + builder);
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] bytes = md.digest(builder.toString().getBytes("utf-8"));
			builder.setLength(0);
			for (byte b : bytes) {
				String hx = Integer.toHexString(b & 0XFF);
				builder.append(hx.length() == 1 ? "0" + hx : hx);
			}
			return builder.toString().toUpperCase();
		} catch (Exception e) {
			return "签名异常";
		}
	}

	/**
	 * 比较器
	 */
	private static Comparator<String> comparator = new Comparator<String>() {
		@Override
		public int compare(String k1, String k2) {
			return k1.compareToIgnoreCase(k2);
		}
	};
}
