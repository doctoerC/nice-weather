package com.niceweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.niceweather.app.R;
import com.niceweather.app.model.City;
import com.niceweather.app.model.Country;
import com.niceweather.app.model.NiceWeatherDB;
import com.niceweather.app.model.Province;
import com.niceweather.app.util.HttpCallbackListener;
import com.niceweather.app.util.HttpUtil;
import com.niceweather.app.util.Utility;

public class ChooseAreaActivity extends Activity {
public static final int LEVEL_PROVINCE=0;
public static final int LEVEL_CITY=1;
public static final int LEVEL_COUNTRY=2;

private ProgressDialog progressDialog;
private TextView titleText;
private ListView listView;
private ArrayAdapter<String> adapter;
private NiceWeatherDB niceWeatherDB;
private List<String> dataList=new ArrayList<String>();

//省列表
private List<Province> provinceList;
//市列表
private List<City> cityList;
//县列表
private List<Country> countryList;
//选中的省份
private Province selectedProvince;
private City selectedCity;
private Country selectedCountry;
//当前选中的级别
private int currentLevel;
//是否从weatherActivity中跳转过来
private boolean isFromWeatherActivity;

@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		//已经选择了城市且不是从WeatherActivity跳转过来,才会直接跳转WeatherActivity
		if (prefs.getBoolean("city_selected",false) && !isFromWeatherActivity) {
			Intent intent=new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView=(ListView)findViewById(R.id.list_view);
		titleText=(TextView)findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				dataList);
		listView.setAdapter(adapter);
		niceWeatherDB=NiceWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel==LEVEL_PROVINCE) {
					selectedProvince=provinceList.get(position);
					queryCities();
				}else if(currentLevel==LEVEL_CITY){
					selectedCity=cityList.get(position);
					queryCountries();
				}else if (currentLevel==LEVEL_COUNTRY) {
					String countryCode=countryList.get(position).getCountryCode();
					Intent intent=new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("country_code", countryCode);
					startActivity(intent);
					finish();
				}
			}
		});
	
		queryProvinces();
	}
//查询所有的省,优先从数据库中查询,如果没又查询到再去服务器
private void queryProvinces(){
	provinceList=niceWeatherDB.loadProvinces();
	if (provinceList.size()>0) {
		dataList.clear();
		for(Province province:provinceList){
			dataList.add(province.getProvinceName());
		}
		adapter.notifyDataSetChanged();
		listView.setSelection(0);
		titleText.setText("中国");
		currentLevel=LEVEL_PROVINCE;
	}else{
		queryFromServer(null,"province");
	}
}
//查询选中省内所有的市,优先从数据库中查询,如果没有再去服务器上查询
private void queryCities(){
	cityList=niceWeatherDB.loadCities(selectedProvince.getId());
	if (cityList.size()>0) {
		dataList.clear();
		for(City city:cityList){
			dataList.add(city.getCityName());
		}
		adapter.notifyDataSetChanged();
		listView.setSelection(0);
		titleText.setText(selectedProvince.getProvinceName());
		currentLevel=LEVEL_CITY;
	}else{
		queryFromServer(selectedProvince.getProvinceCode(),"city");
	}
}
//查询选中市中所有的县
private void queryCountries(){
	countryList=niceWeatherDB.loadCountries(selectedCity.getId());
	if (countryList.size()>0) {
		dataList.clear();
		for(Country country:countryList){
			dataList.add(country.getCountryName());
		}
		adapter.notifyDataSetChanged();
		listView.setSelection(0);
		titleText.setText(selectedCity.getCityName());
		currentLevel=LEVEL_COUNTRY;
	}else{
		queryFromServer(selectedCity.getCityCode(),"country");
	}
}
//根据传入的代号和类型从服务器上查询省市县的数据
private void queryFromServer(final String code,final String type){
	String address;
	if (!TextUtils.isEmpty(code)) {
		address="http://www.weather.com.cn/data/list3/city"+code+".xml";
	}else{
		address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean result=false;
				if ("province".equals(type)) {
					result=Utility.handleProvincesResponse(niceWeatherDB, response);
					
				}else if ("city".equals(type)) {
					result=Utility.handleCitiesResponse(niceWeatherDB, response, selectedProvince.getId());
				}else if ("country".equals(type)) {
					result=Utility.handleCountrisResponse(niceWeatherDB, response, selectedCity.getId());
				}
				if (result) {
					//通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
								
							}else if ("city".equals(type)) {
								queryCities();
							}else if ("country".equals(type)) {
								queryCountries();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				//通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this,"加载失败" , Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
				
	}

//显示进度对话框
private void showProgressDialog(){
	if (progressDialog==null) {
		progressDialog=new ProgressDialog(this);
		progressDialog.setMessage("正在加载..");
		progressDialog.setCancelable(false);
	}
	progressDialog.show();
}
private void closeProgressDialog(){
	if (progressDialog!=null) {
		
		progressDialog.dismiss();
	}
}
//捕获back按键,根据当前的级别来判断,此时应该返回市列表,省列表,还是县列表,还是直接退出
@Override
	public void onBackPressed() {
	if (currentLevel==LEVEL_COUNTRY) {
		queryCities();
	}else if (currentLevel==LEVEL_CITY) {
		queryProvinces();
		
	}else {
		if (isFromWeatherActivity) {
			Intent intent=new Intent(this, WeatherActivity.class);
			startActivity(intent);
		}
		finish();
	}
	}

}
