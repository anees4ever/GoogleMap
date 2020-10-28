package com.anees4ever.googlemap.network;

import com.anees4ever.googlemap.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitApiClass {
    public static Retrofit getWith(String baseUrl) {
        OkHttpClient.Builder builder= new OkHttpClient().newBuilder();
//        if (BuildConfig.DEBUG) {
//            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//            interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
//            builder.addInterceptor(interceptor);
//            compile 'com.squareup.okhttp3:logging-interceptor:3.4.0'
//        }

        builder.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("Referer", "https://smebusinesssoftware.com").build();
            return chain.proceed(request);
        });

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(new OkHttpClient().newBuilder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static String[] extractHostAndUrl(String url) {
        String scheme= "";
        if(url.startsWith("https://")) {
            scheme= "https://";
            url = url.substring(8);
        } else if(url.startsWith("http://")) {
            scheme= "http://";
            url = url.substring(7);
        }
        String[] urlInfo= new String[2];
        if(url.contains("/")) {
            int pos= url.indexOf("/");
            urlInfo[0] = scheme + url.substring(0, pos + 1);
            urlInfo[1] = url.substring(pos + 1);
        } else {
            urlInfo[0]= scheme + url;
            urlInfo[1]= "";
        }
        return urlInfo;
    }
}
