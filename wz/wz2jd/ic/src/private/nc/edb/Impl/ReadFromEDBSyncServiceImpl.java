package nc.edb.Impl;

import nc.vo.pu.m23.entity.ArriveHeaderVO;
import nc.vo.pu.m23.entity.ArriveItemVO;
import nc.vo.pu.m23.entity.ArriveVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.data.IRowSet;
import nc.vo.ic.m45.entity.PurchaseInBodyVO;
import nc.vo.ic.m45.entity.PurchaseInHeadVO;
import nc.vo.ic.m45.entity.PurchaseInVO;
import nc.wznc2jd.JDHelper.CommonHelper;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.edb.EDBHelper.EdbGetInfo;
import nc.edb.Helper.ParseXml;
import nc.edb.Helper.ResponseGetXml;
import nc.edb.itf.MapList;
import nc.edb.itf.ReadFromEDBSyncServiceItf;
import nc.impl.pubapp.pattern.data.bill.BillQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReadFromEDBSyncServiceImpl implements ReadFromEDBSyncServiceItf {
	private static final long serialVersionUID = 1586262846504659456L;
	int maxdays = 30;

	@Override
	public void createPurchaseInFromEDB() throws BusinessException {
		// TODO 自动生成的方法存根

		try {
			// BillInsert<PurchaseInVO> insert = new BillInsert<PurchaseInVO>();
			BillQuery<ArriveVO> arrive = new BillQuery<ArriveVO>(ArriveVO.class);
			StringBuilder builder = new StringBuilder();
			builder.append(" select A.pk_arriveorder from po_arriveorder A ");
			builder.append(" left join po_arriveorder_b b on A.pk_arriveorder = b.pk_arriveorder ");
			builder.append(" where A.Vdef19='Y' and A.Fbillstatus ='3' and A.bisback ='N' and A.Dr=0 ");
			ArrayList<String> nos = new ArrayList<String>();

			IRowSet set = CommonHelper.sqlUtil.query(builder.toString());
			while (set.next()) {
				String no = set.getString(0);// 到货单号
				if (!nos.contains(no)) {
					nos.add(no);
				}
			}
			if (nos != null && nos.size() > 0) {
				ArriveVO[] arr = arrive.query(nos.toArray((new String[nos
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

						// 返回的xml解析出入库量和条形码
						String res = EdbGetInfo.edbInStoreGet(arriveHVO
								.getVbillcode());
						ArrayList<ResponseGetXml> rg = ParseXml
								.parseGetXMl(res);

						ArriveItemVO[] bvo = arr[i].getBVO();
						for (ArriveItemVO bodys : bvo) {
							po.setCrececountryid(bodys.getCrececountryid());// 国家
							po.setCsendcountryid(bodys.getCrececountryid());
							po.setCtaxcountryid(bodys.getCrececountryid());
							po.setCwarehouseid(bodys.getPk_receivestore());// 仓库
							String itemid = bodys.getPk_material();// 物料编码
							String materialbarcode = CommonHelper
									.GetMaterialbarcode(itemid);
							UFDouble qty = null;
							for (int j = 0; j < rg.size(); j++) {
								if (materialbarcode.equals(rg.get(j)
										.getBar_code())) {
									qty = CommonHelper.ToUFDoubleFromStr(rg
											.get(j).Pro_number);
								}
							}

							po.setNtotalnum(qty);// 入库量

							PurchaseInBodyVO body = new PurchaseInBodyVO();
							body.setCastunitid(bodys.getCastunitid());// 单位
							body.setCbodywarehouseid(bodys.getPk_receivestore()); // 仓库
							body.setCcurrencyid(bodys.getCcurrencyid());// 本币币种
							body.setCfanaceorgoid(bodys.getPk_org());// 组织
							body.setCfirstbillbid(bodys.getCsourcebid());// 源头单据表体主键
							body.setCfirstbillhid(bodys.getCsourceid());// 源头单据表头主键
							body.setCfirsttranstype(bodys.getVfirsttrantype());// 源头单据交易类型
							body.setCfirsttype(bodys.getCsourcetypecode());// 源头单据类型
							body.setCmaterialoid(bodys.getPk_material());// 物料
							body.setCmaterialvid(bodys.getPk_material());// 物料编码
							body.setCorigcurrencyid(bodys.getCcurrencyid());// 币种
							body.setCreqstoorgoid(bodys.getPk_org());// 需求库存组织最新版本
							body.setCreqstoorgvid(bodys.getPk_org_v());// 需求库存组织
							body.setCqtunitid(bodys.getCunitid());// 报价单位
							body.setCorpoid(bodys.getPk_org());// 公司最新版本
							body.setCorpvid(bodys.getPk_org_v());// 公司
							body.setCrowno(bodys.getCrowno());// 行号
							body.setCsourcebillbid(bodys.getPk_arriveorder_b());// 来源单据表体行主键
							body.setCsourcebillhid(bodys.getPk_arriveorder());// 来源单据表头主键
							body.setCsourcetranstype(arriveHVO.getCtrantypeid()); // 来源单据交易类型
							body.setCsourcetype("23");// 来源单据类型
							// 来源单据类型编码
							body.setCsrcmaterialoid(bodys.getPk_material());// 来源物料
							body.setCsrcmaterialvid(bodys.getPk_material());// 来源物料编码
							// 税码
							body.setCunitid(bodys.getCunitid()); // 主单位
							body.setCvendorid(arriveHVO.getPk_supplier());// 供应商
							body.setDbizdate(now.getDate());// 入库日期

							// 所有涉及金额数据
							body.setNassistnum(qty);// 实收数量
							body.setNcalcostmny(bodys.getNmny()); // 计成本金额
							body.setNcaltaxmny(bodys.getNmny());// 计税金额
							body.setNchangestdrate(bodys.getNexchangerate());// 折本汇率
							// body.setNitemdiscountrate();
							// 折扣

							body.setNmny(bodys.getNmny());// 本币无税金额
							body.setNnum(qty);// 实收主数量
							body.setNnetprice(bodys.getNprice());// 主本币无税净价
							body.setNorigmny(bodys.getNmny());// 无税金额
							body.setNorigprice(bodys.getNorigprice());// 主无税单价
							body.setNorignetprice(bodys.getNorigprice());// 主无税净价
							body.setNorigtaxmny(bodys.getNorigtaxmny());// 价税合计
							body.setNorigtaxnetprice(bodys.getNorigtaxprice());// 主含税净价
							body.setNorigtaxprice(bodys.getNorigtaxprice());// 主含税单价

							body.setNprice(bodys.getNprice());// 主本币无税单价
							body.setNqtnetprice(bodys.getNprice());// 本币无税净价
							body.setNqtorignetprice(bodys.getNprice());// 无税净价
							body.setNqtorigprice(bodys.getNprice());// 无税单价

							body.setNqtorigtaxnetprice(bodys.getNorigtaxprice());// 含税净价
							body.setNqtorigtaxprice(bodys.getNorigtaxprice());// 含税单价

							body.setNqtprice(bodys.getNprice());// 本币无税单价
							body.setNqttaxnetprice(bodys.getNorigtaxprice());// 本币含税净价
							body.setNqttaxprice(bodys.getNorigtaxprice());// 本币含税单价

							body.setNqtunitnum(bodys.getNnum());// 报价数量
							body.setNshouldassistnum(bodys.getNnum());// 应收数量
							body.setNshouldnum(bodys.getNnum());// 应收主数量
							// 税额
							body.setNtaxmny(bodys.getNorigtaxmny());// 本币价税合计
							body.setNtaxnetprice(bodys.getNorigtaxprice());// 主本币含税净价
							body.setNtaxprice(bodys.getNorigtaxprice());// 主本币含税单价
							body.setNtaxrate(bodys.getNtaxrate());// 税率

							body.setPk_creqwareid(bodys.getPk_receivestore());// 需求仓库
							body.setPk_group(bodys.getPk_group());
							body.setPk_org(bodys.getPk_org());
							body.setPk_org_v(bodys.getPk_org_v());
							body.setVfirstbillcode(bodys.getVfirstcode());// 源头单据号
							body.setVfirstrowno(bodys.getVfirstrowno());// 源头单据行号
							// 来源单据号和行号
							body.setStatus(VOStatus.NEW);
							bodyvo.add(body);

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
								if (UFDateTime.getDaysBetween(arr[i].getHVO()
										.getCreationtime(), now) > maxdays) {// 防止错误数据一直读取
									String sql = "update po_arriveorder Set Vdef19='"
											+ arriveHVO.getVdef19()
											+ "' where pk_arriveorder='"
											+ arriveHVO.getPk_arriveorder()
											+ "'";
									CommonHelper.getBaseDao()
											.executeUpdate(sql);
								}
							} catch (Exception e1) {
								Logger.error("生成edb入库数据扭转状态F时发生错误:"
										+ e1.getMessage());
								Logger.error(e1);
							}
							Logger.error("生成edb入库数据发生错误:" + ex.getMessage());
							Logger.error(ex);
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

}
