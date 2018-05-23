package nc.wznc2jd.itf;

import nc.vo.pub.BusinessException;

public interface ReadFromJDSyncServiceItf {
	public void readPurchaseInFromJD() throws BusinessException;
	public void createPurchaseInFromJD() throws BusinessException;
	public void readSaleOutFromJD() throws BusinessException;
	public void readDeliveryFromJD() throws BusinessException;
}
