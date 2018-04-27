package com.huifenqi.search.query;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import com.huifenqi.search.utils.IndexReaderCloseUtil;
import com.huifenqi.search.utils.XmlUtil;

/**
 * 搜索对象工厂 majianchun
 * 
 */
public final class SearcherFactory {

	private static Log LOG = LogFactory.getLog(SearcherFactory.class);

	private static Map<String, IndexReader> readerMap;
	
	
	
	// 索引库LIST
    public static List<String> STORE_LIST = new ArrayList<String>();
    // 类型 Map
 	public static HashMap<String,String> TYPE = new HashMap<String,String>();	
 	
 	static{
 	         // T参数对应类型，同CMS完全对应,同索引对应
 			TYPE.put("house", "0");
 			TYPE.put("room", "1");
 			
 		     // 实际索引类型
 			STORE_LIST.add("house");
 			STORE_LIST.add("room");
 		
 	}
 	

	
	public static IndexSearcher getSearcher(String dataTypes) {
		
		Set<String> types = getTypes(dataTypes);
		IndexSearcher searcher = null;
		try {
			if (types.size() > 0){
				searcher = getSearcher(types);
			}
		} catch (Exception e) {
			LOG.error(dataTypes, e);
		}
		return searcher;
	}

	
	public static Set<String> getTypes(String dataType){
		Set<String> types = new HashSet<String>();
		String[] ts = dataType.split(",");
		for (String t :ts) {
			if (TYPE.keySet().contains(t)){
				types.add(t);
			}
		}
		return types;
	}
	
	/**
	 * 取得搜索器
	 * 
	 * @return
	 * @throws Exception 
	 */
	private static IndexSearcher getSearcher(Set<String> dataTypes) throws Exception {
		List<IndexReader> readerList = new ArrayList<IndexReader>();
		for (String dataType : dataTypes) {
			IndexReader reader = readerMap.get(dataType);
			readerList.add(reader);
		}
		IndexSearcher searcher = buildSearcher(readerList);;
		
		return searcher;
	}

	/**
	 * 装配搜索器
	 * 
	 * @param readerList
	 * @return
	 * @throws Exception
	 */
	private static IndexSearcher buildSearcher(List<IndexReader> readerList) {
		IndexReader reader=null;
		if (readerList.size() == 1) {
			reader = readerList.get(0);
		} else {
			for (int i = 0; i < readerList.size(); i++) {
				if (readerList.get(i) == null)
					readerList.remove(i);
			}
			try {
				reader = new MultiReader(readerList.toArray(new IndexReader[readerList.size()]));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new IndexSearcher(reader);
	}

	/**
	 * 初始化
	 */
	public static void init() {
		System.setProperty("org.apache.lucene.FSDirectory.class", "org.apache.lucene.store.NIOFSDirectory");
		readerMap = new HashMap<String, IndexReader>();
		for (String store : STORE_LIST) {
			try {
				String flag = XmlUtil.getInstance().query(store + ".flag");
				String path = XmlUtil.getInstance().query(store + "." + flag);
				LOG.info("store:{} path:{}"+store+" "+path);
				if (StringUtils.isBlank(path)) {
					LOG.error("{} index path is null"+store);
					continue;
				}
				IndexReader reader = buildReader(path);
				if (reader != null) {
					readerMap.put(store, reader);
				}
			} catch (Exception e) {
				LOG.error("", e);
			}

		}
	}

	/**
	 * 初始化搜索器
	 * 
	 * @param path
	 * @return
	 */
	private static IndexReader buildReader(String path) {
	
		try {
			return DirectoryReader.open(FSDirectory.open(Paths.get(path)));
	
		}
		catch (Exception e) {
			LOG.error("", e);
		}
		return null;
	}

	/**
	 * 获取索引路径
	 * 
	 * @return
	 */
	public static String getIndexPath(String store) {
		String flag = XmlUtil.getInstance().query(store + ".flag");
		LOG.info(flag);
		flag = flag.equals("one") ? "two" : "one";
		String path = XmlUtil.getInstance().query(store + "." + flag);

		return path;
	}

	/**
	 * 索引切换
	 */
	public static boolean changeIndexPath(String store) {
		try {
			String flag = XmlUtil.getInstance().query(store.concat(".flag")).equals("one") ? "two" : "one";
			String path = XmlUtil.getInstance().query(store + "." + flag);
			IndexReader reader = buildReader(path);

			synchronized (readerMap) {
				if (reader != null) {
					IndexReaderCloseUtil.closeReader(readerMap.get(store));
					readerMap.remove(store);
					readerMap.put(store, reader);
					changeIndexFlag(store);
					LOG.info("切换" + store + " searcher成功！");
					return true;
				} else {
					LOG.error("切换" + store + " searcher失败！");
					return false;
				}
			}
		} catch (Exception e) {
			LOG.warn("切换" + store + " searcher失败！", e);
			return false;
		}
	}
	
	
	//当监控发现索引错误时，调用修复索引
	public static boolean repairIndex(String store) {
		try {
			//切换到另一份索引
			String flag = XmlUtil.getInstance().query(store.concat(".flag")).equals("one") ? "two" : "one";
			String path = XmlUtil.getInstance().query(store + "." + flag);
			IndexReader reader = buildReader(path);
			//如果另一份索引不正常，重新加载现在的索引
			if (reader == null) {
				changeIndexFlag(store);
				flag = flag.equals("one") ? "two" : "one";
				path = XmlUtil.getInstance().query(store + "." + flag);
				reader = buildReader(path);
			} 
			if (reader != null) {
				synchronized (readerMap) {
					IndexReaderCloseUtil.closeReader(readerMap.get(store));
					readerMap.remove(store);
					readerMap.put(store, reader);
					changeIndexFlag(store);
					LOG.info("切换" + store + " searcher成功！");
					return true;
				}
			} 
		} catch (Exception e) {
			LOG.error("", e);
		}
		return false;
	}
	
	public static boolean isRightIndex(String store) {
		IndexSearcher searcher = null;
		try {
			searcher = new IndexSearcher(readerMap.get(store));
			Query query = new MatchAllDocsQuery();
			TopDocs hits = searcher.search(query, 1);
			return hits.scoreDocs.length > 0 ? true : false;
		} catch (Exception e) {
			LOG.error("index format error：" + store, e);
		}
		return false;
	}
	/**
	 * 索引标识切换
	 * 
	 * @param store
	 */
	public static void changeIndexFlag(String store) {
		String flag = XmlUtil.getInstance().query(store.concat(".flag"));
		XmlUtil.getInstance().updateField(store.concat(".flag"), flag.equals("one") ? "two" : "one");
	}

}
