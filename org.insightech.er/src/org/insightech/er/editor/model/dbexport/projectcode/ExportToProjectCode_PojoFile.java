package org.insightech.er.editor.model.dbexport.projectcode;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;

import org.insightech.er.ResourceString;
import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.diagram_contents.element.node.NodeElement;
import org.insightech.er.editor.model.diagram_contents.element.node.table.ERTable;
import org.insightech.er.editor.model.diagram_contents.element.node.table.TableView;
import org.insightech.er.editor.model.diagram_contents.element.node.table.column.NormalColumn;
import org.insightech.er.editor.model.settings.export.ExportJavaSetting;
import org.insightech.er.util.Check;

/**
 * Pojo文件
 * @author ljc
 *
 */
public class ExportToProjectCode_PojoFile extends ProjectCodeTemplate{

	private static final String TEMPLATE;

	private static final String IMPLEMENTS;

	private static final String PROPERTIES;

	private static final String SET_PROPERTIES;

	private static final String SETTER_GETTER;

	private static final String SETTER_GETTER_ADDER;

	private static final String HASHCODE_EQUALS;

	private static final String HASHCODE_LOGIC;

	private static final String EQUALS_LOGIC;

	private static final String EXTENDS;
	private static final String TOSTRING;
	
	static {
		try {
			TEMPLATE = ProjectCodeCommon.loadResource("template");
			IMPLEMENTS = ProjectCodeCommon.loadResource("@implements");
			PROPERTIES = ProjectCodeCommon.loadResource("@properties");
			SET_PROPERTIES = ProjectCodeCommon.loadResource("@set_properties");
			SETTER_GETTER = ProjectCodeCommon.loadResource("@setter_getter");
			SETTER_GETTER_ADDER = ProjectCodeCommon.loadResource("@setter_getter_adder");
			HASHCODE_EQUALS = ProjectCodeCommon.loadResource("@hashCode_equals");
			HASHCODE_LOGIC = ProjectCodeCommon.loadResource("@hashCode logic");
			EQUALS_LOGIC = ProjectCodeCommon.loadResource("@equals logic");
			EXTENDS = ProjectCodeCommon.loadResource("@extends");
			TOSTRING = ProjectCodeCommon.loadResource("@toString");

		  

		} catch (IOException e) {
			e.printStackTrace();
			throw new ExceptionInInitializerError(e);
		}
	}
	public ExportToProjectCode_PojoFile(ProjectCodeSetting setting, ExportJavaSetting exportJavaSetting, ERDiagram diagram) {
		super(setting,exportJavaSetting, diagram);
	}

	@Override
	public String generateContent(ERTable table) throws IOException {
		this.importClasseNames.clear();
		this.importClasseNames.add("java.io.Serializable");
		this.sets.clear();

		String content = TEMPLATE;
		content = content.replace("@implements", IMPLEMENTS);

		content = this.replacePropertiesInfo(content, table);
		content = this.replaceHashCodeEqualsInfo(content, table);

		String classDescription = ResourceString.getResourceString("java.template.class.description").replaceAll(
				"@LogicalTableName",Matcher.quoteReplacement(table.getLogicalName())).replaceAll("@tableDescription", table.getDescription());

		content = this.replaceClassInfo(content, classDescription,this.getClassName(table),this.exportJavaSetting.getClassNameSuffix());
		content = this.replaceExtendInfo(content);
		content = this.replaceImportInfo(content);
		content = this.replaceConstructorInfo(content);

		return content;
	}
	
	@Override
	public String getOutPath(ERTable table) {
		// TODO Auto-generated method stub
		String folderPath=parsePackageToFileFolderPath(this.getNamespace(),File.separator);
		return folderPath+this.getClassName(table)+this.exportJavaSetting.getClassNameSuffix()+".java";
	}
	
