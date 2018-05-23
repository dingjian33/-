package nc.wznc2jd.JDHelper;

import com.jd.open.api.sdk.*;
import com.jd.open.api.sdk.response.ECLP.*;
import com.jd.open.api.sdk.request.ECLP.*;

public class InventoryHelper {

	String accessToken = ConfigHelper.getInstance().getAccessToken();
	String appKey = ConfigHelper.getInstance().getAppKey();
	String appSecret = ConfigHelper.getInstance().getAppSecret();
	String SERVER_URL = ConfigHelper.getInstance().getSERVER_URL();

	public EclpGoodsTransportGoodsInfoResponse Create(nc.vo.bd.material.MaterialVO data,String org_def1) throws Exception {
		if (data == null) {
			throw new Exception("传输的存货档案不能为空");
		}
		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey, appSecret);

		EclpGoodsTransportGoodsInfoRequest request = new EclpGoodsTransportGoodsInfoRequest();

		request.setDeptNo(org_def1);
		request.setIsvGoodsNo(data.getCode());
		request.setSpGoodsNo(data.getCode());
		request.setBarcodes(data.getCode());
		request.setThirdCategoryNo("9203");//	商品三级分类编码  
		request.setGoodsName(data.getName());
		request.setSafeDays(365);
		//		request.setAbbreviation( "jingdong" );
		//		request.setBrandNo( "jingdong" );
		//		request.setBrandName( "jingdong" );
		//		request.setManufacturer( "jingdong" );
		//		request.setProduceAddress( "jingdong" );
		//		request.setStandard( "jingdong" );
		//		request.setColor( "jingdong" );
		//		request.setSize( "jingdong" );
		//		request.setSizeDefinition( "jingdong" );
		//		request.setGrossWeight( 123 );
		//		request.setNetWeight( 123 );
		//		request.setLength( 123 );
		//		request.setWidth( 123 );
		//		request.setHeight( 123 );
				request.setInstoreThreshold( 0.33F );
				request.setOutstoreThreshold( 0.8F );
		//		request.setSerial( "jingdong" );
		//		request.setBatch( "jingdong" );
		//		request.setCheapGift( "jingdong" );
		//		request.setQuality( "jingdong" );
		//		request.setExpensive( "jingdong" );
		//		request.setLuxury( "jingdong" );
		//		request.setBreakable( "jingdong" );
		//		request.setLiquid( "jingdong" );
		//		request.setConsumables( "jingdong" );
		//		request.setAbnormal( "jingdong" );
		//		request.setImported( "jingdong" );
		//		request.setHealth( "jingdong" );
		//		request.setTemperature( "jingdong" );
		//		request.setTemperatureCeil( "jingdong" );
		//		request.setTemperatureFloor( "jingdong" );
		//		request.setHumidity( "jingdong" );
		//		request.setHumidityCeil( "jingdong" );
		//		request.setHumidityFloor( "jingdong" );
		//		request.setMovable( "jingdong" );
		//		request.setService3g( "jingdong" );
		//		request.setSample( "jingdong" );
		//		request.setOdor( "jingdong" );
		//		request.setSex( "jingdong" );
		//		request.setPrecious( "jingdong" );
		//		request.setMixedBatch( "jingdong" );
		//		request.setReserve1( "jingdong" );
		//		request.setReserve2( "jingdong" );
		//		request.setReserve3( "jingdong" );
		//		request.setReserve4( "jingdong" );
		//		request.setReserve5( "jingdong" );
		//		request.setReserve6( "jingdong" );
		//		request.setReserve7( "jingdong" );
		//		request.setReserve8( "jingdong" );
		//		request.setReserve9( "jingdong" );
		//		request.setReserve10( "jingdong" );
		//		request.setFashionNo( "jingdong" );

