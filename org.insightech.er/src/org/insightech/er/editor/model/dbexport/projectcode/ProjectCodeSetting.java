package org.insightech.er.editor.model.dbexport.projectcode;

/**
 * 项目配置
 * @author ljc
 *
 */
public class ProjectCodeSetting {

	private String beanPath;
	private String daoPath;
	private String servicePath;
	private String serviceImplPath;
	private String mybatisXmlPath;
	private String componentPath;
	private String componentImplPath;
	private String controllerPath;
	private String viewPath;
	
	public void setBeanPath(String path)
	{
		this.beanPath=path;
	}
	
	public String getBeanPath()
	{
		return this.beanPath;
	}
	
	public void setDaoPath(String path)
	{
		this.daoPath=path;
	}
	
	public String getDaoPath()
	{
		return this.daoPath;
	}
	
	public void setServicePath(String path)
	{
		this.servicePath=path;
	}
	
	public String getServicePath()
	{
		return this.servicePath;
	}
	
	public void setServiceImplPath(String path)
	{
		this.serviceImplPath=path;
	}
	
	public String getServiceImplPath()
	{
		return this.serviceImplPath;
	}
	
	public void setComponentPath(String path)
	{
		this.componentPath=path;
	}
	
	public String getComponentPath()
	{
		return this.componentPath;
	}
	
	public void setComponentImplPath(String path)
	{
		this.componentImplPath=path;
	}
	
	public String getComponentImplPath()
	{
		return this.componentImplPath;
	}
	
	public void setMybatisXmlPath(String path)
	{
		this.mybatisXmlPath=path;
	}
	
	public String getMybatisXmlPath()
	{
		return this.mybatisXmlPath;
	}
	
	public void setControllerPath(String path)
	{
		this.controllerPath=path;
	}
	
	public String getControllerPath()
	{
		return this.controllerPath;
	}
	public void setViewPath(String path)
	{
		this.viewPath=path;
	}
	
	public String getViewPath()
	{
		return this.viewPath;
	}
	
}
