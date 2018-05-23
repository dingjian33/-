package nc.wznc2jd.Plugin;

import nc.bs.businessevent.bd.BDCommonEvent;
import nc.bs.businessevent.IBusinessEvent;
import nc.bs.businessevent.IEventType;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jd.open.api.sdk.response.ECLP.EclpGoodsTransportGoodsInfoResponse;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.pmpub.uap.util.ExceptionUtils;
import nc.vo.pub.BusinessException;
import nc.wznc2jd.JDHelper.CommonHelper;
import nc.wznc2jd.JDHelper.InventoryHelper;

public class InventoryAfterAdd implements nc.bs.businessevent.IBusinessListener {
	public void doAction(IBusinessEvent event) throws BusinessException {
		//		int x = 0;
		try {
			if (IEventType.TYPE_INSERT_AFTER.equals(event.getEventType())
					|| IEventType.TYPE_UPDATE_AFTER.equals(event.getEventType())) {
				BDCommonEvent e = (BDCommonEvent) event;
				Object[] value;
				if ((IEventType.TYPE_INSERT_AFTER.equals(event.getEventType())))
					value = e.getOldObjs();
				else
					value = e.getNewObjs();
				if (null == value) {
					return;
				}
				try {
					ArrayList<nc.vo.bd.material.MaterialVO> datas = new ArrayList<nc.vo.bd.material.MaterialVO>();
					for (int i = 0; i < value.length; i++) {
						datas.add((nc.vo.bd.material.MaterialVO) value[i]);
					}
					InventoryHelper helper = new InventoryHelper();
					for (int i = 0; i < value.length; i++) {
						nc.vo.bd.material.MaterialVO data = datas.get(i);
						String org_def1 = CommonHelper.Getdef1(data.getPk_org());
						String issend = data.getDef1();
						if (CommonHelper.ToBooleanFromString(issend)) {
							//		        			EclpGoodsTransportGoodsInfoResponse rs=helper.Create(data);
							//		        			if(!CommonHelper.StringEqual(rs.getCode(), "0")){
							//		        				throw new Exception("传给 京东发生异常："+rs.getMsg());
							//		        			}
							String jdcode = helper.CreateOrUpdate(data,org_def1);
							data.setDef2(jdcode);
							
							String sql = "update bd_material Set Def2='" + data.getDef2() + "' where pk_material='" + data.getPk_material() + "'";
							CommonHelper.getBaseDao().executeUpdate(sql);
						}
					}
				} catch (Exception ex) {
					throw ex;
				}
			}
		} catch (Exception e) {
			throw new BusinessException(e);
		}
	}

}