		EclpGoodsTransportGoodsInfoResponse response = client.execute(request);
		return response;
	}

	public EclpGoodsUpdateGoodsInfoResponse Update(nc.vo.bd.material.MaterialVO data) throws Exception {
		if (data == null) {
			throw new Exception("传输的存货档案不能为空");
		}
		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey, appSecret);
		EclpGoodsUpdateGoodsInfoRequest request = new EclpGoodsUpdateGoodsInfoRequest();

		request.setGoodsNo(data.getDef2());
		request.setSpGoodsNo(data.getCode());//销售平台商品编码 
		//request.setBarcodes( "jingdong" );
		request.setAbbreviation(data.getName());
		//request.setBrandNo( "jingdong" );
		//request.setBrandName( "jingdong" );
		//request.setManufacturer( "jingdong" );
		//request.setProduceAddress( "jingdong" );
		//request.setStandard( "jingdong" );
		//request.setColor( "jingdong" );
		//request.setSize( "jingdong" );
		//request.setSizeDefinition( "jingdong" );
		//request.setGrossWeight( 123 );
		//request.setNetWeight( 123 );
		//request.setLength( 123 );
		//request.setWidth( 123 );
		//request.setHeight( 123 );
		//request.setBatch( "jingdong" );
		//request.setCheapGift( "jingdong" );
		//request.setQuality( "jingdong" );
		//request.setExpensive( "jingdong" );
		//request.setLuxury( "jingdong" );
		//request.setBreakable( "jingdong" );
		//request.setLiquid( "jingdong" );
		//request.setConsumables( "jingdong" );
		//request.setAbnormal( "jingdong" );
		//request.setImported( "jingdong" );
		//request.setHealth( "jingdong" );
		//request.setTemperature( "jingdong" );
		//request.setTemperatureCeil( "jingdong" );
		//request.setTemperatureFloor( "jingdong" );
		//request.setHumidity( "jingdong" );
		//request.setHumidityCeil( "jingdong" );
		//request.setHumidityFloor( "jingdong" );
		//request.setMovable( "jingdong" );
		//request.setService3g( "jingdong" );
		//request.setSample( "jingdong" );
		//request.setOdor( "jingdong" );
		//request.setSex( "jingdong" );
		//request.setPrecious( "jingdong" );
		//request.setMixedBatch( "jingdong" );
		//request.setReserve1( "jingdong" );
		//request.setReserve2( "jingdong" );
		//request.setReserve3( "jingdong" );
		//request.setReserve4( "jingdong" );
		//request.setReserve5( "jingdong" );
		//request.setReserve6( "jingdong" );
		//request.setReserve7( "jingdong" );
		//request.setReserve8( "jingdong" );
		//request.setReserve9( "jingdong" );
		//request.setReserve10( "jingdong" );
		//request.setFashionNo( "jingdong" );

		EclpGoodsUpdateGoodsInfoResponse response = client.execute(request);

		return response;
	}

	public EclpGoodsQueryGoodsInfoResponse Query(nc.vo.bd.material.MaterialVO data,String org_def1) throws Exception {
		if (data == null) {
			throw new Exception("传输的存货档案不能为空");
		}
		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey, appSecret);

		EclpGoodsQueryGoodsInfoRequest request = new EclpGoodsQueryGoodsInfoRequest();

		request.setDeptNo(org_def1);
		request.setIsvGoodsNos(data.getCode());//ISV商品编码 
		request.setGoodsNos(data.getDef2());
		//		request.setQueryType( 2 ); 

		EclpGoodsQueryGoodsInfoResponse response = client.execute(request);
		return response;

	}

	public String CreateOrUpdate(nc.vo.bd.material.MaterialVO data,String org_def1) throws Exception {
		EclpGoodsQueryGoodsInfoResponse rs1 = this.Query(data,org_def1);
		if (CommonHelper.StringEqual(rs1.getCode(), "0")) {
			if (rs1.getGoodsInfoList() != null && rs1.getGoodsInfoList().size() > 0) {
				if(!CommonHelper.IsNullOrEmpty(data.getDef2())){
					EclpGoodsUpdateGoodsInfoResponse rs = this.Update(data);
					if (!CommonHelper.StringEqual(rs.getCode(), "0")) {
						throw new Exception("调用京东物料更新接口失败:" + rs.getMsg()+rs.getUrl());
					}
				}
				return rs1.getGoodsInfoList().get(0).getGoodsNo()[0];
			} else {
				EclpGoodsTransportGoodsInfoResponse rs = this.Create(data,org_def1);
				if (!CommonHelper.StringEqual(rs.getCode(), "0")) {
					throw new Exception("调用京东物料新增接口失败:" + rs.getMsg()+rs.getUrl());
				}
				return rs.getGoodsNo();
			}
		} else {
			throw new Exception("调用京东物料查询接口失败:" + rs1.getMsg()+rs1.getUrl());
		}
	}

}
