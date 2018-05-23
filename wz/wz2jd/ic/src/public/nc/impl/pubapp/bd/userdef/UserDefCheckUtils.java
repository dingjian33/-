package nc.impl.pubapp.bd.userdef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.bd.userdefitem.IUserdefitemQryService;
import nc.md.MDBaseQueryFacade;
import nc.md.model.IBean;
import nc.md.model.MetaDataException;
import nc.md.model.type.IType;
import nc.vo.bd.userdefrule.UserdefitemVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.pub.IAttributeMeta;
import nc.vo.pub.ISuperVO;
import nc.vo.pub.IVOMeta;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.pub.lang.UFTime;
import nc.vo.pubapp.pattern.exception.ExceptionUtils;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;

public class UserDefCheckUtils {
  private static class UserDefQueryParam {
    String prefix;

    String ruleCode;

    public UserDefQueryParam(String prefix, String ruleCode) {
      super();
      this.prefix = prefix;
      this.ruleCode = ruleCode;
    }

  }

  /**
   * 
   * 校验自定义项类型，由于很多单据的前缀都是vdef和vbdef，并且元数据路径也可以通过所属vo的
   * class获取，所以可以简化参数，自定义项的元数据路径/编码规则可以不传
   * 
   * @param vos 单据vo数据
   * @param voClasses 自定义项所在实体(主子实体)的class
   */
  public static <E extends AbstractBill> void check(E[] vos, Class[] voClasses) {
    // List<String> prefixs = new ArrayList<String>();
    // prefixs.add("vdef");
    // if (voClasses.length == 2) {
    // prefixs.add("vbdef");
    // }
    List<String> prefixs = UserDefCheckUtils.getDefaultPrefix(voClasses.length);
    UserDefCheckUtils.check(vos, prefixs.toArray(new String[0]), voClasses);
  }

  /**
   * 
   * 校验自定义项类型，自定义项的元数据路径通过vo class获取
   * 
   * @param vos 单据vo数据
   * @param prefixs 自定义项前缀
   * @param voClasses 自定义项所在实体(主子实体)的class
   */
  public static <E extends AbstractBill> void check(E[] vos, String[] prefixs,
      Class[] voClasses) {
    if (prefixs == null) {
      UserDefCheckUtils.check(vos, voClasses);
    }
    // List<String> ruleCodes = new ArrayList<String>();
    // for (int i = 0; i < voClasses.length; i++) {
    // try {
    // IBean bean =
    // MDBaseQueryFacade.getInstance().getBeanByFullClassName(
    // voClasses[i].getName());
    // ruleCodes.add(bean.getFullName());
    // }
    // catch (MetaDataException e) {
    // ExceptionUtils.wrappException(e);
    // }
    // }
    List<String> ruleCodes = UserDefCheckUtils.findRuleCodes(voClasses);
    UserDefCheckUtils.check(vos, ruleCodes.toArray(new String[0]), prefixs,
        voClasses);
  }

