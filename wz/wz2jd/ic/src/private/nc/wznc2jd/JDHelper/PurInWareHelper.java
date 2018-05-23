package nc.wznc2jd.JDHelper;

import java.util.ArrayList;
import java.util.HashMap;

import nc.vo.pu.m23.entity.ArriveItemVO;
import nc.vo.pubapp.pattern.data.IRowSet;

import com.jd.open.api.sdk.*;
import com.jd.open.api.sdk.response.ECLP.*;
import com.jd.open.api.sdk.request.ECLP.*;

import nc.vo.pub.lang.UFDouble;

public class PurInWareHelper {

	String SERVER_URL = ConfigHelper.getInstance().getSERVER_URL();
	String accessToken = ConfigHelper.getInstance().getAccessToken();
	String appKey = ConfigHelper.getInstance().getAppKey();
	String appSecret = ConfigHelper.getInstance().getAppSecret();

	public EclpPoQueryPoOrderResponse Query(ArrayList<String> poOrderNos)
			throws Exception {
		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey,
				appSecret);

		EclpPoQueryPoOrderRequest request = new EclpPoQueryPoOrderRequest();
		request.setPoOrderNo(GetStr(poOrderNos));
		EclpPoQueryPoOrderResponse response = client.execute(request);
		return response;

	}

	public EclpPoAddPoOrderResponse Create(nc.vo.pu.m23.entity.ArriveVO data,String org_def1)
			throws Exception {
		// ArrayList<String> codes = new ArrayList<String>();
		ArrayList<String> ids = new ArrayList<String>();
		ArrayList<String> qtys = new ArrayList<String>();
		ArrayList<String> status = new ArrayList<String>();
		for (int i = 0; i < data.getBVO().length; i++) {
			ArriveItemVO body = data.getBVO()[i];
			ids.add(body.getPk_srcmaterial());// 物料
			UFDouble num = body.getNastnum();// .getNshouldassistnum();到货数量
			if (num != null)
				qtys.add(String.valueOf(num.intValue()));
			else
				qtys.add("0");
			status.add("1");
		}

		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey,
				appSecret);

		EclpPoAddPoOrderRequest request = new EclpPoAddPoOrderRequest();

		request.setSpPoOrderNo(data.getHVO().getVbillcode());// 外部采购订单号
		request.setDeptNo(org_def1);// 事业部EBU4418046513690
		// request.setWhNo("110007600");
		request.setWhNo(CommonHelper.GetWhJdCode(data.getBVO()[0]
				.getPk_receivestore()));// 仓库编码
		request.setSupplierNo("EMS4418046513631");// 供应商
		request.setDeptGoodsNo(ConvertMaterial(ids));
		request.setNumApplication(GetStr(qtys));
		request.setGoodsStatus(GetStr(status));
		// request.setBarCodeType( "jingdong,yanfa,pop" );

		EclpPoAddPoOrderResponse response = client.execute(request);
		return response;

	}

	public EclpPoCancalPoOrderResponse Delete(nc.vo.pu.m23.entity.ArriveVO data)
			throws Exception {
		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey,
				appSecret);

		EclpPoCancalPoOrderRequest request = new EclpPoCancalPoOrderRequest();
		request.setPoOrderNo(data.getHVO().getVdef13());

		EclpPoCancalPoOrderResponse response = client.execute(request);
		return response;
	}

	public static String ConvertMaterial(ArrayList<String> ids)
			throws Exception {
		HashMap<String, String> results = GetMaterialCodes(ids);
		ArrayList<String> codes = new ArrayList<String>();
		for (int i = 0; i < ids.size(); i++) {
			String key = ids.get(i);
			if (results.containsKey(key))
				codes.add(results.get(key));
			else
				codes.add("");
		}
		return GetStr(codes);
	}

	public static HashMap<String, String> GetMaterialCodes(ArrayList<String> ids)
			throws Exception {

		HashMap<String, String> results = new HashMap<String, String>();
		// for(int i=0;i<ids.size();i++){
		// if(!results.containsKey(ids.get(i))){
		// results.put(ids.get(i), "");
		// }
		// }
		StringBuilder builder = new StringBuilder();
		// int size = this.itemids.size();
		builder.append(" select A.DEF2,A.pk_material,A.code from bd_material A  ");
		builder.append(" where dr=0 ");
		builder.append(CommonHelper.GetInStr("A.pk_material", ids));

		IRowSet rs = CommonHelper.sqlUtil.query(builder.toString());
		while (rs.next()) {
			String jdcode = rs.getString(0);//
			String id = rs.getString(1);// id
			String code = rs.getString(2);//
			if (CommonHelper.IsNullOrEmpty(jdcode)) {
				throw new Exception("编码为" + code + "的物料的京东商品编码为空");
			}
			results.put(id, jdcode);
		}
		return results;

	}

	public static String GetStr(ArrayList<String> strarray) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strarray.size(); i++) {
			String s = strarray.get(i);
			if (i > 0) {
				sb.append(",");
			}
			sb.append(s);
		}
		return sb.toString();
	}
}
