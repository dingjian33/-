package nc.wznc2jd.JDHelper;

import java.util.List;

import nc.bs.dao.BaseDAO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.data.IRowSet;

public class CommonHelper {

	public static nc.impl.pubapp.pattern.database.DataAccessUtils sqlUtil = new nc.impl.pubapp.pattern.database.DataAccessUtils();

	public static String GetInStr(String name, List<String> values) {
		return GetInStr(name, values.toArray(new String[values.size()]));
	}

	private static BaseDAO dao = null;

	public static BaseDAO getBaseDao() {
		if (dao == null) {
			dao = new BaseDAO();
		}
		return dao;
	}

	public static BaseDAO getNewBaseDao() {
		dao = new BaseDAO();
		return dao;
	}

	public static String GetInStr(String name, String[] values) {
		int size = values.length;
		int maxsize = 600;
		StringBuilder builder = new StringBuilder();
		if (size == 0) {
			builder.append(" and 1!=1");
		} else if (size <= maxsize) {
			builder.append(" and ");
			builder.append(name);
			builder.append(" in( ");
			for (int i = 0; i < size; i++) {
				if (i == size - 1) {
					builder.append("'");
					builder.append(values[i]);
					builder.append("')");
				} else {
					builder.append("'");
					builder.append(values[i]);
					builder.append("',");
				}
			}
		} else {
			int n = size / maxsize;
			builder.append(" and (");
			builder.append(name);
			builder.append(" in( ");
			for (int i = 0; i < maxsize; i++) {
				if (i == maxsize - 1) {
					builder.append("'");
					builder.append(values[i]);
					builder.append("')");
				} else {
					builder.append("'");
					builder.append(values[i]);
					builder.append("',");
				}
			}
			for (int i = 2; i <= n; i++) {
				int r = i * maxsize;
				int t = r - maxsize;
				builder.append(" or ");
				builder.append(name);
				builder.append(" in( ");
				for (int j = t; j < r; j++) {
					if (j == r - 1) {
						builder.append("'");
						builder.append(values[j]);
						builder.append("')");
					} else {
						builder.append("'");
						builder.append(values[j]);
						builder.append("',");
					}
				}
			}
			int t = n * maxsize;
			builder.append(" or ");
			builder.append(name);
			builder.append(" in( ");
			for (int i = t; i < size; i++) {
				if (i == size - 1) {
					builder.append("'");
					builder.append(values[i]);
					builder.append("'))");
				} else {
					builder.append("'");
					builder.append(values[i]);
					builder.append("',");
				}
			}
		}
		return builder.toString();
	}

	public static Boolean StringEqual(String value1, String value2) {
		if (IsNullOrEmpty(value1) && IsNullOrEmpty(value2)) {
			return true;
		} else if (IsNullOrEmpty(value1)) {
			return false;
		} else {
			return value1.equals(value2);
		}
	}

	public static String ToString(Object obj) {
		if (IsNullOrEmpty(obj)) {
			return "";
		} else {
			return obj.toString();
		}
	}

	public static Boolean IsNullOrEmpty(Object obj) {
		if (obj == null) {
			return true;
		}
		String value = obj.toString();
		return (value == null || value.length() == 0 || IsNullOrWhiteSpace(value));
	}

	public static Boolean IsNullOrEmpty(String value) {
		return (value == null || value.length() == 0 || IsNullOrWhiteSpace(value));
	}

	public static Boolean IsNullOrWhiteSpace(String value) {
		if (value == null)
			return true;

		for (int i = 0; i < value.length(); i++) {
			if (!Character.isSpaceChar(value.charAt(i)))
				return false;
		}

		return true;
	}

	public static String SubString(String value, int beginIndex, int endIndex) {
		if (IsNullOrEmpty(value)) {
			return "";
		} else {
			if (value.length() > endIndex) {
				return value.substring(beginIndex, endIndex);
			} else {
				return value;
			}
		}
	}

	public static Boolean ToBooleanFromChar(char ch) {
		Boolean value = (ch == 'Y' || ch == 'y');
		return value;
	}

	public static UFDouble ToUFDoubleFromStr(String s) {
		return new UFDouble(ToDoubleFromStr(s));
	}

	public static UFDouble ToUFDouble(int value) {
		return new UFDouble(value);
	}

	public static Double ToDoubleFromStr(String s) {
		try {
			return Double.valueOf(s);
		} catch (Exception e) {
			return Double.valueOf(0);
		}
	}

	public static Boolean GetBoolean(UFBoolean value) {
		try {
			return value.booleanValue();
		} catch (Exception e) {
			return false;
		}
	}

	public static Boolean ToBooleanFromString(String val) {
		Boolean value;
		if (val != null
				&& val.length() > 0
				&& (val.equalsIgnoreCase("true") || val.charAt(0) == 'Y' || val
						.charAt(0) == 'y'))
			value = true;
		else
			value = false;
		return value;
	}

	public static String GetWhCode(String id) {
		String sql = "select code  from bd_stordoc where pk_stordoc='" + id
				+ "'";
		String code = "";
		IRowSet rs = CommonHelper.sqlUtil.query(sql);
		while (rs.next()) {
			code = rs.getString(0);// itemID
		}
		return code;
	}

	public static String GetWarehouse(String id) {
		String defsql = "select def1  from bd_stordoc where pk_stordoc='" + id
				+ "'";
		String def1 = "";
		IRowSet def = CommonHelper.sqlUtil.query(defsql);
		while (def.next()) {
			def1 = def.getString(0);
		}
		String sql = "select name  from bd_defdoc where pk_defdoc='" + def1
				+ "'";
		String storeType = "";
		IRowSet rs = CommonHelper.sqlUtil.query(sql);
		while (rs.next()) {
			storeType = rs.getString(0);
		}
		return storeType;
	}

	public static String Getdef1(String org) {
		String sql = "select DEF1  from org_orgs where pk_org='" + org + "'";
		String def1 = "";
		IRowSet rs = CommonHelper.sqlUtil.query(sql);
		while (rs.next()) {
			def1 = rs.getString(0);// itemID
		}
		return def1;
	}

	public static String GetWarecode(String id) {
		String sql = "select DEF2  from bd_stordoc where pk_stordoc='" + id
				+ "'";
		String jdcode = "";
		IRowSet rs = CommonHelper.sqlUtil.query(sql);
		while (rs.next()) {
			jdcode = rs.getString(0);// itemID
		}
		return jdcode;
	}
	public static String GetMaterialbarcode (String id) {
		String sql = "select materialbarcode  from bd_material where pk_material='" + id
				+ "'";
		String materialbarcode  = "";
		IRowSet rs = CommonHelper.sqlUtil.query(sql);
		while (rs.next()) {
			materialbarcode  = rs.getString(0);// itemID
		}
		return materialbarcode ;
	}

	public static String GetMaterialcode (String id) {
		String sql = "select code  from bd_material where pk_material='" + id
				+ "'";
		String code  = "";
		IRowSet rs = CommonHelper.sqlUtil.query(sql);
		while (rs.next()) {
			code  = rs.getString(0);// itemID
		}
		return code ;
	}
}