  /**
   * 
   * 单据可能包含多个实体，需要指定每个实体所引用自定义项的编码规则，以及字段前缀
   * 注意ruleCodes、prefixs、voClasses这几个数组参数中的元素位置要一一对应，同时
   * 有些单据如果满足：主表前缀是vdef，子表前缀使用vbdef
   * 也可以调用简化参数的{{@link #check(AbstractBill[], Class[])}或{
   * {@link #check(AbstractBill[], String[], Class[])}方法
   * 
   * @param vos 单据vo数据
   * @param ruleCodes 自定义项规则编码
   * @param prefixs 自定义项前缀
   * @param voClasses 自定义项所在实体(主子实体)的class
   */
  public static <E extends AbstractBill> void check(E[] vos,
      String[] ruleCodes, String[] prefixs, Class[] voClasses) {
    if (ruleCodes == null) {
      List<String> ruleCodeList = UserDefCheckUtils.findRuleCodes(voClasses);
      UserDefCheckUtils.check(vos, ruleCodeList.toArray(new String[0]),
          prefixs, voClasses);
      return;
    }
    if (prefixs == null) {
      List<String> prefixList =
          UserDefCheckUtils.getDefaultPrefix(voClasses.length);
      UserDefCheckUtils.check(vos, ruleCodes,
          prefixList.toArray(new String[0]), voClasses);
      return;
    }
    Map<Class<? extends ISuperVO>, UserDefQueryParam> paramMap =
        UserDefCheckUtils.initQueryParam(ruleCodes, prefixs, voClasses);
    String pk_group = InvocationInfoProxy.getInstance().getGroupId();
    Map<String, List<UserdefitemVO>> UserdefitemVOMap =
        UserDefCheckUtils.queryUserDefitems(pk_group, Arrays.asList(ruleCodes));
    try {
      for (int i = 0; vos != null && i < vos.length; i++) {
        Set<Entry<Class<? extends ISuperVO>, UserDefQueryParam>> entrySet =
            paramMap.entrySet();
        for (Entry<Class<? extends ISuperVO>, UserDefQueryParam> entry : entrySet) {
          Class<? extends ISuperVO> voClass = entry.getKey();
          UserDefQueryParam param = entry.getValue();
          String prefix = param.prefix;
          String ruleCode = param.ruleCode;
          ISuperVO[] superVOs = null;
          if (vos[i].getParent() != null
              && vos[i].getParent().getClass() == voClass) {
            superVOs = new ISuperVO[] {
              vos[i].getParent()
            };
          }
          else if (vos[i].getParent().getClass() != voClass) {
            superVOs = vos[i].getChildren(voClass);
          }
          if (superVOs == null) {
            continue;
          }
          // UserDefCheckUtils.check(superVOs, prefix, ruleCode);
          /* 2015-5-5 wangweir 修改原因 Begin*/
//          UserDefCheckUtils.check(superVOs, prefix, ruleCode, UserdefitemVOMap);
          UserDefCheckUtils.check636(superVOs, prefix, ruleCode,
              UserdefitemVOMap);
          /* 2015-5-5 wangweir End*/
        }

      }
    }
    catch (Exception e) {
      ExceptionUtils.wrappException(e);
    }

  }

