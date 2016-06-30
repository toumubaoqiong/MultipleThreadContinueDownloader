package com.vince.multiplethreadcontinuedownloader.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;


/**
 * 业务Bean，实现对数据的操作
 */
public class FileService {
	private DBOpenHelper openHelper;

	public FileService(Context context) {
		openHelper = new DBOpenHelper(context);
	}

	/**
	 * 获取特定URI的每条线程已经下载的文件长度
	 * @param path
	 * @return
	 */
	public Map<Integer, Integer> getData(String path){
		//获取可读的数据库句柄，一般情况下在该操作的内部实现中其返回的其实是可写的数据库句柄
		SQLiteDatabase db = openHelper.getReadableDatabase();
		//根据下载路径查询所有线程下载数据，返回的Cursor指向第一条记录之前
		Cursor cursor = db.rawQuery("select threadid, downlength from filedownlog where downpath=?", new String[]{path});
		//建立一个哈希表用于存放每条线程的已经下载的文件长度
		Map<Integer, Integer> data = new HashMap<Integer, Integer>();
		while(cursor.moveToNext()){
			data.put(cursor.getInt(0), cursor.getInt(1));
			data.put(cursor.getInt(cursor.getColumnIndexOrThrow("threadid")), cursor.getInt(cursor.getColumnIndexOrThrow("downlength")));
		}
		cursor.close();
		db.close();
		return data;
	}

	/**
	 * 保存每条线程已经下载的文件长度
	 * @param path	下载的路径
	 * @param map 现在的id和已经下载的长度的集合
	 */
	public void save(String path,  Map<Integer, Integer> map){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.beginTransaction();
		try{
			for(Map.Entry<Integer, Integer> entry : map.entrySet()){
				db.execSQL("insert into filedownlog(downpath, threadid, downlength) values(?,?,?)",
						new Object[]{path, entry.getKey(), entry.getValue()});
			}
			db.setTransactionSuccessful();
		}finally{
			db.endTransaction();
		}
		db.close();
	}

	/**
	 * 实时更新每条线程已经下载的文件长度
	 * @param path
	 * @param map
	 */
	public void update(String path, int threadId, int pos){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL("update filedownlog set downlength=? where downpath=? and threadid=?",
				new Object[]{pos, path, threadId});
		db.close();
	}

	/**
	 * 当文件下载完成后，删除对应的下载记录
	 * @param path
	 */
	public void delete(String path){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL("delete from filedownlog where downpath=?", new Object[]{path});
		db.close();
	}
}
