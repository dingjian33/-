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
import nc.vo.so.m4331.entity.DeliveryVO;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class DeliveryHelper {

	String SERVER_URL = ConfigHelper.getInstance().getSERVER_URL();
	String accessToken = ConfigHelper.getInstance().getAccessToken();
	String appKey = ConfigHelper.getInstance().getAppKey();
	String appSecret = ConfigHelper.getInstance().getAppSecret();

	public EclpOrderQueryOrderResponse Query(String sono) throws Exception {
		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey, appSecret);

		EclpOrderQueryOrderRequest request = new EclpOrderQueryOrderRequest();

		request.setEclpSoNo(sono);

		EclpOrderQueryOrderResponse response = client.execute(request);
		return response;
	}

	public EclpOrderAddOrderResponse Approve(DeliveryVO data) throws Exception {

		ArrayList<String> ids = new ArrayList<String>();
		ArrayList<String> qtys = new ArrayList<String>();
		Map<String, Integer> idandqtys = new HashMap<String, Integer>();
		String whid="";
		ArrayList<String> status = new ArrayList<String>();
		for (int i = 0; i < data.getChildrenVO().length; i++) {
			nc.vo.so.m4331.entity.DeliveryBVO body = data.getChildrenVO()[i];
			//			ids.add(body.getCmaterialvid());
			//			UFDouble num = body.getNastnum();//.getNshouldassistnum();
			//			if (num != null)
			//				qtys.add(String.valueOf(num.intValue()));
			//			else
			//				qtys.add("0");
			//			status.add("1");
			String id = body.getCmaterialvid();
			Integer qty;
			UFDouble num = body.getNastnum();//.getNshouldassistnum();
			if (num != null)
				qty = num.intValue();
			else
				qty = 0;
			if (idandqtys.containsKey(id)) {
				qty = qty + idandqtys.get(id);
			} else {
				ids.add(id);
			}
			idandqtys.put(id, qty);
			whid=body.getCsendstordocid();
		}
		for (int i = 0; i < ids.size(); i++) {
			String id = ids.get(i);
			Integer qty = idandqtys.get(id);
			qtys.add(String.valueOf(qty));
			status.add("1");
		}
		nc.vo.so.m4331.entity.DeliveryHVO head = data.getParentVO();
		String consigneeName = head.getVdef4();
		StringBuilder builder = new StringBuilder();
		builder.append(head.getVdef5());
		builder.append(head.getVdef6());
		builder.append(head.getVdef7());
		builder.append(head.getVdef8());
		String consigneeAddress = builder.toString();//data.getHead().getVdef5()+data.getHead().getVdef6()+data.getHead().getVdef7()+data.getHead().getVdef8();
		String phone = head.getVdef9();
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

		request.setIsvUUID(head.getVbillcode());
		request.setIsvSource("ISV0020000000076");
		request.setShopNo("ESP0020000004720");
		//		  request.setBdOwnerNo( "jingdong" );
		request.setDepartmentNo("EBU4418046513690");
//		request.setWarehouseNo("110007600");
		request.setWarehouseNo(CommonHelper.GetWhJdCode(whid));
		request.setShipperNo("CYS0000010");
		request.setSalesPlatformOrderNo(head.getVdef3());
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
		request.setOrderMark("00000000000000000000000000000000000000000000000000");
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
//		throw new Exception("发货传给京东发生异常：");
		EclpOrderAddOrderResponse response = client.execute(request);
		return response;
	}

	public EclpOrderCancelOrderResponse UnApprove(DeliveryVO data) throws JdException {
		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey, appSecret);
		EclpOrderCancelOrderRequest request = new EclpOrderCancelOrderRequest();
		request.setEclpSoNo(data.getParentVO().getVdef13());
		EclpOrderCancelOrderResponse response = client.execute(request);
		return response;
	}

}
