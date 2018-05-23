package nc.wznc2jd.Impl;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.vo.pu.m23.entity.ArriveVO;
import nc.impl.pubapp.pattern.data.bill.BillQuery;
import nc.impl.pubapp.pattern.data.bill.BillUpdate;
import nc.itf.uap.pf.IPfExchangeService;
import nc.itf.uap.pf.busiflow.PfButtonClickContext;
import nc.ui.ml.NCLangRes;
import nc.vo.ic.m45.entity.PurchaseInVO;
import nc.vo.ic.m4c.entity.SaleOutBodyVO;
import nc.vo.ic.m4c.entity.SaleOutVO;
import nc.vo.ic.pub.define.ICPubMetaNameConst;
import nc.vo.ic.pub.util.ValueCheckUtil;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.exception.ExceptionUtils;
import nc.vo.pubapp.util.VORowNoUtils;
import nc.vo.so.m30.entity.SaleOrderVO;
import nc.vo.so.m4331.entity.DeliveryVO;
import nc.vo.uap.pf.PFRuntimeException;
import nc.wznc2jd.JDHelper.CommonHelper;
import nc.wznc2jd.itf.UpdateAndSignBillItf;

public class UpdateAndSignBillImpl implements UpdateAndSignBillItf {

	public void UpdateAndSignPurchaseIn(PurchaseInVO newvo, PurchaseInVO oldvo) throws BusinessException {
		if (newvo == null || oldvo == null)
			return;
		BillQuery<PurchaseInVO> query = new BillQuery<PurchaseInVO>(PurchaseInVO.class);
		BillUpdate<PurchaseInVO> update = new BillUpdate<PurchaseInVO>();

		update.update(new PurchaseInVO[] { newvo }, new PurchaseInVO[] { oldvo });
		PurchaseInVO[] vos = query.query(new String[] { newvo.getHead().getCgeneralhid() });//更新后刷新
		NCLocator.getInstance().lookup(nc.pubitf.ic.m45.api.IPurchaseInMaintainAPI.class).signBills(vos);
		NCLocator.getInstance().lookup(nc.pubitf.ic.m45.api.IPurchaseInMaintainAPI.class).insertBills(vos);
	}

	@Override
	public void UpdateAndSignPurchaseIn_RequiresNew(PurchaseInVO newvo,
			PurchaseInVO oldvo) throws BusinessException {
		UpdateAndSignPurchaseIn(newvo, oldvo);
	}

	public SaleOutVO[] InsertReturnOrder_RequiresNew(SaleOutVO[] newvos)
			throws BusinessException {
		return this.InsertReturnOrder(newvos);
	}

	public SaleOutVO[] DeliveryToSaleOut(DeliveryVO[] deliveryvos)
			throws BusinessException {
		if (deliveryvos == null)
			return null;
		ArrayList<SaleOutVO> results = new ArrayList<SaleOutVO>();
		AggregatedValueObject[] avo = changeVos(deliveryvos, "4331", "4C");
		if (avo != null) {
			for (int i = 0; i < avo.length; i++) {
				results.add((SaleOutVO) avo[i]);
			}
		}
		return results.toArray(new SaleOutVO[results.size()]);
	}

	public SaleOutVO[] DeliveryToSaleOut_RequiresNew(DeliveryVO[] deliveryvos)
			throws BusinessException {
		return DeliveryToSaleOut(deliveryvos);
	}

	public SaleOrderVO[] SaleOutToSaleOrder(SaleOutVO[] vos)
			throws BusinessException {
		if (vos == null)
			return null;
		ArrayList<SaleOrderVO> results = new ArrayList<SaleOrderVO>();
		AggregatedValueObject[] avo = changeVos(vos, "4C", "30");
		if (avo != null) {
			for (int i = 0; i < avo.length; i++) {
				results.add((SaleOrderVO) avo[i]);
			}
		}
		return results.toArray(new SaleOrderVO[results.size()]);
	}

	public SaleOutVO[] SaleOrderToSaleOut(SaleOrderVO[] vos)
			throws BusinessException {
		if (vos == null)
			return null;
		ArrayList<SaleOutVO> results = new ArrayList<SaleOutVO>();
		AggregatedValueObject[] avo = changeVos(vos, "30", "4C");
		if (avo != null) {
			for (int i = 0; i < avo.length; i++) {
				results.add((SaleOutVO) avo[i]);
			}
		}
		return results.toArray(new SaleOutVO[results.size()]);
	}

	private static AggregatedValueObject[] changeVos(
			AggregatedValueObject[] vos, String srctype, String totype) {
		AggregatedValueObject[] tmpRetVos = null;

		try {
			int classifyMode = PfButtonClickContext.NoClassify;
			tmpRetVos = changeVos(vos, srctype, totype, classifyMode);// getExchangeService().runChangeDataAryNeedClassify("",
																		// "",
																		// vos,
																		// null,
																		// classifyMode);

		} catch (PFRuntimeException ex) {
			throw ex;
		}
		return processRowNO(tmpRetVos);
	}

	private static IPfExchangeService getExchangeService() {

		// nc.impl.uap.pf.PfExchangeServiceImpl i=null;
		IPfExchangeService exchangeService = null;
		if (exchangeService == null)
			exchangeService = NCLocator.getInstance().lookup(
					IPfExchangeService.class);
		return exchangeService;
	}

	private static AggregatedValueObject[] changeVos(
			AggregatedValueObject[] vos, String srctype, String totype,
			int classifyMode) {
		AggregatedValueObject[] tmpRetVos = null;

		try {
			// tmpRetVos =
			// getExchangeService().runChangeDataAryNeedClassify("4331", "4C",
			// vos, null, classifyMode);
			tmpRetVos = getExchangeService().runChangeDataAryNeedClassify(
					srctype, totype, vos, null, classifyMode);
		} catch (BusinessException ex) {
			Logger.error(ex.getMessage(), ex);
			throw new PFRuntimeException(NCLangRes.getInstance().getStrByID(
					"pfworkflow1", "PfUtilClient-000004", null,
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
	protected static AggregatedValueObject[] processRowNO(
			AggregatedValueObject[] retvos) {
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


	@Override
	public SaleOutVO[] InsertSaleOut(SaleOutVO[] newvos)
			throws BusinessException {
		// TODO 自动生成的方法存根
		return null;
	}

	@Override
	public SaleOutVO[] InsertSaleOut_RequiresNew(SaleOutVO[] newvos)
			throws BusinessException {
		// TODO 自动生成的方法存根
		return null;
	}

	@Override
	public SaleOutVO[] InsertReturnOrder(SaleOutVO[] newvos)
			throws BusinessException {
		// TODO 自动生成的方法存根
		return null;
	}

	@Override
	public void UpdateAndSignSaleOut(SaleOutVO newvo, SaleOutVO oldvo)
			throws BusinessException {
		// TODO 自动生成的方法存根
		
	}
}
