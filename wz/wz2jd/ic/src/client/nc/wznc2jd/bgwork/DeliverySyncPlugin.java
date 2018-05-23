package nc.wznc2jd.bgwork;

import java.util.Collection;
import java.util.HashMap;

import nc.bs.Common.date.DateUtils;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.bs.pub.pa.PreAlertObject;
import nc.bs.pub.taskcenter.BgWorkingContext;
import nc.bs.pub.taskcenter.IBackgroundWorkPlugin;
import nc.vo.bd.material.MaterialVO;
import nc.vo.pub.BusinessException;
import nc.wznc2jd.itf.ReadFromJDSyncServiceItf;


//·¢»õµ¥
public class DeliverySyncPlugin implements  IBackgroundWorkPlugin{
	@Override
	public PreAlertObject executeTask(BgWorkingContext arg0)
			throws BusinessException {
		NCLocator.getInstance().lookup(ReadFromJDSyncServiceItf.class).readDeliveryFromJD();
		return null;
	}
}
