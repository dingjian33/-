package nc.wzscan.Impl;

import java.util.ArrayList;
import java.util.HashMap;

import uap.iweb.log.Logger;

import com.jd.open.api.sdk.domain.ECLP.EclpOpenService.OrderDetail;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.impl.pubapp.pattern.data.bill.BillQuery;
import nc.itf.uap.rbac.IUserManageQuery;
import nc.vo.ic.m4c.entity.SaleOutBodyVO;
import nc.vo.ic.m4c.entity.SaleOutVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.data.IRowSet;
import nc.vo.sm.UserVO;
import nc.vo.so.m4331.entity.DeliveryVO;
import nc.wznc2jd.JDHelper.CommonHelper;
import nc.wzscan.itf.DeliveryScanMainItf;

public class DeliveryScanMainImpl implements DeliveryScanMainItf {

	public String CreateSaleOutByDelivery(String usercode,String constr,String docno) throws Exception {

		UserVO user;
		constr=constr.trim();
		usercode=usercode.trim();
		try {
			IUserManageQuery service = (IUserManageQuery) NCLocator.getInstance().lookup(IUserManageQuery.class.getName());
			user = service.findUserByCode(usercode, constr);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
		if(user==null){
			throw new Exception("数据源"+constr+"中找不到用户"+usercode);
		}
		UFDateTime now = new UFDateTime();
		InvocationInfoProxy.getInstance().setBizDateTime(now.getMillis());
		InvocationInfoProxy.getInstance().setGroupId(user.getPk_group());
		InvocationInfoProxy.getInstance().setUserCode(user.getUser_code());
		InvocationInfoProxy.getInstance().setUserDataSource(constr);
		
		
		BillQuery<DeliveryVO> query = new BillQuery<DeliveryVO>(DeliveryVO.class);
		StringBuilder builder = new StringBuilder();
		builder.append(" select A.cdeliveryid from so_delivery A ");
		builder.append(" where A.Dr=0 and A.fstatusflag='2' and A.vbillcode ='");
		builder.append(docno);
		builder.append("'");
		ArrayList<String> ids = new ArrayList<String>();
		IRowSet set = CommonHelper.sqlUtil.query(builder.toString());
		while (set.next()) {
			String id = set.getString(0);// id
			if (!ids.contains(id)) {
				ids.add(id);
			}
		}
		if (ids.size() > 0) {
			DeliveryVO[] vos = query.query(ids.toArray((new String[ids.size()])));
			for(int i=0;i<vos.length;i++){
				DeliveryVO vo=vos[i];
				try {
					DeliveryVO[] deliveryvos = { vo };
					nc.vo.ic.m4c.entity.SaleOutVO[] ovos = NCLocator.getInstance()
							.lookup(nc.wznc2jd.itf.UpdateAndSignBillItf.class)
							.DeliveryToSaleOut(deliveryvos);
					if (ovos.length > 0) {
						nc.vo.ic.m4c.entity.SaleOutVO ovo = ovos[0];
						ovo.getHead().setBillmaker(user.getCuserid());
						if(CommonHelper.IsNullOrEmpty(ovo.getHead().getCtrantypeid())){
							ovo.getHead().setCtrantypeid("0001A21000000000237S");//出入库类型
						}
						for (int j = 0; j < ovo.getBodys().length; j++) {
							SaleOutBodyVO body = ovo.getBodys()[j];
							UFDouble dqty = body.getNshouldnum();
							body.setNnum(dqty);
							body.setNassistnum(dqty);
							body.setDbizdate(now.getDate());
						}
						ArrayList<SaleOutVO> csovos = new ArrayList<SaleOutVO>();
						csovos.add(ovo);
						SaleOutVO[] saleoutvos = csovos.toArray(new SaleOutVO[csovos.size()]);
						NCLocator.getInstance().lookup(nc.wznc2jd.itf.UpdateAndSignBillItf.class)
								.InsertSaleOut(saleoutvos);
					}
				} catch (Exception ex) {
					throw ex;
				}
			}
		} else {
			throw new Exception("系统中找不到单号为'" + docno + "'的已审核发货单");
		}

		return "成功" + docno;
	}
}
