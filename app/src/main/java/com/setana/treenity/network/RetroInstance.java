package com.setana.treenity.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroInstance {

    public static String Base_URL = "https://treedataapi-default-rtdb.firebaseio.com/"; // tree_data.json"

    private static Retrofit retrofit;

    public static Retrofit getRetroClient() { // retrofit instance가 없다면 만들어주고 있다면 retro 객체 반환

        if(retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Base_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
