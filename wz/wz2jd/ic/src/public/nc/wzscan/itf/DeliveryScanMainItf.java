package nc.wzscan.itf;

import nc.vo.pub.BusinessException;

public interface DeliveryScanMainItf {
	public String CreateSaleOutByDelivery(String usercode,String constr,String docno) throws Exception;
}
