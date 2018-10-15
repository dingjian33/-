package nc.edb.bgwork;


import nc.bs.framework.common.NCLocator;
import nc.bs.pub.pa.PreAlertObject;
import nc.bs.pub.taskcenter.BgWorkingContext;
import nc.bs.pub.taskcenter.IBackgroundWorkPlugin;
import nc.edb.itf.ReadFromEDBSyncServiceItf;
import nc.vo.pub.BusinessException;

//²É¹º¶©µ¥
public class PurchaseInSyncPlugin implements IBackgroundWorkPlugin {
	@Override
	public PreAlertObject executeTask(BgWorkingContext arg0)
			throws BusinessException {
		NCLocator.getInstance().lookup(ReadFromEDBSyncServiceItf.class)
				.createPurchaseInFromEDB();
		return null;
	}
}

