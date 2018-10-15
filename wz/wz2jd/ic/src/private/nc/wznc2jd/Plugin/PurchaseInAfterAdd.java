package nc.wznc2jd.Plugin;

import nc.bs.ic.general.businessevent.ICGeneralCommonEvent;
import nc.bs.businessevent.BusinessEvent;
import nc.bs.businessevent.IBusinessEvent;
import nc.edb.EDBHelper.EdbGetInfo;
import nc.edb.Helper.ParseXml;
import nc.edb.Helper.ResponseAddXml;
import java.util.ArrayList;
import com.jd.open.api.sdk.response.ECLP.EclpPoAddPoOrderResponse;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.ValueObject;
import nc.wznc2jd.JDHelper.*;

//   ������
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
			 * Integer a=1; if(a.equals(1)) throw new Exception("���������������");
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
					String storeType = CommonHelper
							.GetWarehouse(data.getBVO()[0].getPk_receivestore());// ��ȡ�ֿ�����
					
					if (!CommonHelper.GetBoolean(data.getHVO().getBisback())) {// �˻�
						if ("����".equals(storeType)) {
							String org_def1 = CommonHelper.Getdef1(data.getHVO()
									.getPk_org());// ��ҵ������
							EclpPoAddPoOrderResponse rs = helper.Create(data,
									org_def1);
							if (!CommonHelper.StringEqual(rs.getCode(), "0")) {
								throw new Exception("�ɹ����� ���������쳣��"
										+ rs.getMsg() + rs.getUrl());
							} else {
								data.getHVO().setVdef19("Y");
								data.getHVO().setVdef20(rs.getPoOrderNo());
								String sql = "update po_arriveorder Set Vdef19='"
										+ data.getHVO().getVdef19()
										+ "',Vdef20='"
										+ data.getHVO().getVdef20()
										+ "' where pk_arriveorder='"
										+ data.getHVO().getPk_arriveorder()
										+ "'";
								CommonHelper.getBaseDao().executeUpdate(sql);
							}
						} else if ("ǧͨ".equals(storeType)) {
							String s = EdbGetInfo.edbInStoreAdd(data);
							//String s ="<?xml version=\"1.0\" encoding=\"utf-8\"?><items><totalResults>1</totalResults><Rows><is_success>True</is_success><response_Code>200</response_Code><response_Msg>��ⵥ����ɹ�</response_Msg><field>DH2018101200000028</field></Rows></items>";
							ResponseAddXml px = ParseXml.parseAddXMl(s);
							if ("200".equals(px.getResponse_Code())) {
								data.getHVO().setVdef19("Y");
								String sql = "update po_arriveorder Set Vdef19='"
										+ data.getHVO().getVdef19()
										+ "' where pk_arriveorder='"
										+ data.getHVO().getPk_arriveorder()
										+ "'";
								CommonHelper.getBaseDao().executeUpdate(sql);
							} else {
								throw new Exception("������⴫��EDB�����쳣��"
										+ px.getResponse_Msg());
							}
						}
					} else {
						throw new Exception("�ɹ������쳣��" + "�����Ѿ��˻�");
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
			vos = (AggregatedValueObject[]) objs.getNewObjects(); // ������ʱ��NewObjectΪ�գ��޸ĵ�ʱ��ı�NewObject,OldObject����
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
