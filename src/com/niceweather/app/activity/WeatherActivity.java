package com.niceweather.app.activity;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.niceweather.app.R;
import com.niceweather.app.util.HttpCallbackListener;
import com.niceweather.app.util.HttpUtil;
import com.niceweather.app.util.Utility;

public class WeatherActivity extends Activity {
private LinearLayout weatherInfoLayout;
//用于显示城市名
private TextView cityNameText;
//用于显示天气描述信息
private TextView publishText;
//用于显示天气描述消息
private TextView weatherDespText;
//用于显示气温1
private TextView temp1Text;
//用于显示气温2
private TextView temp2Text;
//用于显示当前日期
private TextView currentDateText;

@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//初始化各种控件
		weatherInfoLayout=(LinearLayout)findViewById(R.id.weather_info_layout);
		cityNameText=(TextView)findViewById(R.id.city_name);
	    publishText=(TextView)findViewById(R.id.publish_text);
	    weatherDespText=(TextView)findViewById(R.id.weather_desp);
	    temp1Text=(TextView)findViewById(R.id.temp1);
	    temp2Text=(TextView)findViewById(R.id.temp2);
	    currentDateText=(TextView) findViewById(R.id.current_date);
	    String countryCode=getIntent().getStringExtra("country_code");
	    if (!TextUtils.isEmpty(countryCode)) {
			//有县级代号时就去查询天气
	    	publishText.setText("同步中...");
	    	weatherInfoLayout.setVisibility(View.INVISIBLE);
	    	cityNameText.setVisibility(View.INVISIBLE);
	    	queryWeatherCode(countryCode);
		}else{
			//没有县级代号时就直接显示本地天气
			showWeather();
		}
	    
	}

//查询县级代号对应的天气代号
private void queryWeatherCode(String countryCode) {
	String address="http://www.weather.com.cn/data/list3/city"+countryCode+".xml";
	queryFromServer(address,"countryCode");
	
}
private void queryWeatherInfo(String weatherCode){
	String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
	queryFromServer(address, "weatherCode");
}
private void queryFromServer(final String address,final String type) {
HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
	
	@Override
	public void onFinish(final String response) {
		if ("countryCode".equals(type)) {
			if (!TextUtils.isEmpty(response)) {
				String [] array=response.split("\\|");
				if (array.length==2 && array!=null) {
					String weatherCode=array[1];
					queryWeatherInfo(weatherCode);
				}
			}
		}else if ("weatherCode".equals(type)) {
			//处理服务器返回的天气信息
			Utility.handleWeatherResponse(WeatherActivity.this, response);
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					showWeather();
				}
			});
		}
		
	}
	
	@Override
	public void onError(Exception e) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
               publishText.setText("同步失败");				
			}
		});
	}
});
}
//从sharedpreferences文件中读取储存的天气信息,并显示到界面上.
private void showWeather() {
	SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
	cityNameText.setText(prefs.getString("city_name", ""));
	temp1Text.setText(prefs.getString("temp1", ""));
	temp2Text.setText(prefs.getString("temp2", ""));
	weatherDespText.setText(prefs.getString("weather_desp", ""));
	currentDateText.setText(prefs.getString("current_date", ""));
	publishText.setText("今天"+prefs.getString("publish_time", "")+"发布");
	weatherInfoLayout.setVisibility(View.VISIBLE);
	cityNameText.setVisibility(View.VISIBLE);
	
}
}