  /**
   * 校验自定义项类型是否一致
   * 
   * @param vos
   * @param prefix
   */
  // private static void check(ISuperVO[] vos, String prefix, String ruleCode) {
  // if (vos == null || vos.length == 0) {
  // return;
  // }
  // try {
  // IBean bean =
  // MDBaseQueryFacade.getInstance().getBeanByFullClassName(
  // vos[0].getClass().getName());
  // String pk_group = InvocationInfoProxy.getInstance().getGroupId();
  // Map<Integer, UserdefitemVO> userdefitemVOMap =
  // UserDefCheckUtils.getUserDefitem(pk_group, ruleCode);
  // for (ISuperVO superVO : vos) {
  // Set<String> attrNames = superVO.usedAttributeNames();
  // for (String attrName : attrNames) {
  // if (UserDefCheckUtils.isUserDefColumn(attrName, prefix)) {
  // // String indexStr = attrName.substring(attrName.length() - 1);
  // String indexStr =
  // attrName.substring(prefix.length(), attrName.length());
  // Integer index = Integer.valueOf(indexStr);
  // // Object objValue = superVO.getAttributeValue(prefix + index);
  // Object objValue = superVO.getAttributeValue(attrName);
  // if (objValue != null) {
  // UserdefitemVO itemVO = userdefitemVOMap.get(index);
  // if (itemVO == null) {
  // ExceptionUtils.wrappBusinessException(NCLangRes4VoTransl
  // .getNCLangRes().getStrByID("pubapp_0", "0pubapp-0344",
  // null, new String[] {
  // bean.getDisplayName(), "" + index
  // })/*@res {0}没有引用自定义项[ {1} ]。*/);
  // return;
  // }
  // IType iType =
  // MDBaseQueryFacade.getInstance().getTypeByID(
  // itemVO.getClassid(), IType.STYLE_SINGLE);
  //
  // UserDefCheckUtils.checkType(iType.getTypeType(),
  // objValue.toString(), itemVO.getShowname(), bean);
  // }
  // }
  // }
  // }
  // }
  // catch (Exception e) {
  // ExceptionUtils.wrappException(e);
  // }
  // }
  private static void check636(ISuperVO[] vos, String prefix, String ruleCode,
      Map<String, List<UserdefitemVO>> UserdefitemVOMap) {
    if (vos == null || vos.length == 0) {
      return;
    }
    try {
      IBean bean =
          MDBaseQueryFacade.getInstance().getBeanByFullClassName(
              vos[0].getClass().getName());
      Map<Integer, UserdefitemVO> userdefitemVOMap =
          UserDefCheckUtils.getUserDefitem(ruleCode, UserdefitemVOMap);
      /* 2015-5-5 wangweir 效率问题 Begin*/
      
      //1、获取自定义项字段
      IVOMeta meta = vos[0].getMetaData();
      IAttributeMeta[] attributes = meta.getAttributes();
      List<String> typeCustomerFields = new ArrayList<String>();
      Map<String, Integer> filed2Index = new HashMap<String, Integer>();
      for (IAttributeMeta attribute : attributes) {
        String name = attribute.getName();
        if (attribute.getModelType() == IType.TYPE_CUSTOM
            && UserDefCheckUtils.isUserDefColumn(name, prefix)) {
          typeCustomerFields.add(name);
          String indexStr = name.substring(prefix.length(), name.length());
          Integer index = Integer.valueOf(indexStr);
          filed2Index.put(name, index);
        }
      }

      if (typeCustomerFields.size() == 0) {
        return;
      }

      //2、优先缓存字段，再循环VO。一般情况下字段的个数确定
      for (String field : typeCustomerFields) {
        int index = filed2Index.get(field);
        UserdefitemVO itemVO = userdefitemVOMap.get(index);
        IType iType = null;
        if (itemVO != null) {
          iType =
              MDBaseQueryFacade.getInstance().getTypeByID(itemVO.getClassid(),
                  IType.STYLE_SINGLE);
        }
        for (ISuperVO superVO : vos) {
          Object objValue = superVO.getAttributeValue(field);
          if (objValue == null) {
            continue;
          }

          if (itemVO == null) {
            ExceptionUtils.wrappBusinessException(NCLangRes4VoTransl
                .getNCLangRes().getStrByID("pubapp_0", "0pubapp-0344", null,
                    new String[] {
                      bean.getDisplayName(), "" + index
                    })/*@res {0}没有引用自定义项[ {1} ]。*/);
            return;
          }
          UserDefCheckUtils.checkType(iType.getTypeType(), objValue.toString(),
              itemVO.getShowname(), bean);
        }

      }
    }
    catch (Exception e) {
      ExceptionUtils.wrappException(e);
    }
    /* 2015-5-5 wangweir End*/
  }

