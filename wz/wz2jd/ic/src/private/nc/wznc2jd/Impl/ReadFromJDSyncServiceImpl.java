package nc.wznc2jd.Impl;

import nc.vo.pu.m23.entity.ArriveHeaderVO;
import nc.vo.pu.m23.entity.ArriveItemVO;
import nc.vo.pu.m23.entity.ArriveVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.data.IRowSet;
import nc.vo.so.m4331.entity.DeliveryBVO;
import nc.vo.so.m4331.entity.DeliveryHVO;
import nc.vo.so.m4331.entity.DeliveryVO;
import nc.wznc2jd.Helper.DataAccessHelper;
import nc.wznc2jd.Helper.DataSqlQuery;
import nc.wznc2jd.JDHelper.CommonHelper;
import nc.wznc2jd.JDHelper.PurInWareHelper;
import nc.wznc2jd.JDHelper.ReturnInHelper;
import nc.wznc2jd.JDHelper.SaleOutHelper;
import nc.vo.hzsb.pub.MapList;
import nc.vo.hzsb.sbvo.AggSBNCBillHVO;
import nc.vo.hzsb.sbvo.SBNCBillBVO;
import nc.vo.hzyb.ybvo.AggYBNCBillHVO;
import nc.vo.hzyb.ybvo.YBNCBillBVO;
import nc.vo.ic.m45.entity.PurchaseInBodyVO;
import nc.vo.ic.m45.entity.PurchaseInHeadVO;
import nc.vo.ic.m45.entity.PurchaseInVO;
import nc.wznc2jd.itf.*;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.impl.pubapp.pattern.data.bill.BillInsert;
import nc.impl.pubapp.pattern.data.bill.BillQuery;
import nc.impl.pubapp.pattern.data.bill.BillUpdate;
import nc.vo.ic.m4c.entity.SaleOutBodyVO;
import nc.vo.ic.m4c.entity.SaleOutVO;

