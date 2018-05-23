package nc.wznc2jd.Helper;

import nc.vo.pubapp.pattern.data.IRowSet;
import nc.wznc2jd.JDHelper.CommonHelper;

public class DataSqlQuery {

	public String getPk_group(String id) {
		StringBuilder builder = new StringBuilder();
		builder.append(" select A.pk_group from po_arriveorder A ");
		builder.append(" where A.pk_arriveorder= '" + id + "'");
		IRowSet set = CommonHelper.sqlUtil.query(builder.toString());
		String pk_group = "";
		while (set.next()) {
			pk_group = set.getString(0);
		}
		return pk_group;

	}

	public String getPk_org(String id) {
		StringBuilder builder = new StringBuilder();
		builder.append(" select A.pk_org from po_arriveorder A ");
		builder.append(" where A.pk_arriveorder= '" + id + "'");
		IRowSet set = CommonHelper.sqlUtil.query(builder.toString());
		String pk_org = "";
		while (set.next()) {
			pk_org = set.getString(0);
		}
		return pk_org;

	}

	public String getPk_org_v(String id) {
		StringBuilder builder = new StringBuilder();
		builder.append(" select A.pk_org_v from po_arriveorder A ");
		builder.append(" where A.pk_arriveorder= '" + id + "'");
		IRowSet set = CommonHelper.sqlUtil.query(builder.toString());
		String pk_org_v = "";
		while (set.next()) {
			pk_org_v = set.getString(0);
		}
		return pk_org_v;

	}

}
