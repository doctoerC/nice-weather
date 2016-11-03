package com.niceweather.app.util;

import android.text.TextUtils;

import com.niceweather.app.model.City;
import com.niceweather.app.model.Country;
import com.niceweather.app.model.NiceWeatherDB;
import com.niceweather.app.model.Province;

public class Utility {
//解析和处理服务器返回的省级数据
	public synchronized static boolean handleProvincesResponse(NiceWeatherDB niceWeatherDB,
			String response){
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces=response.split(",");
			if (allProvinces!=null  && allProvinces.length>0) {
				for(String p:allProvinces){
					String [] array=p.split("\\|");//转意字符
					Province province=new Province();
					province.setProvinceName(array[1]);
					province.setProvinceCode(array[0]);
					//将解析出来的数据储存到Province表
					niceWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
//解析和处理服务器返回的市级数据
	public static boolean handleCitiesResponse(NiceWeatherDB niceWeatherDB,
			String response,int provinceId){
		if (!TextUtils.isEmpty(response)) {
			String []allCities=response.split(",");
			if (allCities!=null && allCities.length>0) {
				
				for(String c:allCities){
					String [] array=c.split("\\|");
					City city=new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					niceWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	//解析和处理返回的县级数据
	public static boolean handleCountrisResponse(NiceWeatherDB niceWeatherDB,
			String response,int cityId){
		if (!TextUtils.isEmpty(response)) {
			String [] allCountries=response.split(",");
			if (allCountries!=null && allCountries.length>0) {
				
				for(String c:allCountries){
					String [] array=c.split("\\|");
					Country country=new Country();
					country.setCityId(cityId);
					country.setCountryCode(array[0]);
					country.setCountryName(array[1]);
					niceWeatherDB.saveCountry(country);
				}
				return true;
			}
		}
		return false;
	}
}