import com.jd.open.api.sdk.domain.ECLP.EclpOpenService.OrderDefaultResult;
import com.jd.open.api.sdk.domain.ECLP.EclpOpenService.OrderDetail;
import com.jd.open.api.sdk.domain.ECLP.EclpOpenService.OrderDetailResult;
import com.jd.open.api.sdk.domain.ECLP.EclpOpenService.PoItemModel;
import com.jd.open.api.sdk.domain.ECLP.EclpOpenService.QueryPoModel;
import com.jd.open.api.sdk.domain.ECLP.EclpOpenService.RtwResult;
import com.jd.open.api.sdk.response.ECLP.*;
import com.jd.open.api.sdk.domain.ECLP.EclpOpenService.OrderStatus;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ReadFromJDSyncServiceImpl implements ReadFromJDSyncServiceItf {
	private static final long serialVersionUID = 1586262846504659456L;

	/**
	 * ������������֮����������
	 * 
	 * @param smdate
	 *            ��С��ʱ��
	 * @param bdate
	 *            �ϴ��ʱ��
	 * @return �������
	 * @throws ParseException
	 */
	public static int daysBetween(Date smdate, Date bdate)
			throws java.text.ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		smdate = sdf.parse(sdf.format(smdate));
		bdate = sdf.parse(sdf.format(bdate));
		Calendar cal = Calendar.getInstance();
		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();
		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);

		return Integer.parseInt(String.valueOf(between_days));
	}

	int maxdays = 30;

	public static Boolean CheckCanDelivery(String par) {
		return jdstatus.contains(par);
	}

	public static Boolean CheckCanDelivery(ArrayList<String> pars) {
		for (int i = 0; i < pars.size(); i++) {
			String par = pars.get(i);
			if (CheckCanDelivery(par)) {
				return true;
			}
		}
		return false;
	}

	private static HashSet<String> jdstatus = new HashSet<String>() {
		/**
		 * 
		 */

		{
			add("10017");// ����
			add("10018");// ��Ʒ�Ѵ��
			add("10019");// ���ӷ���
			add("10020");// ��������
			add("10032");// �ּ�����
			add("10033");// վ������
			add("10034");// ��Ͷ
		}
	};

	@Override
	public void createPurchaseInFromJD() throws BusinessException {
		// TODO �Զ����ɵķ������

		try {
			// BillInsert<PurchaseInVO> insert = new BillInsert<PurchaseInVO>();
			BillQuery<ArriveVO> arrive = new BillQuery<ArriveVO>(ArriveVO.class);
			DataSqlQuery query = new DataSqlQuery();
			StringBuilder builder = new StringBuilder();
			builder.append(" select A.VDEF13,A.pk_arriveorder from po_arriveorder A ");
			builder.append(" where A.Vdef12='Y' and A.Fbillstatus ='3' and A.bisback ='N' and A.Dr=0 and A.VDEF13!='~' and A.VDEF13 is not null and A.VDEF13 !='null'");
			ArrayList<String> ids = new ArrayList<String>();
			ArrayList<String> nos = new ArrayList<String>();

			ArrayList<String> itemids = new ArrayList<String>();
			HashMap<String, PoItemModel> modelmap = new HashMap<String, PoItemModel>();

			HashMap<String, String> noidmap = new HashMap<String, String>();
			IRowSet set = CommonHelper.sqlUtil.query(builder.toString());
			while (set.next()) {
				String no = set.getString(0);// vdef13
				if (!nos.contains(no)) {
					nos.add(no);
					String id = set.getString(1);// id
					noidmap.put(no, id);
				}
			}

			if (nos != null && nos.size() > 0) {
				PurInWareHelper helper = new PurInWareHelper();
				EclpPoQueryPoOrderResponse rs = helper.Query(nos);// ��ѯ�����ĵ���
				if (!CommonHelper.StringEqual(rs.getCode(), "0")) {
					throw new Exception("������ѯ�ɹ���ⵥ��Ϣ�����쳣��" + rs.getMsg());
				}
				List<QueryPoModel> results = rs.getQueryPoModelList();
				HashMap<String, QueryPoModel> resultmap = new HashMap<String, QueryPoModel>();
				for (int i = 0; i < results.size(); i++) {
					QueryPoModel result = results.get(i);
					if (CommonHelper.StringEqual(result.getPoOrderStatus(),
							"70")) {
						String no = result.getPoOrderNo();// �������� ==vdef13
						resultmap.put(no, result);
						if (nos.contains(no)) {
							ids.add(noidmap.get(no));// ���ݺ�
						}
						for (int j = 0; j < result.getPoItemModelList().size(); j++) {
							PoItemModel model = result.getPoItemModelList()
									.get(j);
							String goodno = model.getGoodsNo();
							String key = no + "_" + goodno;// vdef13+����id
							if (!modelmap.containsKey(key)) {
								modelmap.put(key, model);
							}
						}
					}
				}
				// ids.add(noidmap.get("EPL4418048215038"));// ���ݺ�
				if (ids.size() > 0) {
					/*
					 * for (int i = 0; i < ids.size(); i++) { PurchaseInVO[] vo
					 * = null; PurchaseInHeadVO po=null; PurchaseInBodyVO[]
					 * body=null; //po.getHead().setCgeneralhid (ids.get(i));
					 * po.setPk_group(query.getPk_group(ids.get(i)));
					 * po.setPk_org(query.getPk_org(ids.get(i)));
					 * po.setPk_org_v(query.getPk_org_v(ids.get(i)));
					 * 
					 * }
					 */

					ArriveVO[] arr = arrive.query(ids.toArray((new String[ids
							.size()])));
					UFDateTime now = new UFDateTime();

					if (arr != null && arr.length > 0) {
						for (int i = 0; i < arr.length; i++) {
							PurchaseInHeadVO po = new PurchaseInHeadVO();
							ArrayList<PurchaseInBodyVO> bodyvo = new ArrayList<PurchaseInBodyVO>();

							ArriveHeaderVO arriveHVO = arr[i].getHVO();
							po.setPk_org(arriveHVO.getPk_org());
							po.setPk_org_v(arriveHVO.getPk_org_v());
							po.setBillmaker(arriveHVO.getBillmaker());// �Ƶ���
							po.setCbizid(arriveHVO.getPk_pupsndoc());// �ɹ�Ա
							po.setCbiztype(arriveHVO.getPk_busitype());// ҵ������
							// ����ɱ���
							po.setCdptid(arriveHVO.getPk_dept());// �ɹ��������°汾
							po.setCdptvid(arriveHVO.getPk_dept_v());// �ɹ�����

							po.setCfanaceorgoid(arriveHVO.getPk_org());// ���������֯���°汾
							po.setCfanaceorgvid(arriveHVO.getPk_org_v());// ���������֯

							po.setCpayfinorgoid(arriveHVO.getPk_org());// Ӧ��������֯���°汾
							po.setCpayfinorgvid(arriveHVO.getPk_org_v());// Ӧ��������֯

							po.setCorpoid(arriveHVO.getPk_org());// ��˾���°汾
							po.setCorpvid(arriveHVO.getPk_org_v());// ��˾

							po.setCpurorgoid(arriveHVO.getPk_org());// �ɹ���֯
							po.setCpurorgvid(arriveHVO.getPk_org_v());// �ɹ���֯���°汾
							po.setCpayfinorgoid(arriveHVO.getPk_org());// Ӧ��������֯���°汾
							po.setCpayfinorgvid(arriveHVO.getPk_org_v());// Ӧ��������֯
							// ����ʱ�� ����ʱ�� �Ƶ�����
							// po.setNtotalnum(arriveHVO.getNtotalastnum());//������

							po.setCreator(arriveHVO.getBillmaker());// ������
							po.setCtrantypeid(arriveHVO.getCtrantypeid());// ��������
							po.setCvendorid(arriveHVO.getPk_supplier());// ��Ӧ��
							po.setVbillcode(arriveHVO.getVbillcode());// ���ݵ���
							po.setVdef13(arriveHVO.getVdef13());

							ArriveItemVO[] bvo = arr[i].getBVO();
							for (ArriveItemVO bodys : bvo) {
								po.setCrececountryid(bodys.getCrececountryid());// ����
								po.setCsendcountryid(bodys.getCrececountryid());
								po.setCtaxcountryid(bodys.getCrececountryid());
								po.setCwarehouseid(bodys.getPk_receivestore());// �ֿ�
								String itemid = bodys.getPk_material();// ���ϱ���
								String no = arriveHVO.getVdef13();
								if (!itemids.contains(itemid)) {
									itemids.add(itemid);
								}
								HashMap<String, String> itemidcodes = PurInWareHelper
										.GetMaterialCodes(itemids);
								if (itemidcodes.containsKey(itemid)) {
									String itemcode = itemidcodes.get(itemid);
									String key = no + "_" + itemcode;
									if (modelmap.containsKey(key)) {
										PoItemModel model = modelmap.get(key);
										UFDouble qty = CommonHelper
												.ToUFDoubleFromStr(model
														.getRealInstoreQty());
										// UFDouble qty = CommonHelper
										// .ToUFDoubleFromStr("55");
										po.setNtotalnum(qty);// ������

										PurchaseInBodyVO body = new PurchaseInBodyVO();
										body.setCastunitid(bodys
												.getCastunitid());// ��λ
										body.setCbodywarehouseid(bodys
												.getPk_receivestore()); // �ֿ�
										body.setCcurrencyid(bodys
												.getCcurrencyid());// ���ұ���
										body.setCfanaceorgoid(bodys.getPk_org());// ��֯
										body.setCfirstbillbid(bodys
												.getCsourcebid());// Դͷ���ݱ�������
										body.setCfirstbillhid(bodys
												.getCsourceid());// Դͷ���ݱ�ͷ����
										body.setCfirsttranstype(bodys
												.getVfirsttrantype());// Դͷ���ݽ�������
										body.setCfirsttype(bodys
												.getCsourcetypecode());// Դͷ��������
										body.setCmaterialoid(bodys
												.getPk_material());// ����
										body.setCmaterialvid(bodys
												.getPk_material());// ���ϱ���
										body.setCorigcurrencyid(bodys
												.getCcurrencyid());// ����
										body.setCreqstoorgoid(bodys.getPk_org());// ��������֯���°汾
										body.setCreqstoorgvid(bodys
												.getPk_org_v());// ��������֯
										body.setCqtunitid(bodys.getCunitid());// ���۵�λ
										body.setCorpoid(bodys.getPk_org());// ��˾���°汾
										body.setCorpvid(bodys.getPk_org_v());// ��˾
										body.setCrowno(bodys.getCrowno());// �к�
										body.setCsourcebillbid(bodys
												.getPk_arriveorder_b());// ��Դ���ݱ���������
										body.setCsourcebillhid(bodys
												.getPk_arriveorder());// ��Դ���ݱ�ͷ����
										body.setCsourcetranstype(arriveHVO
												.getCtrantypeid()); // ��Դ���ݽ�������
										body.setCsourcetype("23");// ��Դ��������
										// ��Դ�������ͱ���
										body.setCsrcmaterialoid(bodys
												.getPk_material());// ��Դ����
										body.setCsrcmaterialvid(bodys
												.getPk_material());// ��Դ���ϱ���
										// ˰��
										body.setCunitid(bodys.getCunitid()); // ����λ
										body.setCvendorid(arriveHVO
												.getPk_supplier());// ��Ӧ��
										body.setDbizdate(now.getDate());// �������

										// �����漰�������
										body.setNassistnum(qty);// ʵ������
										// ncalcostmny ncaltaxmny
										// nchangestdrate nitemdiscountrate
										body.setNcalcostmny(bodys.getNmny()); // �Ƴɱ����
										body.setNcaltaxmny(bodys.getNmny());// ��˰���
										body.setNchangestdrate(bodys
												.getNexchangerate());// �۱�����
										// body.setNitemdiscountrate();
										// �ۿ�

										body.setNmny(bodys.getNmny());// ������˰���
										body.setNnum(qty);// ʵ��������
										body.setNnetprice(bodys.getNprice());// ��������˰����
										body.setNorigmny(bodys.getNmny());// ��˰���
										body.setNorigprice(bodys
												.getNorigprice());// ����˰����
										body.setNorignetprice(bodys
												.getNorigprice());// ����˰����
										body.setNorigtaxmny(bodys
												.getNorigtaxmny());// ��˰�ϼ�
										body.setNorigtaxnetprice(bodys
												.getNorigtaxprice());// ����˰����
										body.setNorigtaxprice(bodys
												.getNorigtaxprice());// ����˰����

										body.setNprice(bodys.getNprice());// ��������˰����
										body.setNqtnetprice(bodys.getNprice());// ������˰����
										body.setNqtorignetprice(bodys
												.getNprice());// ��˰����
										body.setNqtorigprice(bodys.getNprice());// ��˰����

										body.setNqtorigtaxnetprice(bodys
												.getNorigtaxprice());// ��˰����
										body.setNqtorigtaxprice(bodys
												.getNorigtaxprice());// ��˰����

										body.setNqtprice(bodys.getNprice());// ������˰����
										body.setNqttaxnetprice(bodys
												.getNorigtaxprice());// ���Һ�˰����
										body.setNqttaxprice(bodys
												.getNorigtaxprice());// ���Һ�˰����

										body.setNqtunitnum(bodys.getNnum());// ��������
										body.setNshouldassistnum(bodys
												.getNnum());// Ӧ������
										body.setNshouldnum(bodys.getNnum());// Ӧ��������
										// ˰��
										body.setNtaxmny(bodys.getNorigtaxmny());// ���Ҽ�˰�ϼ�
										body.setNtaxnetprice(bodys
												.getNorigtaxprice());// �����Һ�˰����
										body.setNtaxprice(bodys
												.getNorigtaxprice());// �����Һ�˰����
										body.setNtaxrate(bodys.getNtaxrate());// ˰��

										body.setPk_creqwareid(bodys
												.getPk_receivestore());// ����ֿ�
										body.setPk_group(bodys.getPk_group());
										body.setPk_org(bodys.getPk_org());
										body.setPk_org_v(bodys.getPk_org_v());
										body.setVfirstbillcode(bodys
												.getVfirstcode());// Դͷ���ݺ�
										body.setVfirstrowno(bodys
												.getVfirstrowno());// Դͷ�����к�
										// ��Դ���ݺź��к�
										body.setStatus(VOStatus.NEW);
										bodyvo.add(body);

									}
								}

							}
							PurchaseInVO[] vos = this.buildBills(po, bodyvo);

							try {
								NCLocator
										.getInstance()
										.lookup(nc.pubitf.ic.m45.api.IPurchaseInMaintainAPI.class)
										.insertBills(vos);
								NCLocator
										.getInstance()
										.lookup(nc.pubitf.ic.m45.api.IPurchaseInMaintainAPI.class)
										.signBills(vos);
								String sql = "update po_arriveorder Set Fbillstatus ='1' where pk_arriveorder='"
										+ arriveHVO.getPk_arriveorder() + "'";
								CommonHelper.getBaseDao().executeUpdate(sql);

							} catch (Exception ex) {
								try {
									if (UFDateTime.getDaysBetween(arr[i]
											.getHVO().getCreationtime(), now) > maxdays) {// ��ֹ��������һֱ��ȡ
										String sql = "update po_arriveorder Set Vdef12='"
												+ arriveHVO.getVdef12()
												+ "',Vdef13='"
												+ arriveHVO.getVdef13()
												+ "' where pk_arriveorder='"
												+ arriveHVO.getPk_arriveorder() + "'";
										CommonHelper.getBaseDao()
												.executeUpdate(sql);
									}
								} catch (Exception e1) {
									Logger.error("����jd�������Ťת״̬Fʱ��������:"
											+ e1.getMessage());
									Logger.error(e1);
								}
								Logger.error("����jd������ݷ�������:" + ex.getMessage());
								Logger.error(ex);
							}
							
						}
					}
				}
			}
			

		} catch (Exception ex) {
			Logger.error("��ȡjd������ݷ�������:" + ex.getMessage());
			Logger.error(ex);
		}
	}

	public PurchaseInVO[] buildBills(PurchaseInHeadVO po,
			ArrayList<PurchaseInBodyVO> body) {
		List<PurchaseInVO> lst = new ArrayList<PurchaseInVO>();
		MapList<String, PurchaseInBodyVO> ml = new MapList<String, PurchaseInBodyVO>();
		for (PurchaseInBodyVO bvo : body) {
			String str = bvo.getCgeneralbid();
			ml.put(str, bvo);
		}
		Iterator<String> it = ml.keySet().iterator();
		while (it.hasNext()) {
			String key = ((String) it.next());
			List<PurchaseInBodyVO> bodys = ml.get(key);
			if (bodys != null) {
				PurchaseInVO bill = new PurchaseInVO();
				bill.setParentVO(po);
				bill.setChildrenVO(bodys.toArray(new PurchaseInBodyVO[0]));
				lst.add(bill);
			}
		}
		return lst.toArray(new PurchaseInVO[0]);

	}

	@Override
	public void readPurchaseInFromJD() throws BusinessException {
		// TODO �Զ����ɵķ������

	}

	@Override
	public void readSaleOutFromJD() throws BusinessException {
		// TODO �Զ����ɵķ������

	}

	@Override
	public void readDeliveryFromJD() throws BusinessException {
		// TODO �Զ����ɵķ������

	}

	private PurchaseInBodyVO[] createBody(ArriveItemVO[] bvo) {
		return null;

	}
}
