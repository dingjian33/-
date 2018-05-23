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
	 * 计算两个日期之间相差的天数
	 * 
	 * @param smdate
	 *            较小的时间
	 * @param bdate
	 *            较大的时间
	 * @return 相差天数
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
			add("10017");// 复核
			add("10018");// 货品已打包
			add("10019");// 交接发货
			add("10020");// 包裹出库
			add("10032");// 分拣验收
			add("10033");// 站点验收
			add("10034");// 妥投
		}
	};

	@Override
	public void createPurchaseInFromJD() throws BusinessException {
		// TODO 自动生成的方法存根

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
				EclpPoQueryPoOrderResponse rs = helper.Query(nos);// 查询京东的单号
				if (!CommonHelper.StringEqual(rs.getCode(), "0")) {
					throw new Exception("京东查询采购入库单信息发生异常：" + rs.getMsg());
				}
				List<QueryPoModel> results = rs.getQueryPoModelList();
				HashMap<String, QueryPoModel> resultmap = new HashMap<String, QueryPoModel>();
				for (int i = 0; i < results.size(); i++) {
					QueryPoModel result = results.get(i);
					if (CommonHelper.StringEqual(result.getPoOrderStatus(),
							"70")) {
						String no = result.getPoOrderNo();// 订单编码 ==vdef13
						resultmap.put(no, result);
						if (nos.contains(no)) {
							ids.add(noidmap.get(no));// 单据号
						}
						for (int j = 0; j < result.getPoItemModelList().size(); j++) {
							PoItemModel model = result.getPoItemModelList()
									.get(j);
							String goodno = model.getGoodsNo();
							String key = no + "_" + goodno;// vdef13+货物id
							if (!modelmap.containsKey(key)) {
								modelmap.put(key, model);
							}
						}
					}
				}
				// ids.add(noidmap.get("EPL4418048215038"));// 单据号
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
							po.setBillmaker(arriveHVO.getBillmaker());// 制单人
							po.setCbizid(arriveHVO.getPk_pupsndoc());// 采购员
							po.setCbiztype(arriveHVO.getPk_busitype());// 业务流程
							// 结算成本域
							po.setCdptid(arriveHVO.getPk_dept());// 采购部门最新版本
							po.setCdptvid(arriveHVO.getPk_dept_v());// 采购部门

							po.setCfanaceorgoid(arriveHVO.getPk_org());// 结算财务组织最新版本
							po.setCfanaceorgvid(arriveHVO.getPk_org_v());// 结算财务组织

							po.setCpayfinorgoid(arriveHVO.getPk_org());// 应付财务组织最新版本
							po.setCpayfinorgvid(arriveHVO.getPk_org_v());// 应付财务组织

							po.setCorpoid(arriveHVO.getPk_org());// 公司最新版本
							po.setCorpvid(arriveHVO.getPk_org_v());// 公司

							po.setCpurorgoid(arriveHVO.getPk_org());// 采购组织
							po.setCpurorgvid(arriveHVO.getPk_org_v());// 采购组织最新版本
							po.setCpayfinorgoid(arriveHVO.getPk_org());// 应付财务组织最新版本
							po.setCpayfinorgvid(arriveHVO.getPk_org_v());// 应付财务组织
							// 创建时间 单据时间 制单日期
							// po.setNtotalnum(arriveHVO.getNtotalastnum());//总数量

							po.setCreator(arriveHVO.getBillmaker());// 创建人
							po.setCtrantypeid(arriveHVO.getCtrantypeid());// 订单类型
							po.setCvendorid(arriveHVO.getPk_supplier());// 供应商
							po.setVbillcode(arriveHVO.getVbillcode());// 单据单号
							po.setVdef13(arriveHVO.getVdef13());

							ArriveItemVO[] bvo = arr[i].getBVO();
							for (ArriveItemVO bodys : bvo) {
								po.setCrececountryid(bodys.getCrececountryid());// 国家
								po.setCsendcountryid(bodys.getCrececountryid());
								po.setCtaxcountryid(bodys.getCrececountryid());
								po.setCwarehouseid(bodys.getPk_receivestore());// 仓库
								String itemid = bodys.getPk_material();// 物料编码
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
										po.setNtotalnum(qty);// 总数量

										PurchaseInBodyVO body = new PurchaseInBodyVO();
										body.setCastunitid(bodys
												.getCastunitid());// 单位
										body.setCbodywarehouseid(bodys
												.getPk_receivestore()); // 仓库
										body.setCcurrencyid(bodys
												.getCcurrencyid());// 本币币种
										body.setCfanaceorgoid(bodys.getPk_org());// 组织
										body.setCfirstbillbid(bodys
												.getCsourcebid());// 源头单据表体主键
										body.setCfirstbillhid(bodys
												.getCsourceid());// 源头单据表头主键
										body.setCfirsttranstype(bodys
												.getVfirsttrantype());// 源头单据交易类型
										body.setCfirsttype(bodys
												.getCsourcetypecode());// 源头单据类型
										body.setCmaterialoid(bodys
												.getPk_material());// 物料
										body.setCmaterialvid(bodys
												.getPk_material());// 物料编码
										body.setCorigcurrencyid(bodys
												.getCcurrencyid());// 币种
										body.setCreqstoorgoid(bodys.getPk_org());// 需求库存组织最新版本
										body.setCreqstoorgvid(bodys
												.getPk_org_v());// 需求库存组织
										body.setCqtunitid(bodys.getCunitid());// 报价单位
										body.setCorpoid(bodys.getPk_org());// 公司最新版本
										body.setCorpvid(bodys.getPk_org_v());// 公司
										body.setCrowno(bodys.getCrowno());// 行号
										body.setCsourcebillbid(bodys
												.getPk_arriveorder_b());// 来源单据表体行主键
										body.setCsourcebillhid(bodys
												.getPk_arriveorder());// 来源单据表头主键
										body.setCsourcetranstype(arriveHVO
												.getCtrantypeid()); // 来源单据交易类型
										body.setCsourcetype("23");// 来源单据类型
										// 来源单据类型编码
										body.setCsrcmaterialoid(bodys
												.getPk_material());// 来源物料
										body.setCsrcmaterialvid(bodys
												.getPk_material());// 来源物料编码
										// 税码
										body.setCunitid(bodys.getCunitid()); // 主单位
										body.setCvendorid(arriveHVO
												.getPk_supplier());// 供应商
										body.setDbizdate(now.getDate());// 入库日期

										// 所有涉及金额数据
										body.setNassistnum(qty);// 实收数量
										// ncalcostmny ncaltaxmny
										// nchangestdrate nitemdiscountrate
										body.setNcalcostmny(bodys.getNmny()); // 计成本金额
										body.setNcaltaxmny(bodys.getNmny());// 计税金额
										body.setNchangestdrate(bodys
												.getNexchangerate());// 折本汇率
										// body.setNitemdiscountrate();
										// 折扣

										body.setNmny(bodys.getNmny());// 本币无税金额
										body.setNnum(qty);// 实收主数量
										body.setNnetprice(bodys.getNprice());// 主本币无税净价
										body.setNorigmny(bodys.getNmny());// 无税金额
										body.setNorigprice(bodys
												.getNorigprice());// 主无税单价
										body.setNorignetprice(bodys
												.getNorigprice());// 主无税净价
										body.setNorigtaxmny(bodys
												.getNorigtaxmny());// 价税合计
										body.setNorigtaxnetprice(bodys
												.getNorigtaxprice());// 主含税净价
										body.setNorigtaxprice(bodys
												.getNorigtaxprice());// 主含税单价

										body.setNprice(bodys.getNprice());// 主本币无税单价
										body.setNqtnetprice(bodys.getNprice());// 本币无税净价
										body.setNqtorignetprice(bodys
												.getNprice());// 无税净价
										body.setNqtorigprice(bodys.getNprice());// 无税单价

										body.setNqtorigtaxnetprice(bodys
												.getNorigtaxprice());// 含税净价
										body.setNqtorigtaxprice(bodys
												.getNorigtaxprice());// 含税单价

										body.setNqtprice(bodys.getNprice());// 本币无税单价
										body.setNqttaxnetprice(bodys
												.getNorigtaxprice());// 本币含税净价
										body.setNqttaxprice(bodys
												.getNorigtaxprice());// 本币含税单价

										body.setNqtunitnum(bodys.getNnum());// 报价数量
										body.setNshouldassistnum(bodys
												.getNnum());// 应收数量
										body.setNshouldnum(bodys.getNnum());// 应收主数量
										// 税额
										body.setNtaxmny(bodys.getNorigtaxmny());// 本币价税合计
										body.setNtaxnetprice(bodys
												.getNorigtaxprice());// 主本币含税净价
										body.setNtaxprice(bodys
												.getNorigtaxprice());// 主本币含税单价
										body.setNtaxrate(bodys.getNtaxrate());// 税率

										body.setPk_creqwareid(bodys
												.getPk_receivestore());// 需求仓库
										body.setPk_group(bodys.getPk_group());
										body.setPk_org(bodys.getPk_org());
										body.setPk_org_v(bodys.getPk_org_v());
										body.setVfirstbillcode(bodys
												.getVfirstcode());// 源头单据号
										body.setVfirstrowno(bodys
												.getVfirstrowno());// 源头单据行号
										// 来源单据号和行号
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
											.getHVO().getCreationtime(), now) > maxdays) {// 防止错误数据一直读取
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
									Logger.error("生成jd入库数据扭转状态F时发生错误:"
											+ e1.getMessage());
									Logger.error(e1);
								}
								Logger.error("生成jd入库数据发生错误:" + ex.getMessage());
								Logger.error(ex);
							}
							
						}
					}
				}
			}
			

		} catch (Exception ex) {
			Logger.error("读取jd入库数据发生错误:" + ex.getMessage());
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
		// TODO 自动生成的方法存根

	}

	@Override
	public void readSaleOutFromJD() throws BusinessException {
		// TODO 自动生成的方法存根

	}

	@Override
	public void readDeliveryFromJD() throws BusinessException {
		// TODO 自动生成的方法存根

	}

	private PurchaseInBodyVO[] createBody(ArriveItemVO[] bvo) {
		return null;

	}
}
