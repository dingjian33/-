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
 * ���ⲿģ���ṩ��Ԫ���ݲ�ѯ����
 * 
 * �����ⲿģ�������Ҫ��ѯԪ���ݣ�������ʹ�ô˲�ѯ����,����ʹ�������κη��񣬷����µĺ���Ը���
 * 
 * ֧��ǰ̨/��̨���湦��
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
	 * ��ѯ���еĸ�ģ�飬���û���򷵻�NULL
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
	 * ���ݸ�ģ��id��ѯ��ģ��
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
	 * ����ģ������ѯ��ģ�������е����
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
	 * ��������ȫ����ѯ��Ӧ������
	 * 
	 * @param attrFullName
	 *            ���磺nameSpace.beanName.attrName
	 * @return
	 * @throws MetaDataException
	 */
	public IAttribute getAttributeByFullName(String attrFullName) throws MetaDataException {
		if (StringUtil.isEmpty(attrFullName))
			return null;
		String[] paths = attrFullName.split("\\.");
		//		�����е�·������ΪnameSpace.beanName.attrName����ʽ
		if (paths == null || paths.length != 3)
			throw new MetaDataException(NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0000")/*getAttributeByFullName�����е�·������ΪnameSpace.beanName.attrName����ʽ!*/
					+ attrFullName);

		IBean bean = getBeanByName(paths[0], paths[1]);
		if (bean == null)
			return null;
		return bean.getAttributeByName(paths[2]);
	}

	/**
	 * ������������ѯ��Ӧ������
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
	 * ����bean��fullClassName��ѯ��Ӧ��bean
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
	 * ���ݻ������͵�index(����IType.String)��ȡ��������
	 * 
	 * @param index
	 * @return
	 */
	public IPrimitiveType getPrimitiveTypeByTypeIndex(int index) {
		BaseComponent baseComp = null;
		try {
			baseComp = (BaseComponent) getComponentByID(IComponent.BASE_COMPONENT_ID);
		} catch (MetaDataException e) {
			Logger.error(NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0001")/*���һ����������ʧ��!*/, e);
			throw new MetaDataRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0001")/*���һ����������ʧ��!*/ + e.getMessage());
		}
		if (baseComp == null)
			return null;
		List<IPrimitiveType> ptypes = baseComp.getAllPrimitiveTypes();
		for (IPrimitiveType primitiveType : ptypes) {
			if (index == primitiveType.getTypeType())
				return primitiveType;
		}
		String errorMSG = ptypes == null ? NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0002")/*��������Ϊnull*/ : NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0003")/*������������*/ + ptypes.size();
		Logger.error(NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0004")/*δ�ҵ���صĻ������ͣ���������index=*/ + index + NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0005")/*;���ػ������ͣ�*/ + errorMSG);
		return null;
	}

	/**
	 * ����bean��fullClassName��ѯ��Ӧ��bean
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
	 * ����typeID��ѯ��Ӧ��IType
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
	 * ����beanID��ѯ��Ӧ��bean
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
	 * ����beanȫ����ѯ��Ӧ��bean
	 * 
	 * @param beanFullName
	 *            ���磺nameSpace.beanName
	 * @return
	 * @throws MetaDataException
	 */
	public IBean getBeanByFullName(String beanFullName) throws MetaDataException {

		if (StringUtil.isEmpty(beanFullName))
			return null;
		String[] paths = beanFullName.split("\\.");
		//		·������ΪnameSpace.beanName����ʽ!
		if (paths == null || paths.length != 2)
			throw new MetaDataException(NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0006")/*getBeanByFullName�����е�·������ΪnameSpace.beanName����ʽ!*/ + beanFullName);

		return getBeanByName(paths[0], paths[1]);
	}

	/**
	 * ͨ��beanID�õ����
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
	 * ����beanȫ����ѯ��Ӧ��bean
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
	 * ����ʵ��ȫ����ѯ��Ӧ��ʵ��
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
	 * ����ʵ��ȫ����ѯ��Ӧ��ʵ��
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
	 * �������id�ͳ��������ʵ��
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
	 * �������id�ͳ��������ʵ��
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
	 * �������ȫ����ѯ��Ӧ�����
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
	 * �������ID�������
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
	 * ����ģ����(ģ��ID��ģ������ͬ)��ѯ��Ӧ��ģ��
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
	 * ���ݻ������͵�index(����IType.String)��ȡ��������
	 * 
	 * @param index
	 * @return
	 */
	public IPrimitiveType getPrimitiveTypeByFullClassName(String fullClassName) {
		BaseComponent baseComp = null;
		try {
			baseComp = (BaseComponent) getComponentByID(IComponent.BASE_COMPONENT_ID);
		} catch (MetaDataException e) {
			Logger.error(NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0001")/*���һ����������ʧ��!*/, e);
			throw new MetaDataRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("mdbusi", "mdBaseQueryFacade-0001")/*���һ����������ʧ��!*/ + e.getMessage());
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
	 * ����tableName���table
	 * @param tableID
	 * @return
	 * @throws MetaDataException
	 */
	public ITable getTableByName(String tableName) throws MetaDataException {
		return getTableByID(tableName);
	}

	/**
	 * ����tableID���table
	 * @param tableID
	 * @return
	 * @throws MetaDataException
	 */
	public ITable getTableByID(String tableID) throws MetaDataException {
		return MDDBBaseQueryFacade.getInstance().getTableByID(tableID);
	}

	/**
	 * ����Ԫ���ݵ�ǰ̨
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
	 * ����ҵ��ģ��Code��ѯӳ�����
	 * @param moduleCode
	 * @return
	 * @throws MetaDataException
	 */
	public List<IComponent> getComponentsByBusiModule(String moduleCode) 
			throws MetaDataException{
		return MDQueryService.lookupManagerQueryService().getComponentsByBusiModule(moduleCode);
	}
}
