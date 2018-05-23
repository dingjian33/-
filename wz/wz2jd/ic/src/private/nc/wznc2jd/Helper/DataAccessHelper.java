package nc.wznc2jd.Helper;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import nc.impl.pubapp.pattern.database.DBTool;
import nc.jdbc.framework.crossdb.CrossDBConnection;
import nc.vo.pubapp.pattern.exception.ExceptionUtils;
import nc.vo.pubapp.pattern.exception.TransferSqlException;
import nc.vo.pubapp.pattern.log.Log;

public class DataAccessHelper extends nc.impl.pubapp.pattern.database.DataAccessUtils {
	public int[] update(List<String> sqls) {
		DBTool tool = new DBTool();
		CrossDBConnection connection = null;
		Statement stmt = null;
		int[] result = null;
		try {
			connection = (CrossDBConnection) tool.getConnection();

			connection.setAddTimeStamp(true);
			stmt = connection.createStatement();

			connection.setAutoCommit(false);
			for (String sql : sqls) {
				stmt.addBatch(sql);
			}
			result = stmt.executeBatch();
			connection.commit();
			connection.setAutoCommit(true);
		} catch (SQLException ex) {
			try {
				connection.rollback();
			} catch (SQLException ex1) {
				ExceptionUtils.wrappException(ex1);
			}
			//	      TransferSqlException e = new TransferSqlException(ex, sql);
			ExceptionUtils.wrappException(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ex) {
					Log.error(ex);
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {
					Log.error(ex);
				}
			}
		}
		return result;
	}

	public int[] update1(String sql) {
		DBTool tool = new DBTool();
		CrossDBConnection connection = null;
		Statement stmt = null;
		int[] result = null;
		try {
			connection = (CrossDBConnection) tool.getConnection();

			connection.setAddTimeStamp(true);
			stmt = connection.createStatement();

			connection.setAutoCommit(false);
			stmt.addBatch(sql);

			result = stmt.executeBatch();
			connection.commit();
			connection.setAutoCommit(true);
		} catch (SQLException ex) {
			try {
				connection.rollback();
			} catch (SQLException ex1) {
				ExceptionUtils.wrappException(ex1);
			}
			//	      TransferSqlException e = new TransferSqlException(ex, sql);
			ExceptionUtils.wrappException(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ex) {
					Log.error(ex);
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {
					Log.error(ex);
				}
			}
		}
		return result;
	}
}
