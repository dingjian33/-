package nc.wznc2jd.Plugin;

import nc.bs.businessevent.bd.BDCommonEvent;
import nc.bs.businessevent.BusinessEvent;
import nc.bs.businessevent.IBusinessEvent;
import nc.bs.businessevent.IEventType;


import java.util.ArrayList;
import nc.vo.pub.BusinessException;
import nc.wznc2jd.JDHelper.CommonHelper;
import nc.wznc2jd.JDHelper.InventoryHelper;

public class InventoryAfterAdd implements nc.bs.businessevent.IBusinessListener {
	public void doAction(IBusinessEvent event) throws BusinessException {
		//		int x = 0;
		try {
			
			//AggregatedValueObject[] value = getVOs(event);
			if (IEventType.TYPE_INSERT_AFTER.equals(event.getEventType())
					|| IEventType.TYPE_UPDATE_AFTER.equals(event.getEventType())) {
				BDCommonEvent e = (BDCommonEvent) event;
				Object[] value;
				if ((IEventType.TYPE_INSERT_AFTER.equals(event.getEventType())))
					value = e.getNewObjs();
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
						if (CommonHelper.ToBooleanFromString(issend)) {//�Ƿ񴫾���
							//		        			EclpGoodsTransportGoodsInfoResponse rs=helper.Create(data);
							//		        			if(!CommonHelper.StringEqual(rs.getCode(), "0")){
							//		        				throw new Exception("���� ���������쳣��"+rs.getMsg());
							//		        			}
							String jdcode = helper.CreateOrUpdate(data,org_def1);
							data.setDef2(jdcode);
							
							String sql = "update bd_material Set Def2='" + data.getDef2() + "' where pk_material='" + data.getPk_material() + "'";
							CommonHelper.getBaseDao().executeUpdate(sql);
						}
					}
				} catch (Exception ex) {
					throw ex;
				
			}}
		} catch (Exception e) {
			throw new BusinessException(e);
		}
	}
//	private Object[] getVOs(IBusinessEvent event)
//			throws BusinessException {
//		ValueObject obj = (ValueObject) event.getUserObject();
//		AggregatedValueObject[] vos = null;
//		if ((obj instanceof ICGeneralCommonEvent.ICGeneralCommonUserObj)) {
//			ICGeneralCommonEvent.ICGeneralCommonUserObj objs = (ICGeneralCommonEvent.ICGeneralCommonUserObj) obj;
//			vos = (AggregatedValueObject[]) objs.getNewObjects(); // ������ʱ��NewObjectΪ�գ��޸ĵ�ʱ��ı�NewObject,OldObject����
//		} else {
//			BusinessEvent.BusinessUserObj objs = (BusinessEvent.BusinessUserObj) obj;
//			vos = (AggregatedValueObject[]) objs.getUserObj();
//		}
//		if ((vos == null) || (vos.length == 0)) {
//			return null;
//		}
//		return vos;
	//}

}
