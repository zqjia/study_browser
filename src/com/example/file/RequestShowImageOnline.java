package com.example.file;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.example.androidstudy_web.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class RequestShowImageOnline extends AsyncTask<String, String, Bitmap>
{
	
	private Dialog dialog;
	private Context context;
	
	public RequestShowImageOnline(Context context)
	{
		this.context = context;
	}

	/**
	 * 在子线程执行前进行调用，比如显示一个进度条
	 * */
	@Override
	protected void onPreExecute() 
	{
		super.onPreExecute();
		this.dialog = new AlertDialog.Builder(this.context)
			.setMessage("正在加载...")
			.create();
		this.dialog.show();
	}
	
	/**
	 * 执行子线程，内容一般为比较耗时的操作，例如下载等
	 * */
	@Override
	protected Bitmap doInBackground(String... params) 
	{
		Bitmap imgShow = null;
		try{
			URL  url = new URL(params[0]);
	        HttpURLConnection conn  = (HttpURLConnection)url.openConnection();
	        conn.setDoInput(true);
	        conn.connect(); 
	        InputStream inputStream=conn.getInputStream();
	        imgShow = BitmapFactory.decodeStream(inputStream); 
	        inputStream.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return imgShow;
	}
	
	/**
	 * 执行完后台子线程后，运行完结操作
	 * */
	@Override
	protected void onPostExecute(Bitmap result)
	{
		super.onPostExecute(result);
		View popupImgMenu = LayoutInflater.from(context).inflate(R.layout.activity_imgsview, null);
		ImageView showImg = (ImageView) popupImgMenu.findViewById(R.id.imgsview);
		showImg.setImageBitmap(result);
		this.dialog.dismiss();
		new AlertDialog.Builder(context).setView(popupImgMenu).create().show();
	}
}
