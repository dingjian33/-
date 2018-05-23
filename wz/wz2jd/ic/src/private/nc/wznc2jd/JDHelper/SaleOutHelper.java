package nc.wznc2jd.JDHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import nc.vo.pubapp.pattern.data.IRowSet;

import com.jd.open.api.sdk.*;
import com.jd.open.api.sdk.response.ECLP.*;
import com.jd.open.api.sdk.request.ECLP.*;

import nc.vo.pub.lang.UFDouble;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.logging.Logger;
//import nc.bs.pub.pfxx.vouchers.CgeneralhProcessVoucher;
//import nc.bs.pub.pfxx.vouchers.XML_cgeneralhTranslator;
//import nc.itf.ic.CgeneralhOutBtnItf;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.vo.pub.BusinessException;
import nc.vo.pubapp.pattern.exception.ExceptionUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class SaleOutHelper {

	String SERVER_URL = ConfigHelper.getInstance().getSERVER_URL();
	String accessToken = ConfigHelper.getInstance().getAccessToken();
	String appKey = ConfigHelper.getInstance().getAppKey();
	String appSecret = ConfigHelper.getInstance().getAppSecret();

	public EclpOrderQueryOrderResponse Query(String sono) throws Exception {
		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey, appSecret);

		//		nc.bs.ct.purdaily.update.PurdailyUpdateBP b;
		EclpOrderQueryOrderRequest request = new EclpOrderQueryOrderRequest();

		request.setEclpSoNo(sono);

		EclpOrderQueryOrderResponse response = client.execute(request);
		return response;
	}

	public EclpOrderQueryOrderStatusResponse QueryStatus(String sono) throws Exception {
		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey, appSecret);
		EclpOrderQueryOrderStatusRequest request = new EclpOrderQueryOrderStatusRequest();
		request.setEclpSoNo(sono);
		EclpOrderQueryOrderStatusResponse response = client.execute(request);
		return response;
	}

	public EclpOrderAddOrderResponse Create(nc.vo.ic.m4c.entity.SaleOutVO data) throws Exception {

		ArrayList<String> ids = new ArrayList<String>();
		ArrayList<String> qtys = new ArrayList<String>();
		ArrayList<String> status = new ArrayList<String>();
		for (int i = 0; i < data.getBodys().length; i++) {
			nc.vo.ic.m4c.entity.SaleOutBodyVO body = data.getBodys()[i];
			ids.add(body.getCmaterialvid());
			UFDouble num = body.getNassistnum();//.getNshouldassistnum();
			if (num != null)
				qtys.add(String.valueOf(num.intValue()));
			else
				qtys.add("0");
			status.add("1");
		}
		nc.vo.ic.m4c.entity.SaleOutHeadVO head = data.getHead();
		//		  String s="select name  from bd_customer where pk_customer ='1"+head.getCcustomerid()+"'";
		//		  IRowSet rs = CommonHelper.sqlUtil.query(s);
		//			String custname="";
		//			while (rs.next()) {
		//				custname = rs.getString(0);
		//			}

		String consigneeName = data.getHead().getVdef4();
		StringBuilder builder = new StringBuilder();
		builder.append(data.getHead().getVdef5());
		builder.append(data.getHead().getVdef6());
		builder.append(data.getHead().getVdef7());
		builder.append(data.getHead().getVdef8());
		String consigneeAddress = builder.toString();//data.getHead().getVdef5()+data.getHead().getVdef6()+data.getHead().getVdef7()+data.getHead().getVdef8();
		String phone = data.getHead().getVdef9();
		if (CommonHelper.IsNullOrEmpty(consigneeName)) {
			throw new Exception("收货人不允许为空");
		}
		if (CommonHelper.IsNullOrEmpty(consigneeAddress)) {
			throw new Exception("收货地址不允许为空");
		}
		if (CommonHelper.IsNullOrEmpty(phone)) {
			throw new Exception("联系方式不允许为空");
		}
		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey, appSecret);

		EclpOrderAddOrderRequest request = new EclpOrderAddOrderRequest();

		request.setIsvUUID(data.getHead().getVbillcode());
		request.setIsvSource("ISV0020000000076");
		request.setShopNo("ESP0020000004720");
		//		  request.setBdOwnerNo( "jingdong" );
		request.setDepartmentNo("EBU4418046513690");
//		request.setWarehouseNo("110007600");
		request.setWarehouseNo(CommonHelper.GetWhJdCode(data.getHead().getCwarehouseid()));
		request.setShipperNo("CYS0000010");
