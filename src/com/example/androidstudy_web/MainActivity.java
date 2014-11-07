package com.example.androidstudy_web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.file.ImageDownloadManager;
import com.example.file.FileShowManager;
import com.example.file.RequestShowImageOnline;
import com.example.other.FavAndHisManager;
import com.example.other.ItemLongClickedPopWindow;
import com.example.other.ToolsPopWindow;

public class MainActivity extends Activity {
	
	//LOG标签
	private static final String DEG_TAG = "webBrowser";
	
	/**
	 * 请求码（默认，代表不做处理）
	 * @param	REQUEST_DEFAULT
	 * {@value #REQUEST_DEFAULT}
	 * */
	public static final int REQUEST_DEFAULT = -1;
	/**
	 * 请求码（历史或者书签）
	 * @param	REQUEST_OPEN_FAV_OR_HIS
	 * {@value #REQUEST_OPEN_FAV_OR_HIS}
	 * */
	public static final int REQUEST_OPEN_FAV_OR_HIS = 0;
	/**
	 * 请求码（保存图片路径）
	 * @param	REQUEST_SAVE_IMAGE_PATH
	 * {@value #REQUEST_SAVE_IMAGE_PATH}
	 * */
	public static final int REQUEST_SAVE_IMAGE_PATH = 1;

	//webView相关
	private WebView webHolder;
	private WebSettings settings;
	private WebViewClient viewClient;
	private WebChromeClient chromeClient;
	
	//地址栏编辑框
	private EditText webUrlStr;
	private TextView webUrlTitle;
	
	//搜索栏按钮
	private Button webUrlGoto;
	private Button webUrlCancel;
	private Button webUrlFavorites;
	//工具栏按钮组
	private Button preButton;
	private Button nextButton;
	private Button toolsButton;
	private Button windowsButton;
	private Button homeButton;
	
	//工具栏按钮显示界面
	private ToolsPopWindow toolsPopWindow;
	
	//弹出式菜单
	private ItemLongClickedPopWindow itemLongClickedPopWindow; 
	
	//地址栏布局管理器
	private LinearLayout webUrlLayoutInput;
	private LinearLayout webUrlLayoutShow;
	//收藏历史界面
	//private RelativeLayout favoritesAndHistory;
	
	//地址栏显示标签
	private boolean inputShow = false;
	
	//URL地址
	private String url = "";
	
	//URL网页标题
	private String title = "";
	
	//事件监听器
	private ButtonClickedListener buttonClickedListener;
	private WebUrlStrChangedListener urlStrChangedListener;
	private WebViewTouchListener webViewTouchListener;
	private OnOutOfFocusLinstener onOutOfFocusLinstener;
	private WebViewLongClickedListener webViewLongClickedListener;
	private PopWindowMenu popWindowMenu;
	
	//进度条
	private ProgressBar webProgressBar;
	
	//手势
	private GestureDetector mGestureDetector;
	private GestureListener gestureListener;
	
	//书签管理
	private FavAndHisManager favAndHisManager;
	
	//保存图片弹出对话框
	private Dialog saveImageToChoosePath;
	
	//保存图片按钮
	private TextView choosePath;
	private TextView imgSaveName;
	