  private static void check(ISuperVO[] vos, String prefix, String ruleCode,
      Map<String, List<UserdefitemVO>> UserdefitemVOMap) {
    if (vos == null || vos.length == 0) {
      return;
    }
    try {
      IBean bean =
          MDBaseQueryFacade.getInstance().getBeanByFullClassName(
              vos[0].getClass().getName());
      // String pk_group = InvocationInfoProxy.getInstance().getGroupId();
      Map<Integer, UserdefitemVO> userdefitemVOMap =
          UserDefCheckUtils.getUserDefitem(ruleCode, UserdefitemVOMap);
      IType type;
      for (ISuperVO superVO : vos) {
        Set<String> attrNames = superVO.usedAttributeNames();
        for (String attrName : attrNames) {
        	if (null == bean.getAttributeByName(attrName)) {
        		continue;
        	}
          type = bean.getAttributeByName(attrName).getDataType();

          if (IType.TYPE_CUSTOM== type.getTypeType() && UserDefCheckUtils.isUserDefColumn(attrName, prefix)) {
            // String indexStr = attrName.substring(attrName.length() - 1);
            String indexStr =
                attrName.substring(prefix.length(), attrName.length());
            Integer index = Integer.valueOf(indexStr);
            // Object objValue = superVO.getAttributeValue(prefix + index);
            Object objValue = superVO.getAttributeValue(attrName);
            if (objValue != null) {
              UserdefitemVO itemVO = userdefitemVOMap.get(index);
              if (itemVO == null) {
                ExceptionUtils.wrappBusinessException(NCLangRes4VoTransl
                    .getNCLangRes().getStrByID("pubapp_0", "0pubapp-0344",
                        null, new String[] {
                          bean.getDisplayName(), "" + index
                        })/*@res {0}没有引用自定义项[ {1} ]。*/);
                return;
              }
              IType iType =
                  MDBaseQueryFacade.getInstance().getTypeByID(
                      itemVO.getClassid(), IType.STYLE_SINGLE);

              UserDefCheckUtils.checkType(iType.getTypeType(),
                  objValue.toString(), itemVO.getShowname(), bean);
            }
          }
        }
      }
    }
    catch (Exception e) {
      ExceptionUtils.wrappException(e);
    }
  }

  /**
   * 校验类型
   * 
   * @param type
   * @param obj
   * @param itemShowName
   */
  private static void checkType(int type, Object obj, String itemShowName,
      IBean bean) {
    String objValue = obj.toString();
    try {
      switch (type) {
        case IType.TYPE_UFDouble:
        case IType.TYPE_UFMoney:
          new UFDouble(objValue);
          break;
        case IType.TYPE_UFBoolean:
          if (!objValue.equals("Y") && !objValue.equals("N")) {
            ExceptionUtils.wrappBusinessException(NCLangRes4VoTransl
                .getNCLangRes().getStrByID("pubapp_0", "0pubapp-0343", null,
                    new String[] {
                      bean.getDisplayName(), itemShowName
                    })/*@res {0}自定义项[ {1} ]类型错误。*/);
          }
          break;
        case IType.TYPE_UFDate:
          new UFDate(objValue);
          break;
        case IType.TYPE_UFDATE_LITERAL:
          new UFLiteralDate(objValue);
          break;
        case IType.TYPE_UFDateTime:
          new UFDateTime(objValue);
          break;
        case IType.TYPE_UFTime:
          new UFTime(objValue);
          break;
        case IType.TYPE_Integer:
          Integer.parseInt(objValue);
          break;
        case IType.TYPE_UFID:
        case IType.TYPE_String:
        case IType.REF:
        case IType.MULTILANGUAGE:
        case IType.ENTITY:
        case IType.TYPE_CONVERSIONRATE:
          String.valueOf(obj);
          break;
        default:
          ExceptionUtils.unSupported();
      }
    }
    catch (Exception e) {
      ExceptionUtils.wrappBusinessException(NCLangRes4VoTransl.getNCLangRes()
          .getStrByID("pubapp_0", "0pubapp-0343", null, new String[] {
            bean.getDisplayName(), itemShowName
          })/*@res {0}自定义项[ {1} ]类型错误。*/);
    }
  }

  private static List<String> findRuleCodes(Class[] voClasses) {
    List<String> ruleCodes = new ArrayList<String>();
    for (int i = 0; i < voClasses.length; i++) {
      try {
        IBean bean =
            MDBaseQueryFacade.getInstance().getBeanByFullClassName(
                voClasses[i].getName());
        ruleCodes.add(bean.getFullName());
      }
      catch (MetaDataException e) {
        ExceptionUtils.wrappException(e);
      }
    }
    return ruleCodes;
  }

  private static List<String> getDefaultPrefix(int num) {
    List<String> prefixs = new ArrayList<String>();
    prefixs.add("vdef");
    if (num == 2) {
      prefixs.add("vbdef");
    }
    return prefixs;
  }

  // private static Map<Integer, UserdefitemVO> getUserDefitem(String pk_org,
  // String ruleCode) {
  // // 查询自定义项
  // UserdefitemVO[] defs = UserDefCheckUtils.queryUserDefitem(pk_org,
  // ruleCode);
  // return UserDefCheckUtils.prefixItems(defs);
  // }