//		request.setSalesPlatformOrderNo(data.getHead().getVdef3());
		request.setSalePlatformSource("6");
		//		  request.setSalesPlatformCreateTime( "2012-12-12 12:12:12" );
		request.setConsigneeName(consigneeName);
		request.setConsigneeMobile(phone);
		//		  request.setConsigneePhone( "jingdong" );
		//		  request.setConsigneeEmail( "jingdong" );
		//		  request.setExpectDate( "2012-12-12 12:12:12" );
		//		  request.setAddressProvince( "jingdong" );
		//		  request.setAddressCity( "jingdong" );
		//		  request.setAddressCounty( "jingdong" );
		//		  request.setAddressTown( "jingdong" );
		request.setConsigneeAddress(consigneeAddress);
		//		  request.setConsigneePostcode( "jingdong" );
		//		  request.setReceivable( 123 );
		//		  request.setConsigneeRemark( "jingdong" );
		request.setOrderMark("10000000000000000000000000000000000000000000000000");
		//		  request.setThirdWayBill( "jingdong" );
		//		  request.setPackageMark( "jingdong" );
		//		  request.setBusinessType( "jingdong" );
		//		  request.setDestinationCode( "jingdong" );
		//		  request.setDestinationName( "jingdong" );
		//		  request.setSendWebsiteCode( "jingdong" );
		//		  request.setSendWebsiteName( "jingdong" );
		//		  request.setSendMode( "jingdong" );
		//		  request.setReceiveMode( "jingdong" );
		//		  request.setAppointDeliveryTime( "jingdong" );
		//		  request.setInsuredPriceFlag( "jingdong" );
		//		  request.setInsuredValue( 123 );
		//		  request.setInsuredFee( 123 );
		//		  request.setThirdPayment( 123 );
		//		  request.setMonthlyAccount( "jingdong" );
		//		  request.setShipment( "jingdong" );
		//		  request.setSellerRemark( "jingdong" );
		//		  request.setThirdSite( "jingdong" );
		//		  request.setCustomsStatus( "jingdong" );
		//		  request.setCustomerName( "jingdong" );
		//		  request.setInvoiceTitle( "jingdong" );
		//		  request.setInvoiceContent( "jingdong" );
		//		  request.setGoodsType( "jingdong" );
		//		  request.setGoodsLevel( "jingdong" );
		//		  request.setCustomsPort( "jingdong" );
		request.setGoodsNo(PurInWareHelper.ConvertMaterial(ids));
		//		  request.setPrice( "123,234,345" );
		request.setQuantity(PurInWareHelper.GetStr(qtys));

		EclpOrderAddOrderResponse response = client.execute(request);
		return response;
	}

	public EclpOrderCancelOrderResponse Delete(nc.vo.ic.m4c.entity.SaleOutVO data) throws JdException {
		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey, appSecret);
		EclpOrderCancelOrderRequest request = new EclpOrderCancelOrderRequest();
		request.setEclpSoNo(data.getHead().getVdef13());
		EclpOrderCancelOrderResponse response = client.execute(request);
		return response;
	}
	private String readDateInform() throws BusinessException {
		String url="";
		FileInputStream fin = null;
		Properties prop = new Properties();
		try { 
			String configFilePath = "modules/so/META-INF/vouchersync.properties";
			fin = new FileInputStream(new File(configFilePath));
			prop.load(fin);
			url = prop.getProperty("postfhurl");
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != fin) {
				try {
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return url;
	}
	public void Approve(nc.vo.ic.m4c.entity.SaleOutVO data) throws Exception {
		try {
			//          String  url += "?number=" + li.get(i)[0] + "&logisticsName=" + li.get(i)[18] + "&logisticsNo='"+li.get(i)[19]+"'" ;

			//            /beginDate=2016-11-01 00:00:00&endDate=2016-12-01 00:00:00
			//发送get请求
			String lgnum = CommonHelper.ToString(data.getHead().getVdef11());
			String lgname = CommonHelper.ToString(data.getHead().getVdef10());
			String docno = CommonHelper.ToString(data.getHead().getVdef3());
			String u=readDateInform();
			Logger.error("审核出库单接口获取Url地址：" + u);
			if (!CommonHelper.IsNullOrEmpty(docno)&&!CommonHelper.IsNullOrEmpty(u)) {
				String url = u+"?number=" + docno + "&logisticsName="
						+ java.net.URLEncoder.encode(lgname, "utf-8") + "&logisticsNo=" + lgnum;
				//				url= java.net.URLEncoder.encode(url,"utf-8");
				HttpPost request = new HttpPost(url);
				HttpClient client = new DefaultHttpClient();
				HttpResponse response = client.execute(request);
				Logger.error("审核出库单接口执行Url地址：" + url);
				/** 请求发送成功，并得到响应 **/
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				} else {
					throw new Exception("回写商城失败"+u);
				}
			}
		} catch (ClientProtocolException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
}
