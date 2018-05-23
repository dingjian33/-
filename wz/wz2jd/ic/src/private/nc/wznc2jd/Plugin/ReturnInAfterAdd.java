package nc.wznc2jd.Plugin;

import nc.bs.ic.general.businessevent.ICGeneralCommonEvent;
import nc.bs.businessevent.IBusinessEvent;
import nc.bs.businessevent.IEventType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.pmpub.uap.util.ExceptionUtils;
import nc.vo.pub.BusinessException;
import nc.wznc2jd.JDHelper.*;

public class ReturnInAfterAdd implements nc.bs.businessevent.IBusinessListener {
	public void doAction(IBusinessEvent event) throws BusinessException {
		try {
			if (IEventType.TYPE_INSERT_AFTER.equals(event.getEventType())) {
				ICGeneralCommonEvent e = (ICGeneralCommonEvent) event;
				Object[] value = e.getOldObjs();
				if (null == value) {
					return;
				}
				//		        Integer i=1;
				//		        if(i.equals(1))
				//		        	throw new Exception("物料新增插件进入");

				try {

					ArrayList<nc.vo.ic.m4c.entity.SaleOutVO> datas = new ArrayList<nc.vo.ic.m4c.entity.SaleOutVO>();
					for (int x = 0; x < value.length; x++) {
						datas.add((nc.vo.ic.m4c.entity.SaleOutVO) value[x]);
					}
				} catch (Exception ex) {
					throw ex;
				}
			}
		} catch (Exception e) {
			throw new BusinessException(e);
		}
	}

}
