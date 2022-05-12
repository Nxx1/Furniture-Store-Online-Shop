package com.potensiutama.kusenstoreclient.SendNotificationPack;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA2MH7d2c:APA91bFOmI9Qi4bKMpko2W4M3CaDeRGaN8ODYqCYr2iBO48DrdWF_LAGF8P-AhGgg7vKuDi9qYOiA_oSzckNZi97GHSnqoxi9XNJ64wgRGIBQrh5GNKXT4wQay-PZ97Hj7XYrrkORpfT" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}

