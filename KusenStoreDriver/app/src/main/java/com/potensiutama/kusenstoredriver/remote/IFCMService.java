package com.potensiutama.kusenstoredriver.remote;

import com.potensiutama.kusenstoredriver.model.FCMResponse;
import com.potensiutama.kusenstoredriver.model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Apply:application/json",
            "Authorization:key=AAAACGNgLs8:APA91bEzlDQuDdub75Abci98hhPrwQh41YBcjLQglBY82Cf-vfBRDnZpkiW1jvG9fIAiBLntD248YSbZbmqZTiHw0CJulk-dr0S_lOAkWE88M7PqgZK7I2f5EZvlyLELCvrSO5BOUE4N"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);

}
