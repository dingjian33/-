package nc.md;

import java.util.List;

import nc.bs.framework.common.RuntimeEnv;
import nc.bs.logging.Logger;
import nc.md.innerservice.MDQueryService;
import nc.md.model.IAttribute;
import nc.md.model.IBean;
import nc.md.model.IBusinessEntity;
import nc.md.model.IComponent;
import nc.md.model.IModule;
import nc.md.model.ITable;
import nc.md.model.MetaDataException;
import nc.md.model.MetaDataRuntimeException;
import nc.md.model.impl.BaseComponent;
import nc.md.model.type.IPrimitiveType;
import nc.md.model.type.IType;
import nc.md.persist.designer.vo.ClassVO;
import nc.md.util.MDUtil;
import nc.mddb.MDDBBaseQueryFacade;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.ml.NCLangRes4VoTransl;

/**
 * 对外部模块提供的元数据查询服务。
 * 
 * 所有外部模块如果需要查询元数据，都必须使用此查询服务,不得使用其他任何服务，否则导致的后果自负。
 * 
 * 支持前台/后台缓存功能
 * 
 * @author cch
 * 
 */
public class MDBaseQueryFacade implements IMDQueryFacade {

	private static MDBaseQueryFacade facadeInstance = null;

	private MDBaseQueryFacade() {
	}

	public static IMDQueryFacade getInstance() {
		if (facadeInstance == null)
			synchronized (MDBaseQueryFacade.class) {
				if (facadeInstance == null) {
					facadeInstance = new MDBaseQueryFacade();
				}
			}
		return facadeInstance;
	}

	/**
	 * 查询所有的根模块，如果没有则返回NULL
	 * 
	 * @return
	 * @throws MetaDataException
	 */
	public List<IModule> getRootModules() throws MetaDataException {
		if (RuntimeEnv.getInstance().isRunningInServer())
			return MDQueryService.lookupMDQueryService().getRootModules();
		else {
			return MDUICacheProxy.loadRootModules();
		}
	}

	/**
	 * 根据父模块id查询子模块
	 * 
	 * @param moduleID
	 * @return
	 * @throws MetaDataException
	 */
	public List<IModule> getModulesByParentID(String moduleID) throws MetaDataException {
		if (RuntimeEnv.getInstance().isRunningInServer())
			return MDQueryService.lookupMDInnerQueryService().getModulesByParentID(moduleID);
		else {
			return MDUICacheProxy.getModulesByParentID(moduleID);
		}
	}

	/**
	 * 根据模块名查询该模块里所有的组件
	 * 
	 * @return
	 * @throws MetaDataException
	 */
	public List<IComponent> getAllComponentsByModuleID(String moduleID) throws MetaDataException {
		if (RuntimeEnv.getInstance().isRunningInServer())
			return MDQueryService.lookupMDQueryService().getAllComponentsByModuleName(moduleID);
		else {
			return MDUICacheProxy.getAllComponentsByModuleID(moduleID);
		}
	}

	/**
	 * 根据属性全名查询相应的属性
	 * 
	 * @param attrFullName
	 *            形如：nameSpace.beanName.attrName
	 * @return
	 * @throws MetaDataException
	 */
	public IAttribute getAttributeByFullName(String attrFullName) throws MetaDataException {
		if (StringUtil.isEmpty(attrFullName))
			return null;
		String[] paths = attrFullName.split("\\.");
		//		方法中的路径必须为nameSpace.beanName.attrName的形式
		if (paths == null || paths.length != 3)
			throw new MetaDataException(NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0000")/*getAttributeByFullName方法中的路径必须为nameSpace.beanName.attrName的形式!*/
					+ attrFullName);

		IBean bean = getBeanByName(paths[0], paths[1]);
		if (bean == null)
			return null;
		return bean.getAttributeByName(paths[2]);
	}

	/**
	 * 根据属性名查询相应的属性
	 * 
	 * @param nameSpace
	 * @param entityName
	 * @param attrName
	 * @return
	 * @throws MetaDataException
	 */
	public IAttribute getAttributeByName(String nameSpace, String entityName, String attrName)
			throws MetaDataException {
		IBean bean = getBeanByName(nameSpace, entityName);
		if (bean == null)
			return null;
		return bean.getAttributeByName(attrName);
	}

	/**
	 * 根据bean的fullClassName查询相应的bean
	 * 
	 * @param entityClassName
	 * @return
	 * @throws MetaDataException
	 */
	public IBean getBeanByFullClassName(String entityClassName) throws MetaDataException {
		//for cglib
		int index = entityClassName.indexOf("$$");
		if (index != -1) {
			entityClassName = entityClassName.substring(0, index);
		}
		if (RuntimeEnv.getInstance().isRunningInServer())
			return MDQueryService.lookupMDQueryService().getBeanByFullClassName(entityClassName);
		else {
			return MDUICacheProxy.getBeanByFullClassName(entityClassName);
		}
	}

