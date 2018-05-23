package nc.wznc2jd.bgwork;


import nc.bs.framework.common.NCLocator;
import nc.bs.pub.pa.PreAlertObject;
import nc.bs.pub.taskcenter.BgWorkingContext;
import nc.bs.pub.taskcenter.IBackgroundWorkPlugin;
import nc.vo.pub.BusinessException;
import nc.wznc2jd.itf.ReadFromJDSyncServiceItf;

//²É¹º¶©µ¥
public class PurchaseInSyncPlugin implements IBackgroundWorkPlugin {
	@Override
	public PreAlertObject executeTask(BgWorkingContext arg0)
			throws BusinessException {
		NCLocator.getInstance().lookup(ReadFromJDSyncServiceItf.class)
				.createPurchaseInFromJD();
		return null;
	}
}
