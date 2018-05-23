package nc.wznc2jd.itf;

import nc.bs.framework.common.NCLocator;
import nc.vo.ic.m45.entity.PurchaseInVO;
import nc.vo.ic.m4c.entity.SaleOutVO;
import nc.vo.pu.m23.entity.ArriveVO;
import nc.vo.pub.BusinessException;
import nc.vo.so.m4331.entity.DeliveryVO;

public interface UpdateAndSignBillItf {
	public void UpdateAndSignPurchaseIn(PurchaseInVO newvo,PurchaseInVO oldvo) throws BusinessException;
	public void UpdateAndSignPurchaseIn_RequiresNew(PurchaseInVO newvo,PurchaseInVO oldvo) throws BusinessException;
	public void UpdateAndSignSaleOut(SaleOutVO newvo,SaleOutVO oldvo) throws BusinessException;
	public SaleOutVO[] InsertSaleOut(SaleOutVO[] newvos) throws BusinessException ;
	public SaleOutVO[] InsertSaleOut_RequiresNew(SaleOutVO[] newvos) throws BusinessException ;
	public SaleOutVO[] DeliveryToSaleOut(DeliveryVO[] deliveryvos) throws BusinessException ;
	public SaleOutVO[] DeliveryToSaleOut_RequiresNew(DeliveryVO[] deliveryvos) throws BusinessException ;
	public SaleOutVO[] InsertReturnOrder(SaleOutVO[] newvos) throws BusinessException;
	public SaleOutVO[] InsertReturnOrder_RequiresNew(SaleOutVO[] newvos) throws BusinessException;
	
}
