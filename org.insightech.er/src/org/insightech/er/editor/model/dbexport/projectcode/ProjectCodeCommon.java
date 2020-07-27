package org.insightech.er.editor.model.dbexport.projectcode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.logging.Logger;

import org.insightech.er.ResourceString;
import org.insightech.er.db.sqltype.SqlType;
import org.insightech.er.editor.controller.editpart.element.connection.AbstractERDiagramConnectionEditPart;
import org.insightech.er.editor.model.dbexport.java.ExportToJavaManager;
import org.insightech.er.editor.model.diagram_contents.element.node.table.ERTable;
import org.insightech.er.util.io.IOUtils;

/**
 * 
 * @author ljc
 *
 */
public class ProjectCodeCommon {
	
	private static Logger logger = Logger
			.getLogger(ProjectCodeCommon.class.getName());

	private static final String TEMPLATE_DIR = "projectcode/";
	private static final String[] KEYWORDS = { "java.template.constructor",
			"java.template.getter.description",
			"java.template.set.adder.description",
			"java.template.set.getter.description",
			"java.template.set.property.description",
			"java.template.set.setter.description",
			"java.template.setter.description", };
	
	public static  String getClassName(ERTable table) {
		return getCamelCaseName(table);
	}

	 
	public static String getFullClassName(SqlType type) {
		if (type == null) {
			return "";
		}
		Class clazz = type.getJavaClass();

		String name = clazz.getCanonicalName();

		return name;
	}
	
	public static  String getCamelCaseName(ERTable table) {
		return getCamelCaseName(table.getLogicalName(), true);
	}

	/**
	 * 驼峰名称
	 * @param name
	 * 名称
	 * @param capital
	 * 首字母是否大写的
	 * @param toUpperCase
	 * 是否转为小写
	 * @return
	 */
	public static String getCamelCaseName(String name,boolean capital,boolean toLowerCase)
	{
		String className = name.toLowerCase();
		if(!toLowerCase)
		{
			className=name;
		}

		if (capital && className.length() > 0) {
			String first = className.substring(0, 1);
			String other = className.substring(1);

			className = first.toUpperCase() + other;
		}else if(className.length()>0){
			String first = className.substring(0, 1);
			String other = className.substring(1);

			className = first.toLowerCase() + other;
		}

		while (className.indexOf("_") == 0) {
			className = className.substring(1);
		}

		int index = className.indexOf("_");

		while (index != -1) {
			String before = className.substring(0, index);
			if (className.length() == index + 1) {
				className = before;
				break;
			}

			String target = className.substring(index + 1, index + 2);

			String after = null;

			if (className.length() == index + 1) {
				after = "";

			} else {
				after = className.substring(index + 2);
			}

			className = before + target.toUpperCase() + after;

			index = className.indexOf("_");
		}

		return className;
	}
	
	/**
	 * 驼峰名称
	 * @param name
	 * 名称
	 * @param capital
	 * 首字母是否大写，true为首字母大写，否为不设置。
	 * @return
	 */
	public static String getCamelCaseName(String name, boolean capital) {
		return getCamelCaseName(name,capital,false);
	}

	/**
	 * 加载资源文件
	 * @param templateName
	 * @return
	 * @throws IOException
	 */
	public static String loadResource(String templateName) throws IOException {
		String resourceName = TEMPLATE_DIR+ templateName + ".txt";

		URL url =ProjectCodeCommon.class.getClassLoader().getResource(resourceName);
		//System.out.printf("=====test====\r\t==: path="+url.getPath());
		//logger.log( Level.INFO, "templateName="+templateName);
		//logger.log( Level.INFO, "=====test====\r\t==: path="+url.getPath());
		InputStream in = ProjectCodeCommon.class.getClassLoader()
				.getResourceAsStream(resourceName);

		if (in == null) {
			throw new FileNotFoundException(resourceName);
		}

		try {
			String content = IOUtils.toString(in);

			for (String keyword : KEYWORDS) {
				content = content.replaceAll(keyword, Matcher
						.quoteReplacement(ResourceString
								.getResourceString(keyword)));
			}

			return content;

		} finally {
			in.close();
		}
	}
}
