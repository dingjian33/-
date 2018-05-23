package nc.wznc2jd.JDHelper;

import nc.vo.pubapp.pattern.data.IRowSet;

public class ConfigHelper {
private  static ConfigHelper ch=new ConfigHelper();
	public static ConfigHelper getInstance(){
		ch=new ConfigHelper();
		return ch;
	}
protected ConfigHelper(){
	String s="select * from (select  appkey,appsecret,accesstoken from JDCOnfig) where rownum<=1;";
	  IRowSet rs = CommonHelper.sqlUtil.query(s);
		String custname="";
		while (rs.next()) {

			 appKey=rs.getString(0);
			 appSecret=rs.getString(1);
			 accessToken=rs.getString(2);
		}
	
}
	protected ConfigHelper(String _accessToken,
	String _appKey,String _appSecret){
//		 SERVER_URL=_SERVER_URL;
		 accessToken=_accessToken;
		 appKey=_appKey;
		 appSecret=_appSecret;
	}
//	String SERVER_URL="";
	String accessToken="";
	String appKey="";
	String appSecret="";

	String SERVER_URL="https://api.jd.com/routerjson";
//	public String getSERVER_URL() {
//		return SERVER_URL;
//	}
//	public void setSERVER_URL(String sERVER_URL) {
//		SERVER_URL = sERVER_URL;
//	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getAppKey() {
		return appKey;
	}
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	public String getAppSecret() {
		return appSecret;
	}
	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}
	

	public String getSERVER_URL() {
		return SERVER_URL;
	}
}