	/**
	 * 根据基本类型的index(比如IType.String)获取基本类型
	 * 
	 * @param index
	 * @return
	 */
	public IPrimitiveType getPrimitiveTypeByTypeIndex(int index) {
		BaseComponent baseComp = null;
		try {
			baseComp = (BaseComponent) getComponentByID(IComponent.BASE_COMPONENT_ID);
		} catch (MetaDataException e) {
			Logger.error(NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0001")/*查找基本类型组件失败!*/, e);
			throw new MetaDataRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0001")/*查找基本类型组件失败!*/ + e.getMessage());
		}
		if (baseComp == null)
			return null;
		List<IPrimitiveType> ptypes = baseComp.getAllPrimitiveTypes();
		for (IPrimitiveType primitiveType : ptypes) {
			if (index == primitiveType.getTypeType())
				return primitiveType;
		}
		String errorMSG = ptypes == null ? NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0002")/*本地类型为null*/ : NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0003")/*本地类型数量*/ + ptypes.size();
		Logger.error(NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0004")/*未找到相关的基本类型，基本类型index=*/ + index + NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0005")/*;本地基本类型：*/ + errorMSG);
		return null;
	}

	/**
	 * 根据bean的fullClassName查询相应的bean
	 * 
	 * @param entityClassName
	 * @return
	 * @throws MetaDataException
	 */
	public IType getTypeByFullClassName(String typeClassName) throws MetaDataException {

		if (RuntimeEnv.getInstance().isRunningInServer())
			return MDQueryService.lookupMDQueryService().getTypeByFullClassName(typeClassName);
		else {
			return MDUICacheProxy.getTypeByFullClassName(typeClassName);
		}
	}

	/**
	 * 根据typeID查询相应的IType
	 * 
	 * @param typeID
	 * @param nStyle
	 * @return
	 * @throws MetaDataException
	 */
	public IType getTypeByID(String typeID, int nStyle) throws MetaDataException {
		if (RuntimeEnv.getInstance().isRunningInServer())
			return MDQueryService.lookupMDInnerQueryService().getTypeByCombineID(typeID, nStyle);
		else {
			IComponent comp = getComponentByTypeID(typeID);
			if (comp == null)
				return null;
			return MDUtil.getCompositeType(comp.getTypeByID(typeID), nStyle);
		}
	}

	public List<ClassVO> getAllTypesByTag(String tag) throws MetaDataException {
		return MDQueryService.lookupMDQueryService().getAllTypesByTag(tag);
	}

	/**
	 * 根据beanID查询相应的bean
	 * 
	 * @param beanID
	 * @return
	 * @throws MetaDataException
	 */
	public IBean getBeanByID(String beanID) throws MetaDataException {
		IType type = getTypeByID(beanID, IType.STYLE_SINGLE);
		if (type == null) { throw new IllegalArgumentException("Type can't be null! beanID:" + beanID); }
		if (MDUtil.isMDBean(type))
			return (IBean) type;
		return null;
	}

	/**
	 * 根据bean全名查询相应的bean
	 * 
	 * @param beanFullName
	 *            形如：nameSpace.beanName
	 * @return
	 * @throws MetaDataException
	 */
	public IBean getBeanByFullName(String beanFullName) throws MetaDataException {

		if (StringUtil.isEmpty(beanFullName))
			return null;
		String[] paths = beanFullName.split("\\.");
		//		路径必须为nameSpace.beanName的形式!
		if (paths == null || paths.length != 2)
			throw new MetaDataException(NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0006")/*getBeanByFullName方法中的路径必须为nameSpace.beanName的形式!*/ + beanFullName);

		return getBeanByName(paths[0], paths[1]);
	}

	/**
	 * 通过beanID得到组件
	 * 
	 * @param beanID
	 * @return
	 */
	public IComponent getComponentByTypeID(String beanID) throws MetaDataException {
		if (beanID == null)
			return null;
		if (RuntimeEnv.getInstance().isRunningInServer()) {
			return MDQueryService.lookupMDInnerQueryService().getComponentByTypeID(beanID);
		} else {
			return MDUICacheProxy.getComponentByTypeID(beanID);
		}
	}

	/**
	 * 根据bean全名查询相应的bean
	 * 
	 * @param nameSpace
	 * @param beanName
	 * @return
	 * @throws MetaDataException
	 */
	public IBean getBeanByName(String nameSpace, String beanName) throws MetaDataException {
		if (RuntimeEnv.getInstance().isRunningInServer()) {
			return MDQueryService.lookupMDQueryService().getBeanByName(nameSpace, beanName);
		} else {
			return MDUICacheProxy.getBeanByName(nameSpace, beanName);
		}
	}

	/**
	 * 根据实体全名查询相应的实体
	 * 
	 * @param entityFullName
	 * @return
	 * @throws MetaDataException
	 */
	public IBusinessEntity getBusinessEntityByFullName(String entityFullName)
			throws MetaDataException {
		IBean bean = getBeanByFullName(entityFullName);
		if (MDUtil.isEntityType(bean))
			return (IBusinessEntity) bean;
		return null;
	}

	/**
	 * 根据实体全名查询相应的实体
	 * 
	 * @param nameSpace
	 * @param entityName
	 * @return
	 * @throws MetaDataException
	 */
	public IBusinessEntity getBusinessEntityByName(String nameSpace, String entityName)
			throws MetaDataException {
		IBean bean = getBeanByName(nameSpace, entityName);
		if (MDUtil.isEntityType(bean))
			return (IBusinessEntity) bean;
		return null;
	}

	/**
	 * 根据组件id和场景来获得实体
	 * @param compID
	 * @param power
	 * @return
	 * @throws MetaDataException
	 */
	public List<IBusinessEntity> getBusinessEntityByComAndPower(String compID,String power)
			throws MetaDataException{
		if(StringUtil.isEmpty(compID)||StringUtil.isEmpty(power)){
			return null;
		}
		return MDQueryService.lookupManagerQueryService().getBusinessEntitiesByComIDAndPower(compID, power);
	}
	
	/**
	 * 根据组件id和场景来获得实体
	 * @param moduleID
	 * @param power
	 * @return
	 * @throws MetaDataException
	 */
	public List<IBusinessEntity> getBusinessEntityByModuleIDAndPower(String moduleID,String power)
		throws MetaDataException{
		if(StringUtil.isEmpty(moduleID)||StringUtil.isEmpty(power)){
			return null;
		}
		if (RuntimeEnv.getInstance().isRunningInServer()){
			return MDQueryService.lookupManagerQueryService().getBusinessEntityByModuleIDAndPower(moduleID, power);
		}else{
			return MDUICacheProxy.getBusinessByModuleIDAndPower(moduleID, power);
		}
		
	}
	/**
	 * 根据组件全名查询相应的组件
	 * 
	 * @param compName
	 * @return
	 * @throws MetaDataException
	 */
	public IComponent getComponentByName(String compName) throws MetaDataException {

		if (StringUtil.isEmpty(compName))
			return null;
		if (RuntimeEnv.getInstance().isRunningInServer())
			return MDQueryService.lookupMDQueryService().getComponentByName(compName);
		else {
			return MDUICacheProxy.getComponentByName(compName);
		}
	}

	/**
	 * 根据组件ID查找组件
	 * 
	 * @param compID
	 * @return
	 * @throws MetaDataException
	 */
	public IComponent getComponentByID(String compID) throws MetaDataException {
		if (StringUtil.isEmpty(compID))
			return null;
		if (RuntimeEnv.getInstance().isRunningInServer())
			return MDQueryService.lookupMDInnerQueryService().getComponentByID(compID);
		else {
			return MDUICacheProxy.getComponentByID(compID);
		}
	}

	/**
	 * 根据模块名(模块ID和模块名相同)查询相应的模块
	 * 
	 * @param moduleName
	 * @return
	 * @throws MetaDataException
	 */
	public IModule getModuleByName(String moduleName) throws MetaDataException {
		if (RuntimeEnv.getInstance().isRunningInServer()) {
			return MDQueryService.lookupMDQueryService().getModuleByName(moduleName);
		} else {
			return MDUICacheProxy.getModuleByName(moduleName);
		}
	}

	/**
	 * 根据基本类型的index(比如IType.String)获取基本类型
	 * 
	 * @param index
	 * @return
	 */
	public IPrimitiveType getPrimitiveTypeByFullClassName(String fullClassName) {
		BaseComponent baseComp = null;
		try {
			baseComp = (BaseComponent) getComponentByID(IComponent.BASE_COMPONENT_ID);
		} catch (MetaDataException e) {
			Logger.error(NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0001")/*查找基本类型组件失败!*/, e);
			throw new MetaDataRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0001")/*查找基本类型组件失败!*/ + e.getMessage());
		}
		if (baseComp == null)
			return null;
		List<IPrimitiveType> ptypes = baseComp.getAllPrimitiveTypes();
		for (IPrimitiveType primitiveType : ptypes) {
			if (primitiveType.getFullClassName().equals(fullClassName))
				return primitiveType;
		}
		return null;
	}

	/**
	 * 根据tableName获得table
	 * @param tableID
	 * @return
	 * @throws MetaDataException
	 */
	public ITable getTableByName(String tableName) throws MetaDataException {
		return getTableByID(tableName);
	}

	/**
	 * 根据tableID获得table
	 * @param tableID
	 * @return
	 * @throws MetaDataException
	 */
	public ITable getTableByID(String tableID) throws MetaDataException {
		return MDDBBaseQueryFacade.getInstance().getTableByID(tableID);
	}

	/**
	 * 加载元数据到前台
	 * @param infos
	 */
	public void loadMDByPath(MDPathInfo[] infos) throws MetaDataException {
		if (RuntimeEnv.getInstance().isRunningInServer()) {
			return;
		} else {
			MDUICacheProxy.loadMDByPath(infos);
		}
	}
	
	/**
	 * 根据业务模块Code查询映射组件
	 * @param moduleCode
	 * @return
	 * @throws MetaDataException
	 */
	public List<IComponent> getComponentsByBusiModule(String moduleCode) 
			throws MetaDataException{
		return MDQueryService.lookupManagerQueryService().getComponentsByBusiModule(moduleCode);
	}
}