	//计时
	private static boolean isExit = false;
	private static Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			isExit = false;
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_main);
		
		//组件初始化
		this.webHolder = (WebView) this.findViewById(R.id.web_holder);
		this.settings = this.webHolder.getSettings();
		this.viewClient = new OwnerWebView();
		this.chromeClient = new OwnerChromeClient();
		
		/*
		 * 界面中定义两个LinearLayout布局，一个是点击url编辑框输入url时候的布局，
		 * 另一个是正常载入url时候的布局
		 * */
		this.webUrlStr = (EditText) this.findViewById(R.id.web_url_input);
		this.webUrlTitle = (TextView) this.findViewById(R.id.web_url_show_title);		
		this.webUrlGoto = (Button) this.findViewById(R.id.web_url_goto);
		this.webUrlCancel = (Button) this.findViewById(R.id.web_url_cancel);
		
		/*
		 * 这里是底部五个按钮的布局
		 * */
		this.webUrlFavorites = (Button) this.findViewById(R.id.web_url_show_favorite);
		this.preButton = (Button) this.findViewById(R.id.pre_button);
		this.nextButton = (Button) this.findViewById(R.id.next_button);
		this.toolsButton = (Button) this.findViewById(R.id.tools_button);
		this.windowsButton = (Button) this.findViewById(R.id.window_button);
		this.homeButton = (Button) this.findViewById(R.id.home_button);
		
		//ToolsPopWindow是继承PopupWindow的一个类
		this.toolsPopWindow = new ToolsPopWindow(this, this.getWindowManager().getDefaultDisplay().getWidth()-30, 
				this.getWindowManager().getDefaultDisplay().getHeight()/3);
		
		//下面这个是处于输入状态时候标题栏的布局
		this.webUrlLayoutInput = (LinearLayout) this.findViewById(R.id.web_url_layout);
		//下面这个是处于正常载入url时候标题栏的布局
		this.webUrlLayoutShow = (LinearLayout) this.findViewById(R.id.web_url_layout_normal);

		//进度条
		this.webProgressBar = (ProgressBar) this.findViewById(R.id.web_progress_bar);
		
		//几个监听器
		this.buttonClickedListener = new ButtonClickedListener();
		this.urlStrChangedListener = new WebUrlStrChangedListener();
		this.webViewTouchListener = new WebViewTouchListener();
		this.webViewLongClickedListener = new WebViewLongClickedListener();
		
		//手势监听
		this.gestureListener = new GestureListener();
		this.mGestureDetector = new GestureDetector(this, gestureListener);
		
		this.favAndHisManager = new FavAndHisManager(getApplicationContext());
		
		//设置参数
		this.settings.setDefaultTextEncodingName("UTF-8");
		this.settings.setJavaScriptEnabled(true);
		
		
		/*
		 * WebViewClient就是帮助WebView处理各种通知、请求事件的，
		 * */
		this.webHolder.setWebViewClient(this.viewClient);
		/*
		 * setWebChromeClient主要处理解析，渲染网页等浏览器做的事情
		 * WebChromeClient是辅助WebView处理Javascript的对话框，网站图标，网站title，加载进度等 
		 * */
		this.webHolder.setWebChromeClient(this.chromeClient);
		this.preButton.setEnabled(false);
		this.nextButton.setEnabled(false);
		
		this.webProgressBar.setVisibility(View.GONE);
		
		//添加监听
		this.webUrlStr.setOnFocusChangeListener(this.onOutOfFocusLinstener);
		this.webUrlStr.addTextChangedListener(this.urlStrChangedListener);
		
		this.webHolder.setOnTouchListener(this.webViewTouchListener);
		this.webHolder.setOnLongClickListener(this.webViewLongClickedListener);
		this.webUrlTitle.setOnClickListener(this.buttonClickedListener);
		
		this.webUrlGoto.setOnClickListener(this.buttonClickedListener);
		this.webUrlCancel.setOnClickListener(this.buttonClickedListener);
		this.webUrlFavorites.setOnClickListener(this.buttonClickedListener);
		this.preButton.setOnClickListener(this.buttonClickedListener);
		this.nextButton.setOnClickListener(this.buttonClickedListener);
		this.toolsButton.setOnClickListener(this.buttonClickedListener);
		this.windowsButton.setOnClickListener(this.buttonClickedListener);
		this.homeButton.setOnClickListener(this.buttonClickedListener);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * WebViewClient自定义类
	 * 覆盖函数：
	 * 1.	shouldOverrideUrlLoading
	 * 2.	onReceivedError
	 * 3.	onPageFinished
	 * */
	private class OwnerWebView extends WebViewClient{

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			/*
			 * return true
			 * means the host application handles the url, while return false means the
			 * current WebView handles the url.			 * 
			 * */
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			view.getSettings().setJavaScriptEnabled(true);
			
			webUrlLayoutInput.setVisibility(View.GONE);
			webUrlLayoutShow.setVisibility(View.GONE);
			inputShow = false;
			webUrlStr.setText(url);
			changeStatueOfWebToolsButton();
			//添加历史
			String date = new SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(new Date()).toString();
			favAndHisManager.addHistory(title, url, Long.parseLong(date));
		}
		
	}
	
	/**
	 * WebChromeClient自定义继承类
	 * 覆盖如下方法
	 * 1.	onProgressChanged
	 * 		用来解决进度条显示问题
	 * */
	private class OwnerChromeClient extends WebChromeClient{

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
			//MainActivity.this.setProgress(newProgress * 100);
			if(newProgress==100){
				webProgressBar.setVisibility(View.GONE);
			}else{
				webProgressBar.setVisibility(View.VISIBLE);
				webProgressBar.setProgress(newProgress);
			}
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
			MainActivity.this.title = title;
			webUrlTitle.setText(title);
		}
		
	}
	
	
	/**
	 * OnClickListener自定义继承类
	 * 用来解决按钮监听
	 * */
	private class ButtonClickedListener implements OnClickListener
	{

		@Override
		public void onClick(View v)
		{
			if(v.getId() == R.id.web_url_goto)
			{
				Log.d(DEG_TAG,"url:" + url);
				webHolder.loadUrl(url);
			}
			else if(v.getId()==R.id.web_url_cancel)
			{
				webUrlStr.setText("");
			}
			else if(v.getId()==R.id.pre_button)
			{
				if(webHolder.canGoBack())
				{
					//后退
					webHolder.goBack();
				}
			}
			else if(v.getId()==R.id.next_button)
			{
				if(webHolder.canGoForward())
				{
					//前进
					webHolder.goForward();
				}
			}
			else if(v.getId()==R.id.tools_button)
			{
				//展现工具视图
				LayoutInflater toolsInflater = LayoutInflater.from(getApplicationContext());
				View toolsView = toolsInflater.inflate(R.layout.tabactivity_tools, null);
				//弹出PopupWindow
				toolsPopWindow.showAtLocation(toolsView, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, toolsButton.getHeight()+20);
				Button refresh = (Button) toolsPopWindow.getView(R.id.tools_normal_refresh);
				Button favorites = (Button) toolsPopWindow.getView(R.id.tools_normal_favorites);
				refresh.setOnClickListener(this);
				favorites.setOnClickListener(this);
			}
			else if(v.getId()==R.id.window_button)
			{
				//此处还未实现新建标签页的功能
			}
			else if(v.getId()==R.id.home_button)
			{
				webHolder.loadUrl("http://www.baidu.com");
			}
			else if(v.getId()==R.id.web_url_show_favorite)
			{
				//添加书签
				Log.d(DEG_TAG, "title:"+title+",url:"+url);
				favAndHisManager.addFavorite(title, url);
				favAndHisManager.getAllFavorites();
			}
			else if(v.getId()==R.id.web_url_show_title)
			{
				//点击标题栏，显示地址输入框架
				Log.d(DEG_TAG, "webtitle button down");
				changeStatueOfWebUrlLayout(true);
			}
			else if(v.getId()==R.id.tools_normal_refresh)
			{
				//刷新
				if(!(url.equals("")&&url.equals("http://")))
				{
					webHolder.loadUrl(url);
				}
			}
			else if(v.getId()==R.id.tools_normal_favorites)
			{
				toolsPopWindow.dismiss();
				startActivityForResult(new Intent(MainActivity.this, FavAndHisActivity.class),
						MainActivity.REQUEST_OPEN_FAV_OR_HIS);
			}
			else if(v.getId()==R.id.dialog_savePath_enter)
			{
				Intent imgSavePath = new Intent(MainActivity.this,FileActivity.class);
				imgSavePath.putExtra("savePath", choosePath.getText().toString());
				startActivityForResult(imgSavePath,MainActivity.REQUEST_SAVE_IMAGE_PATH);
			}
		}
		
	}
	
	/**
	 * OnClickListener自定义继承类
	 * 用来解决菜单功能处理问题
	 * */
	private class PopWindowMenu implements OnClickListener{
		
		private int type;
		private String value;
		
		public PopWindowMenu(int type, String value)
		{
			this.type = type;
			this.value = value;
			Log.d(DEG_TAG, "type:"+type+",value:"+value);
		}

		@Override
		public void onClick(View v) 
		{
			itemLongClickedPopWindow.dismiss();
			if(v.getId()==R.id.item_longclicked_viewImage)
			{
				//图片菜单-查看图片
				new RequestShowImageOnline(MainActivity.this).execute(value);
			}
			else if(v.getId()==R.id.item_longclicked_saveImage)
			{
				//图片菜单-保存图片
				View dialogSaveImg = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_saveimg, null);
				choosePath = (TextView) dialogSaveImg.findViewById(R.id.dialog_savePath_enter);
				imgSaveName = (TextView) dialogSaveImg.findViewById(R.id.dialog_fileName_input);
				final String imgName = value.substring(value.lastIndexOf("/") + 1);
				imgSaveName.setText(imgName);
				choosePath.setOnClickListener(buttonClickedListener);
				saveImageToChoosePath = new AlertDialog.Builder(MainActivity.this)
					.setTitle("选择保存路径")
					.setView(dialogSaveImg)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() 
					{
						
						@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							Log.d(DEG_TAG, "fileName:"+imgName+",filePath:"+choosePath.getText().toString());
							new ImageDownloadManager(MainActivity.this).execute(imgName, value, choosePath.getText().toString());
						}
					})
					.setNegativeButton("取消", null)
					.create();
				saveImageToChoosePath.show();
			}
			else if(v.getId()==R.id.item_longclicked_viewImageAttributes)
			{
				//图片菜单-查看属性
			}
		}
		
	}
	
	/**
	 * OnFocusChangeListener自定义继承类
	 * 覆盖方法如下：
	 * 1.	onFocusChange
	 * */
	private class OnOutOfFocusLinstener implements OnFocusChangeListener
	{

		@Override
		public void onFocusChange(View v, boolean hasFocus)
		{
			//地址输入失去焦点
			Log.d(DEG_TAG, "hasFocus:"+hasFocus);
			if(!webUrlStr.hasFocus())
			{
				Log.d(DEG_TAG, "地址栏输入:lose focus");
				changeStatueOfWebUrlLayout(false);				
			}
		}
		
	}
	
	/**
	 * TextWatcher自定义继承类
	 * 覆盖方法如下：
	 * 1.	afterTextChanged
	 * 2.	beforeTextChanged
	 * 3.	onTextChanged
	 * 		实现更改地址的时候进行地址合法性检测
	 * */
	private class WebUrlStrChangedListener implements TextWatcher{

		@Override
		public void afterTextChanged(Editable editable) {
		}

		@Override
		public void beforeTextChanged(CharSequence charsequence, int i, int j,
				int k) {
			
		}

		@Override
		public void onTextChanged(CharSequence charsequence, int i, int j, int k) 
		{
			url = charsequence.toString();
			if(!(url.startsWith("http://")||url.startsWith("https://")))
			{
				url = "http://"+url;
			}
			
			Log.d(DEG_TAG,"onchangeText:"+url);
			
			if(URLUtil.isNetworkUrl(url)&&URLUtil.isValidUrl(url))
			{
				changeStatueOfWebGoto(true);
			}
			else
			{
				changeStatueOfWebGoto(false);
			}
		}
		
	}
	
	/**
	 * OnTouchListener自定义继承类
	 * 解决将手势交给GestureDetector类解决
	 * */
	private class WebViewTouchListener implements OnTouchListener
	{

		@Override
		public boolean onTouch(View v, MotionEvent event) 
		{
			if(v.getId()==R.id.web_holder){
				//Log.i(DEG_TAG, "info :webViewTouched");
				//Log.i(DEG_TAG, "event:"+event.describeContents());
				return mGestureDetector.onTouchEvent(event);
			}
			return false;
		}
		
	}
	
	/**
	 * GestureDetector.OnGestureListener自定义继承类
	 * 解决各种手势的相对应策略
	 * 1.	向上滑动webView到顶触发事件，显示地址栏
	 * 2.	向下滑动webView触发时间，隐藏地址栏
	 * */
	private class GestureListener implements GestureDetector.OnGestureListener
	{

		@Override
		public boolean onDown(MotionEvent e) 
		{
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) 
		{
			if(webHolder.getScrollY()==0){
				//滑倒顶部
				//Log.d(DEG_TAG, "已经滑倒顶部");
				webUrlLayoutShow.setVisibility(View.VISIBLE);
				if(inputShow){
					webUrlLayoutInput.setVisibility(View.GONE);
					inputShow = false;
				}
			}
			
			if(webHolder.getScrollY()>0){
				//Log.d(DEG_TAG, "已经滑倒底部");
				/*Log.d(DEG_TAG, "contentHeight:"+webHolder.getContentHeight()
						+",hight:"+webHolder.getHeight()
						+",scale:"+webHolder.getScale()
						+",ScrollY:"+webHolder.getScrollY());*/
				
							
				
				webUrlLayoutShow.setVisibility(View.GONE);
				if(inputShow){
					webUrlLayoutInput.setVisibility(View.GONE);
					inputShow = false;
				}
			}
			
			if(e2.getY() - e1.getY() > 0)
			{
				/*
				 * 模仿UC风格，当浏览器不处于顶部并向下滑动时隐藏载入url的标题栏
				 * 而当用户向上滑动或者处于顶部则需要显示载入url的标题栏
				 * */	
				webUrlLayoutShow.setVisibility(View.VISIBLE);
			}
			return true;
		}
		
		@Override
		public void onLongPress(MotionEvent e) 
		{
			PointerXY.x = (int) e.getX();
			PointerXY.y = (int) e.getY();
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) 
		{
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) 
		{
			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e)
		{
			return false;
		}
		
	}
	
	private static class PointerXY
	{
		public static int x;
		public static int y;
		
		public static int getX() 
		{
			return x;
		}
		public static int getY() 
		{
			return y;
		}
		
	}
	
	private class WebViewLongClickedListener implements OnLongClickListener
	{

		@Override
		public boolean onLongClick(View v) 
		{
			HitTestResult result = ((WebView) v).getHitTestResult();
			if (null == result)
				return false;

			int type = result.getType();
			if (type == WebView.HitTestResult.UNKNOWN_TYPE)
				return false;

			if (type == WebView.HitTestResult.EDIT_TEXT_TYPE)
			{
				// let TextView handles context menu
				return true;
			}

			// Setup custom handling depending on the type
			switch (type) 
			{
				case WebView.HitTestResult.PHONE_TYPE:
					// 处理拨号
					break;
				case WebView.HitTestResult.EMAIL_TYPE:
					// 处理Email
					break;
				case WebView.HitTestResult.GEO_TYPE:
					// TODO
					break;
				case WebView.HitTestResult.SRC_ANCHOR_TYPE:
					// 超链接
					Log.d(DEG_TAG, "超链接");
					break;
				case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
				case WebView.HitTestResult.IMAGE_TYPE:
					// 处理长按图片的菜单项
					Log.d(DEG_TAG, "图片");
					itemLongClickedPopWindow = new ItemLongClickedPopWindow(MainActivity.this, ItemLongClickedPopWindow.IMAGE_VIEW_POPUPWINDOW, 200, 250);
					itemLongClickedPopWindow.showAtLocation(v, Gravity.TOP | Gravity.LEFT, PointerXY.getX(), PointerXY.getY()+10);
					TextView viewImage = (TextView) itemLongClickedPopWindow.getView(R.id.item_longclicked_viewImage);
					TextView saveImage = (TextView) itemLongClickedPopWindow.getView(R.id.item_longclicked_saveImage);
					TextView viewImageAttributes = (TextView) itemLongClickedPopWindow.getView(R.id.item_longclicked_viewImageAttributes);
					popWindowMenu = new PopWindowMenu(result.getType(), result.getExtra());
					viewImage.setOnClickListener(popWindowMenu);
					saveImage.setOnClickListener(popWindowMenu);
					viewImageAttributes.setOnClickListener(popWindowMenu);
					break;
				default:
					break;
			}
			return true;
		}

	}
	
	/**
	 * 更改进入
	 * */
	private void changeStatueOfWebGoto(boolean flag){
		if(flag){
			webUrlGoto.setVisibility(View.VISIBLE);
			webUrlCancel.setVisibility(View.GONE);
		}else{
			webUrlGoto.setVisibility(View.GONE);
			webUrlCancel.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * 设置工具栏回溯历史是否可用
	 * */
	private void changeStatueOfWebToolsButton(){
		if(webHolder.canGoBack()){
			//设置可使用状态
			preButton.setEnabled(true);
		}else{
			//设置禁止状态
			preButton.setEnabled(false);
		}
		if(webHolder.canGoForward()){
			//设置可使用状态
			nextButton.setEnabled(true);
		}else{
			//设置禁止状态
			nextButton.setEnabled(false);
		}
	}
	
	/**
	 * 设置工具栏转换
	 * */
	private void changeStatueOfWebUrlLayout(boolean show){
		Log.d(DEG_TAG,"工具栏转换");
		if(show){
			webUrlLayoutShow.setVisibility(View.GONE);
			webUrlLayoutInput.setVisibility(View.VISIBLE);
			inputShow = true;
			webUrlStr.requestFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            //显示软键盘
			if(!imm.isActive()){
				Log.d(DEG_TAG, "软键盘已经关闭");
			}else{
				Log.d(DEG_TAG, "软键盘开启");
			}
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
		}else{
			webUrlLayoutShow.setVisibility(View.VISIBLE);
			webUrlLayoutInput.setVisibility(View.GONE);
			inputShow = false;
		}
	}

	@Override
	public void onBackPressed() {
		//判断是否可后退，是则后退，否则按两次退出程序
		if(webHolder.canGoBack()){
			webHolder.goBack();
		}else{
			if(!isExit){
				isExit = true;
				Toast.makeText(getApplicationContext(), "再按一次退出程序",
	                    Toast.LENGTH_SHORT).show();
				handler.sendEmptyMessageDelayed(0,2000);
			}else{
				finish();
				System.exit(0);
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case REQUEST_DEFAULT:
			//不做任何处理
			break;
		case FavAndHisActivity.RESULT_FAV_HIS:
			webHolder.loadUrl(data.getStringExtra("url"));
			break;
		case FileActivity.RESULT_FILEMANAGER:
			choosePath.setText(data.getStringExtra("savePath"));
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}

