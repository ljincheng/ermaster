package org.insightech.er.editor.model.settings;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.diagram_contents.element.node.table.ERTable;
import org.insightech.er.editor.model.diagram_contents.element.node.table.column.NormalColumn;
import org.insightech.er.editor.model.diagram_contents.not_element.dictionary.Word;

/**
 *  physicalName替换为描述信息（在描述信息不为空的情况下）
 * @author ljc
 *
 */
public class LogicalName2Describe {
	private    String templateResult(String template) 
	{
		try{
			if(template==null)
			{
				return "";
			}
				String[] temps=new String[]{"\\((.+?)\\)","（(.+?)）","\\[(.+?)\\]","：(.*)",":(.*)"};
				String result=new String(template);
				for(String regextemp:temps)
				{
			        Pattern pattern = Pattern.compile(regextemp);
			        Matcher matcher = pattern.matcher(result);
			        StringBuffer sb = new StringBuffer();
			        while (matcher.find()) {
			            String name = matcher.group(1);//键名
			            matcher.appendReplacement(sb, "");
			        }
			        matcher.appendTail(sb);
			        result=sb.toString();
				}
		        return result;
		}catch(Exception e)
		{
			return "";
		}
    }
	

	public void replaceLogicalName(ERDiagram diagram)
	{
		List<ERTable> tablelist= diagram.getDiagramContents().getContents()
		.getTableSet().getList();
		 if(tablelist!=null)
		 {
			 for(ERTable table:tablelist)
			 {
				if(table!=null)
				{
					for(NormalColumn column: table.getExpandedColumns())
					{
						String desc=column.getDescription();
						 if(desc!=null && desc.length()>0)
						 {
							 Word word=column.getWord();
							 
							 word.setLogicalName(templateResult(desc));
						 }
					}
//					String tabDesc=table.getDescription();
//					if(tabDesc!=null && tabDesc.trim().length()>0)
//					{
//						table.setLogicalName(table.getDescription());
//					}
				}
			 }
		 }
	}

}
