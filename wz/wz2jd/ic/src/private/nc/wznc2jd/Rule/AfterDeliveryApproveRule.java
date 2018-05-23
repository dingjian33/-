package nc.wznc2jd.Rule;

import nc.impl.pubapp.pattern.rule.IRule;
import nc.vo.ecpubapp.pattern.exception.ExceptionUtils;
import nc.vo.scmf.pub.util.BusinessEventType;
import nc.vo.so.m4331.entity.DeliveryVO;
import nc.bs.businessevent.*;

public class AfterDeliveryApproveRule implements IRule<DeliveryVO> {

	  @Override
	  public void process(DeliveryVO[] vos) {
		  try {
		      String sourceid = BusinessEventType.getSourceIDByBillVO(vos);
		      EventDispatcher.fireEvent(new BdUpdateEvent(sourceid, IEventType.TYPE_APPROV_AFTER,vos,null));
		    }
		    catch (Exception e) {
		      ExceptionUtils.wrappException(e);
		    }
	  }
}
