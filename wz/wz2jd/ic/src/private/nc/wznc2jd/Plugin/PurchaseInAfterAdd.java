package nc.wznc2jd.Plugin;

import nc.bs.ic.general.businessevent.ICGeneralCommonEvent;
import nc.bs.businessevent.BusinessEvent;
import nc.bs.businessevent.IBusinessEvent;
import nc.bs.businessevent.IEventType;

import java.util.ArrayList;

import com.jd.open.api.sdk.response.ECLP.EclpPoAddPoOrderResponse;

import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.ValueObject;
import nc.vo.pub.lang.UFBoolean;
import nc.wznc2jd.JDHelper.*;

//   到货单
public class PurchaseInAfterAdd implements
		nc.bs.businessevent.IBusinessListener {
	public void doAction(IBusinessEvent event) throws BusinessException {
		try {

			AggregatedValueObject[] value = getVOs(event);
			/*
			 * if (IEventType.TYPE_INSERT_AFTER.equals(event.getEventType())) {
			 * ICGeneralCommonEvent e = (ICGeneralCommonEvent) event; Object[]
			 * value = e.getOldObjs(); if (null == value) { return; }
			 */
			/*
			 * Integer a=1; if(a.equals(1)) throw new Exception("物料新增插件进入");
			 */

			try {

				ArrayList<nc.vo.pu.m23.entity.ArriveVO> datas = new ArrayList<nc.vo.pu.m23.entity.ArriveVO>();
				for (int i = 0; i < value.length; i++) {
					datas.add((nc.vo.pu.m23.entity.ArriveVO) value[i]);
				}
				// TestHelper helper=new TestHelper();
				// helper.Create();
				PurInWareHelper helper = new PurInWareHelper();
				for (int i = 0; i < value.length; i++) {
					nc.vo.pu.m23.entity.ArriveVO data = datas.get(i);
					// String
					// whcode=CommonHelper.GetWhCode(data.getHead().getCwarehouseid());
					UFBoolean isjd = CommonHelper.GetWhIsJd(data.getBVO()[0]
							.getPk_receivestore());
					String org_def1 = CommonHelper.Getdef1(data.getHVO()
							.getPk_org());
					if (CommonHelper.GetBoolean(isjd)
							&& !CommonHelper.GetBoolean(data.getHVO()
									.getBisback())) {// 退货
						EclpPoAddPoOrderResponse rs = helper.Create(data,org_def1);
						if (!CommonHelper.StringEqual(rs.getCode(), "0")) {
							throw new Exception("采购传给 京东发生异常：" + rs.getMsg()
									+ rs.getUrl());
						} else {
							data.getHVO().setVdef12("Y");
							data.getHVO().setVdef13(rs.getPoOrderNo());
							String sql = "update po_arriveorder Set Vdef12='"
									+ data.getHVO().getVdef12() + "',Vdef13='"
									+ data.getHVO().getVdef13()
									+ "' where pk_arriveorder='"
									+ data.getHVO().getPk_arriveorder() + "'";
							CommonHelper.getBaseDao().executeUpdate(sql);

						}
					} else {
						throw new Exception("采购传给 京东发生异常：" + "仓库不属于京东");
					}
				}

			} catch (Exception ex) {
				throw ex;
			}
			// }
		} catch (Exception e) {
			throw new BusinessException(e);
		}
	}

	private AggregatedValueObject[] getVOs(IBusinessEvent event)
			throws BusinessException {
		ValueObject obj = (ValueObject) event.getUserObject();
		AggregatedValueObject[] vos = null;
		if ((obj instanceof ICGeneralCommonEvent.ICGeneralCommonUserObj)) {
			ICGeneralCommonEvent.ICGeneralCommonUserObj objs = (ICGeneralCommonEvent.ICGeneralCommonUserObj) obj;
			vos = (AggregatedValueObject[]) objs.getNewObjects(); // 新增的时候NewObject为空，修改的时候改变NewObject,OldObject不变
		} else {
			BusinessEvent.BusinessUserObj objs = (BusinessEvent.BusinessUserObj) obj;
			vos = (AggregatedValueObject[]) objs.getUserObj();
		}
		if ((vos == null) || (vos.length == 0)) {
			return null;
		}
		return vos;
	}

}