	private String replaceExtendInfo(String content) throws IOException {
		if (Check.isEmpty(this.exportJavaSetting.getExtendsClass())) {
			content = content.replaceAll("@import extends\r\n", "");
			content = content.replaceAll("@extends ", "");

		} else {
			this.importClasseNames.add(this.exportJavaSetting.getExtendsClass());

			content = content.replaceAll("@extends", Matcher.quoteReplacement(EXTENDS));

			int index = this.exportJavaSetting.getExtendsClass().lastIndexOf(
					".");

			String extendsClassWithoutPackage = null;

			if (index == -1) {
				extendsClassWithoutPackage = this.exportJavaSetting
						.getExtendsClass();

			} else {
				extendsClassWithoutPackage = this.exportJavaSetting
						.getExtendsClass().substring(index + 1);
			}

			content = content.replaceAll("@extendsClassWithoutPackage",
					extendsClassWithoutPackage);
			content = content.replaceAll("@extendsClass",
					this.exportJavaSetting.getExtendsClass());
		}

		return content;
	}
	private String replacePropertiesInfo(String content, ERTable table) throws IOException {
		return replacePropertiesInfo(content, table,
				table.getExpandedColumns(), table.getReferringElementList());
	}
	
	private String replacePropertiesInfo(String content, ERTable table,
			List<NormalColumn> columns, List<NodeElement> referringElementList) throws IOException {
		StringBuilder properties = new StringBuilder();
		StringBuilder setterGetters = new StringBuilder();
		StringBuilder toString=new StringBuilder();

		 

		for (NormalColumn normalColumn : columns) {
//			if ( !normalColumn.isPrimaryKey() || normalColumn.isForeignKey()) {
				this.addContent(properties, PROPERTIES, normalColumn);
				this.addContent(setterGetters, SETTER_GETTER, normalColumn);
				this.addContent(toString, TOSTRING, normalColumn);
//			}
		}

		if (referringElementList != null) {
			for (NodeElement referringElement : referringElementList) {
				if (referringElement instanceof TableView) {
					TableView tableView = (TableView) referringElement;

					this.addContent(properties, SET_PROPERTIES, tableView);
					this.addContent(setterGetters, SETTER_GETTER_ADDER,tableView);

					this.sets.add(tableView.getPhysicalName());
				}
			}
		}

		content = content.replaceAll("@properties\r\n",
				Matcher.quoteReplacement(properties.toString()));
		content = content.replaceAll("@setter_getter\r\n",
				Matcher.quoteReplacement(setterGetters.toString()));
		content = content.replaceAll("@toString",Matcher.quoteReplacement(toString.toString()));

		return content;
	}

	private void addCompositeIdContent(StringBuilder contents, String template,
			String compositeIdClassName, ERTable table) {

		String compositeIdPropertyName = compositeIdClassName.substring(0, 1)
				.toLowerCase() + compositeIdClassName.substring(1);

		String propertyDescription = ResourceString.getResourceString(
				"java.template.composite.id.property.description").replaceAll(
				"@LogicalTableName",
				Matcher.quoteReplacement(table.getLogicalName()));

		String value = template;

		value = value.replaceAll("@type", compositeIdClassName);
		value = value.replaceAll("@logicalColumnName", propertyDescription);

		value = value
				.replaceAll("@physicalColumnName", compositeIdPropertyName);
		value = value.replaceAll("@PhysicalColumnName", compositeIdClassName);

		contents.append(value);
		contents.append("\r\n");
	}

	private String replaceHashCodeEqualsInfo(String content, ERTable table) throws IOException {
	  if (table.getPrimaryKeySize() > 0) {
			StringBuilder hashCodes = new StringBuilder();
			StringBuilder equals = new StringBuilder();

			for (NormalColumn primaryKey : table.getPrimaryKeys()) {
				this.addContent(hashCodes, HASHCODE_LOGIC, primaryKey);
				this.addContent(equals, EQUALS_LOGIC, primaryKey);
			}

			String hashCodeEquals = HASHCODE_EQUALS;
			hashCodeEquals = hashCodeEquals.replaceAll("@hashCode logic\r\n",
					hashCodes.toString());
			hashCodeEquals = hashCodeEquals.replaceAll("@equals logic\r\n",
					equals.toString());

			content = content.replaceAll("@hashCode_equals\r\n",
					hashCodeEquals.toString());

		} else {
			content = content.replaceAll("@hashCode_equals\r\n", "");
		}

		return content;
	}
	
