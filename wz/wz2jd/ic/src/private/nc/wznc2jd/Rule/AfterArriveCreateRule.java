package nc.wznc2jd.Rule;

import nc.bs.businessevent.BdUpdateEvent;
import nc.bs.businessevent.EventDispatcher;
import nc.bs.businessevent.IEventType;
import nc.impl.pubapp.pattern.rule.IRule;
import nc.vo.ecpubapp.pattern.exception.ExceptionUtils;
import nc.vo.pu.m23.entity.ArriveVO;
import nc.vo.scmf.pub.util.BusinessEventType;

public class AfterArriveCreateRule implements IRule<ArriveVO> {

	@Override
	public void process(ArriveVO[] vos) {
		// TODO 自动生成的方法存根
		try {
			String sourceid = BusinessEventType.getSourceIDByBillVO(vos);
			EventDispatcher.fireEvent(new BdUpdateEvent(sourceid,
					IEventType.TYPE_INSERT_AFTER, vos, null));
		} catch (Exception e) {
			ExceptionUtils.wrappException(e);
		}
	}

}