  private static Map<Integer, UserdefitemVO> getUserDefitem(String ruleCode,
      Map<String, List<UserdefitemVO>> UserdefitemVOMap) {
    List<UserdefitemVO> list = UserdefitemVOMap.get(ruleCode);
    UserdefitemVO[] defs = null;
    if (list == null) {
      defs = new UserdefitemVO[0];
    }
    else {
      defs = list.toArray(new UserdefitemVO[0]);
    }
    return UserDefCheckUtils.prefixItems(defs);
  }

  private static Map<Class<? extends ISuperVO>, UserDefQueryParam> initQueryParam(
      String[] ruleCodes, String[] prefixs, Class[] voClasses) {
    Map<Class<? extends ISuperVO>, UserDefQueryParam> paramMap =
        new HashMap<Class<? extends ISuperVO>, UserDefQueryParam>();
    // paramMap.clear();
    for (int i = 0; i < voClasses.length; i++) {
      UserDefQueryParam param = new UserDefQueryParam(prefixs[i], ruleCodes[i]);
      paramMap.put(voClasses[i], param);
    }
    return paramMap;
  }

  private static boolean isUserDefColumn(String column, String prefix) {
    if (column.startsWith(prefix)) {
      // String index = column.substring(column.length() - 1);
      String index = column.substring(prefix.length(), column.length());
      try {
        Integer i = Integer.valueOf(index);
        if (i > 0) {
          return true;
        }
      }
      catch (Exception e) {
        return false;
      }
    }
    return false;
  }

  private static Map<Integer, UserdefitemVO> prefixItems(UserdefitemVO[] items) {
    Map<Integer, UserdefitemVO> map = new HashMap<Integer, UserdefitemVO>();
    for (UserdefitemVO item : items) {
      int newIndex = item.getPropindex().intValue();
      // item.setPropindex(Integer.valueOf(newIndex));
      map.put(Integer.valueOf(newIndex), item);
    }
    return map;
  }

  // private static UserdefitemVO[] queryUserDefitem(String pk_org, String
  // ruleCode) {
  // // 获取自定义项
  // IUserdefitemQryService service =
  // NCLocator.getInstance().lookup(IUserdefitemQryService.class);
  //
  // UserdefitemVO[] defs = null;
  // try {
  // List<String> userdefruleCodes = new ArrayList<String>();
  // userdefruleCodes.add(ruleCode);
  // List<String> mdFullNames = new ArrayList<String>();
  // mdFullNames.add(ruleCode);
  // List<UserdefitemVO> deflist =
  // service.queryUserdefitemVOsByRuleCodesOrMDFullNames(userdefruleCodes,
  // mdFullNames, pk_org).get(ruleCode);
  // // defs = service.queryUserdefitemVOsByUserdefruleCode(ruleCode, pk_org);
  //
  // // if (deflist == defs) {
  // // defs = service.qeuryUserdefitemVOsByMDClassFullName(ruleCode, pk_org);
  // // }
  // if (null == deflist) {
  // defs = new UserdefitemVO[0];
  // }
  // else {
  // defs = deflist.toArray(new UserdefitemVO[0]);
  // }
  // }
  // catch (BusinessException e) {
  // ExceptionUtils.wrappException(e);
  // }
  // return defs;
  // }

  private static Map<String, List<UserdefitemVO>> queryUserDefitems(
      String pk_org, List<String> ruleCodes) {
    // 获取自定义项
    IUserdefitemQryService service =
        NCLocator.getInstance().lookup(IUserdefitemQryService.class);
    Map<String, List<UserdefitemVO>> defs = null;
    try {
      defs =
          service.queryUserdefitemVOsByRuleCodesOrMDFullNames(ruleCodes,
        		 ruleCodes, pk_org);

    }
    catch (BusinessException e) {
      ExceptionUtils.wrappException(e);
    }
    return defs;
  }

}