	private void addContent(StringBuilder contents, String template,NormalColumn normalColumn) {
		String value = null;
		if (normalColumn.isForeignKey()) {
			NormalColumn referencedColumn = normalColumn
					.getRootReferencedColumn();

			ERTable referencedTable = (ERTable) referencedColumn
					.getColumnHolder();
			String className = this.getClassName(referencedTable);

			value = template.replaceAll("@type",Matcher.quoteReplacement(className));
			value = value.replaceAll("@logicalColumnName",referencedTable.getName());

			String physicalName = normalColumn.getPhysicalName().toLowerCase();
			physicalName = physicalName.replaceAll(referencedColumn.getPhysicalName().toLowerCase(), "");
			if (physicalName.indexOf(referencedTable.getPhysicalName().toLowerCase()) == -1) {
				physicalName = physicalName + referencedTable.getPhysicalName();
			}

			value = value.replaceAll("@physicalColumnName",
					ProjectCodeCommon.getCamelCaseName(physicalName, false));
			value = value.replaceAll("@PhysicalColumnName",
					ProjectCodeCommon.getCamelCaseName(physicalName, true,false));

		} else {
			value = template.replaceAll("@type",this.getClassName(normalColumn.getType()));
			value = value.replaceAll("@logicalColumnName",normalColumn.getLogicalName());
			value = value.replaceAll("@physicalColumnName", ProjectCodeCommon.getCamelCaseName(normalColumn.getPhysicalName(), false));
			value = value.replaceAll("@PhysicalColumnName",ProjectCodeCommon.getCamelCaseName(normalColumn.getPhysicalName(), true,false));
		}

		contents.append(value);
		contents.append("\r\n");
	}
	
	private void addContent(StringBuilder contents, String template,
			TableView tableView) {

		String value = template;

		this.importClasseNames.add("java.util.Set");
		this.importClasseNames.add("java.util.HashSet");

		value = value.replaceAll("@setType", Matcher.quoteReplacement("Set<"+ProjectCodeCommon.getCamelCaseName(tableView.getPhysicalName(), true)
				+ ProjectCodeCommon.getCamelCaseName(this.exportJavaSetting.getClassNameSuffix(), true)+">"));
		value = value.replaceAll("@type",Matcher.quoteReplacement(ProjectCodeCommon.getCamelCaseName(tableView.getPhysicalName(), true)
						+ ProjectCodeCommon.getCamelCaseName(this.exportJavaSetting.getClassNameSuffix(),true)));
		value = value.replaceAll("@logicalColumnName",Matcher.quoteReplacement(tableView.getName()));
		value = value.replaceAll("@physicalColumnName",Matcher.quoteReplacement(ProjectCodeCommon.getCamelCaseName(tableView.getPhysicalName(), false)));
		value = value.replaceAll("@PhysicalColumnName",Matcher.quoteReplacement(ProjectCodeCommon.getCamelCaseName(tableView.getPhysicalName(), true)));
		contents.append(value);
		contents.append("\r\n");
	}

	@Override
	public String getNamespace()
	{
		StringBuilder sb=new StringBuilder(); 
		String packageName=this.exportJavaSetting.getPackageName();
		String beanPath=this.projectCodeSetting.getBeanPath();
		if(!Check.isEmptyTrim(packageName))
		{
			sb.append(packageName);
		}
		if(!Check.isEmptyTrim(beanPath))
		{
			if(sb.length()>0)
			{
				sb.append(".");
			}
			sb.append(beanPath);
		}
		return sb.toString();
	}

	public String getERTableClassName(ERTable table) {
		String className=this.getClassName(table)+this.exportJavaSetting.getClassNameSuffix();
		return className;
	}

	
}
