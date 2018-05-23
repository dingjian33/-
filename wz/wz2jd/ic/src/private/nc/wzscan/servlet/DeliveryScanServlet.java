package nc.wzscan.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.common.RuntimeEnv;
import nc.bs.framework.common.UserExit;
import nc.bs.framework.comn.NetStreamContext;
import nc.bs.framework.core.service.IFwLogin;
import nc.bs.logging.Logger;
import nc.impl.cmp.proxy.Proxy;
import nc.itf.uap.rbac.IUserManageQuery;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.sm.UserVO;
import nc.wznc2jd.JDHelper.CommonHelper;
import nc.wzscan.itf.DeliveryScanMainItf;
//import nc.bs.ca.capub.service.NCLocatorFactory;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DeliveryScanServlet implements IHttpServletAdaptor {
	private static final long serialVersionUID = 1L;
	private MapListProcessor myMapListprocessor;

	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserVO user;
		String usercode = request.getParameter("usercode");
		String constr = request.getParameter("constr");
		String docno = request.getParameter("docno");

		String returnJson = "";
		try {
			try {
				IUserManageQuery service = (IUserManageQuery) NCLocator.getInstance().lookup(
						IUserManageQuery.class.getName());
				user = service.findUserByCode(usercode, constr);
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
				throw new ServletException(e);
			}
			if (user == null) {
				throw new ServletException("数据源" + constr + "中找不到用户" + usercode);
			}

			UFDateTime now = new UFDateTime();
			InvocationInfoProxy.getInstance().setBizDateTime(now.getMillis());
			InvocationInfoProxy.getInstance().setGroupId(user.getPk_group());
			InvocationInfoProxy.getInstance().setUserCode(user.getUser_code());
			InvocationInfoProxy.getInstance().setUserDataSource(constr);
			DeliveryScanMainItf scanservice = (DeliveryScanMainItf) NCLocator.getInstance().lookup(
					DeliveryScanMainItf.class.getName());
			String error = "";
			try {
				scanservice.CreateSaleOutByDelivery(usercode, constr, docno);
			} catch (Exception e) {
				error = "单号进行出库发生错误:" + e.getMessage();
			}
			PrintWriter out = response.getWriter();
			if (!CommonHelper.IsNullOrEmpty(error)) {
				returnJson = error;
			}
			out.write(returnJson);
			//	nc.bs.framework.server.FindWebResourceServlet
			out.flush();
			out.close();
		} catch (Exception e) {
			Logger.debug(e);
//			throw new ServletException(e);
			returnJson=e.getMessage();
		}
		try{

			PrintWriter out = response.getWriter();
			out.write(returnJson);
			out.flush();
			out.close();
		} catch (Exception e) {
			Logger.error(e);
			throw new ServletException(e);
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		JSONObject paraJson = JSONObject.fromObject(request.getParameter("data"));
		String userId = paraJson.getString("userId");
		String groupId = paraJson.getString("groupId");
		PrintWriter out = response.getWriter();
		IFwLogin login = (IFwLogin) NCLocator.getInstance().lookup(nc.bs.framework.core.service.IFwLogin.class);
		NetStreamContext.setToken(login.login("yg", "111aaa", null));
		//	IMACoverPlanService PlanService = (IMACoverPlanService) NCLocatorFactory
		//	.getInstance().getCANCLocator()
		//	.lookup("nc.pubitf.ca.cuma.account.ma.IMACoverPlanService");
		List<Map> coverPlanList = null;//PlanService.getCoverPlanList(groupId,userId, null, null, "0", "10");
		String returnJson = JSONArray.fromObject(coverPlanList).toString();
		out.write(returnJson);
		//	nc.bs.framework.server.FindWebResourceServlet
		out.flush();
		out.close();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		this.doGet(request, response);
	}

	// public String readJSONString(HttpServletRequest request) {
	// StringBuffer json = new StringBuffer();
	// String line = null;
	// try {
	// BufferedReader reader = request.getReader();
	// while ((line = reader.readLine()) != null) {
	// json.append(line);
	// }
	// } catch (Exception e) {
	// System.out.println(e.toString());
	// }
	// return json.toString();
	// }

}
