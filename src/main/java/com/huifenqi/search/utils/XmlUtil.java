package com.huifenqi.search.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huifenqi.search.configuration.SearchConfiguration;


/**
 * xml工具包<br>
 *  majianchun
 * 
 */
public class XmlUtil {
	private static final Logger log = LoggerFactory.getLogger(XmlUtil.class);
		
	/* 单例模式 */
	private static XmlUtil dom4J;

	private static String filePath;

	private static SAXReader saxReader;

	private XmlUtil() {
	}
	/**
	 * 单例模式，获得实例
	 */
	public synchronized static XmlUtil getInstance() {
		if (dom4J == null) {
			dom4J = new XmlUtil();
		}

		if (filePath == null) {
			try {
				//filePath = XmlUtil.class.getClassLoader().getResource("./base.xml").getPath();
				filePath = "/data/www/search-service/config/base.xml";  
			} catch (Exception e) {
				log.error("加载配置文件base.xml错误!!!", e);
			}
		}
		return dom4J;
	}

	/**
	 * load 载入一个xml文档
	 * 
	 * @param path
	 *            文件路径
	 * 
	 * @return 成功返回Document对象，失败返回null
	 */
	public synchronized Document load(String path) {
		Document document = null;
		try {
			if (saxReader == null) {
				saxReader = new SAXReader();
			}
			document = saxReader.read(new File(path));
		} catch (Exception ex) {
			log.error("载入base.xml文档失败", ex);
			ex.printStackTrace();
		}
		return document;
	}

	/**
	 * 将Document对象保存为一个xml文件到本地
	 * 
	 * 
	 * @param doc
	 *            需要保存的document对象
	 * @return true:保存成功 flase:失败
	 */
	public synchronized boolean xmlWriter(Document doc) {
		boolean isSuccess = true;
		XMLWriter writer = null;// 声明写XML的对象
		OutputFormat format = OutputFormat.createPrettyPrint();// 定义文档的格式为美化型(pretty)
		format.setEncoding("UTF-8");// 格式编码为“UTF-8”
		try {
			writer = new XMLWriter(new FileOutputStream(new File(filePath)), format);// 声明向“d:\”下写入student.xml文档的对象
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			writer.write(doc);// 写XML文档
		} catch (IOException e) {
			isSuccess = false;
			log.error("保存base.xml文档失败", e);
			e.printStackTrace();
		}

		try {
			writer.close();// 关闭输出流
		} catch (IOException e) {
			isSuccess = false;
			e.printStackTrace();
		}
		return isSuccess;
	}

	/**
	 * 更新一个属性<br>
	 * 
	 * @param key
	 *            索引文件属性<br>
	 * @param value
	 *            索引文件属性值<br>
	 * @return 成功返回true,失败返回false<br>
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean updateField(String key, String value) {
		Document doc = load(filePath);
		List<Element> list = doc.selectNodes("/lucene/field/item[@key='" + key + "']");
		Iterator<Element> it = list.iterator();
		while (it.hasNext()) {
			Element attribute = (Element) it.next();
			attribute.setText(value);
		}
		xmlWriter(doc);
		return true;
	}

	/**
	 * 读取属性的方法<br>
	 * 
	 * @param key
	 *            属性名称<br>
	 * @return 返回属性值<br>
	 * @throws DocumentException
	 */
	@SuppressWarnings("unchecked")
	public synchronized String query(String key) {
		Document doc = load(filePath);
		List<Element> list = doc.selectNodes("/lucene/field/item[@key='" + key + "']");
		if(null == list || list.size() == 0){
			return "";
		}
		Element field = (Element) list.get(0);
		return field.getText();
	}

	/**
	 * DOM4J读写XML示例
	 * 
	 * @param args
	 * @throws DocumentException
	 * @throws IOException
	 */
//	public static void main(String[] args) {
//		XmlUtil test = XmlUtil.getInstance();
//		System.out.println(test.query("audit.flag"));
//		test.updateField("audit.flag", "1");
//
//	}
}
