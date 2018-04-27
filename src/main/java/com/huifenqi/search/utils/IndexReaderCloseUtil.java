package com.huifenqi.search.utils;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
/**
 * @author majianchun
 *
 */
public class IndexReaderCloseUtil extends Thread {

	private IndexReader reader;

	private long closeWaitTime;

	public IndexReaderCloseUtil(IndexReader reader, long closeWaitTime) {
		this.reader = reader;
		this.closeWaitTime = closeWaitTime;
	}

	public void run() {
		if (reader != null) {
			try {
				sleep(closeWaitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					reader = null;
				}
			}
		}
	}

	/**
	 * 延迟关闭索引
	 * 
	 * @param dir
	 */
	public static void closeReader(IndexReader reader) {
		// 2分钟
		final long WAIT_TIME = 2 * 60 * 1000;
		new IndexReaderCloseUtil(reader, WAIT_TIME).start();
	}
}
