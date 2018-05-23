package nc.wznc2jd.JDHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.uap.pf.IPfExchangeService;
import nc.ui.ml.NCLangRes;
import nc.vo.ic.pub.define.ICPubMetaNameConst;
import nc.vo.ic.pub.util.ValueCheckUtil;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pubapp.util.VORowNoUtils;
import nc.vo.uap.pf.PFRuntimeException;

import com.jd.open.api.sdk.*;
import com.jd.open.api.sdk.response.ECLP.*;
import com.jd.open.api.sdk.request.ECLP.*;

public class ReturnInHelper {

	String SERVER_URL = ConfigHelper.getInstance().getSERVER_URL();
	String accessToken = ConfigHelper.getInstance().getAccessToken();
	String appKey = ConfigHelper.getInstance().getAppKey();
	String appSecret = ConfigHelper.getInstance().getAppSecret();

	public void Create(nc.vo.ic.m4c.entity.SaleOutVO data) {
		//		nc.ui.pub.pf.PfUtilClient.getRetVos();
	}

	public EclpRtwQueryRtwResponse Query(ArrayList<String> soOrderNos) throws Exception {

		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey, appSecret);
		EclpRtwQueryRtwRequest request = new EclpRtwQueryRtwRequest();
		request.setEclpSoNo(PurInWareHelper.GetStr(soOrderNos));
		EclpRtwQueryRtwResponse response = client.execute(request);
		return response;
	}

	private static IPfExchangeService getExchangeService() {

		//		nc.impl.uap.pf.PfExchangeServiceImpl i=null;
		IPfExchangeService exchangeService = null;
		if (exchangeService == null)
			exchangeService = NCLocator.getInstance().lookup(IPfExchangeService.class);
		return exchangeService;
	}

	private static AggregatedValueObject[] changeVos(AggregatedValueObject[] vos, int classifyMode) {
		AggregatedValueObject[] tmpRetVos = null;

		try {
			tmpRetVos = getExchangeService().runChangeDataAryNeedClassify("", "", vos, null, classifyMode);
		} catch (BusinessException ex) {
			Logger.error(ex.getMessage(), ex);
			throw new PFRuntimeException(NCLangRes.getInstance().getStrByID("pfworkflow1", "PfUtilClient-000004", null,
					new String[] { ex.getMessage() })/* VO交换错误：{0} */, ex);
		}

		return tmpRetVos;
	}

	/**
	 * 重新处理行号。原因：转单之后，注册了数据交换后处理类的场景，合单前按单补了行号，合单后导致行号重复
	 * 
	 * @param retvos
	 * @return
	 */
	protected AggregatedValueObject[] processRowNO(AggregatedValueObject[] retvos) {
		if (ValueCheckUtil.isNullORZeroLength(retvos))
			return retvos;

		for (AggregatedValueObject bill : retvos) {
			// 设置行号
			CircularlyAccessibleValueObject[] bodys = bill.getChildrenVO();
			if (ValueCheckUtil.isNullORZeroLength(bodys))
				continue;
			for (CircularlyAccessibleValueObject body : bodys) {
				body.setAttributeValue(ICPubMetaNameConst.CROWNO, null);
			}

			VORowNoUtils.setVOsRowNoByRule(bodys, ICPubMetaNameConst.CROWNO);

		}
		return retvos;
	}
}
