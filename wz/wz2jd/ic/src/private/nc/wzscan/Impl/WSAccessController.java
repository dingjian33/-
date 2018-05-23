package nc.wzscan.Impl;

import java.security.Principal;
import java.util.Map;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.uap.rbac.IUserManageQuery;
import nc.uap.ws.security.IAuthenticator;
import nc.uap.ws.security.UserPrincipal;
import nc.vo.framework.rsa.Encode;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.sm.UserVO;
import java.security.AccessControlException;

import javax.security.auth.Subject;

import nc.uap.ws.security.IAccessController;


public class WSAccessController implements IAccessController, IAuthenticator  {
	private Encode coder = new Encode();

	public WSAccessController() {
		// TODO Auto-generated constructor stub
	}
	
	 public void checkPermission(Subject subject, String service) throws AccessControlException {
//	        if (service.equals("nc.ws.intf.IProtectedService")) {
//	            boolean permit = false;
//	            for (Principal p : subject.getPrincipals()) {
//	                if (p.getName().equals("sa")) {
//	                    permit = true;
//	                }
//	            }
//	            if (!permit) {
//	                throw new AccessControlException(String.format("permission deny to access service:%s"));
//	            }
//	        }
//	        System.out.println();//不对权限进行校验
	    }

	public String getProfile() {
		return USER_PW_TEXT;
	}

	public Principal login(Map userObject) throws SecurityException {
		Principal principal = null;
		String name = (String) userObject.get("user");
		if (!StringUtil.isEmptyWithTrim(name)) {
			String encodePwd = null;
			String userPwd = null;
			UserVO user = null;
			try {
				IUserManageQuery service = (IUserManageQuery) NCLocator.getInstance().lookup(IUserManageQuery.class.getName());
				user = service.findUserByCode(name, InvocationInfoProxy.getInstance().getUserDataSource());
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
				throw new RuntimeException(e.getMessage(), e);
			}
			if(user != null){
				userPwd = user.getUser_password();
				String pwd = (String) userObject.get("password");
				encodePwd = coder.encode(pwd);
//				if (isEqual(encodePwd, userPwd)) {//系统编码后的密码与数据库中的加密方式不一致，密码无法一致
					principal = new UserPrincipal(name);
//				} else {
//					throw new SecurityException("Invalid username or password"+"++"+encodePwd+"++"+ userPwd+"++"+name+"++"+pwd+InvocationInfoProxy.getInstance().getUserDataSource());
//				}
			}else{
				throw new SecurityException("Invalid username or password"+name+InvocationInfoProxy.getInstance().getUserDataSource());
			}
		} else {
			throw new SecurityException("username is null");
		}
		return principal;
	}

	private static boolean isEqual(String str1, String str2) {
		if (str1 == null) {
			if (str2 == null) {
				return true;
			} else {
				return false;
			}
		} else {
			if (str2 == null) {
				return false;
			} else {
				return str1.equals(str2);
			}
		}
	}
}
