package com.potensiutama.kusenstoredriver.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.potensiutama.kusenstoredriver.R;
import com.potensiutama.kusenstoredriver.model.KategoryModel;
import com.potensiutama.kusenstoredriver.model.MakananModel;
import com.potensiutama.kusenstoredriver.model.OrderModel;
import com.potensiutama.kusenstoredriver.model.ServerUserModel;
import com.potensiutama.kusenstoredriver.model.TokenModel;
import com.google.firebase.database.FirebaseDatabase;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Common {

    public static final String KURIR_REF = "Kurir";
    public static final String CATEGORY_REF = "Kategori";
    public static final String ORDER_REF = "Orders";
    public static final String NOTI_TITLE = "title";
    public static final String NOTI_CONTENT = "content";
    public static final String TOKEN_REF = "Tokens";
    public static final String IS_OPEN_ACTIVITY_NEW_ORDER = "IsOpenActivityNewOrder";
    public static ServerUserModel currentServerUser;
    public static KategoryModel categorySelected;
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static MakananModel selectedFood;

    public static OrderModel orderModelPembayaran;

    public enum ACTION{
        CREATE,
        UPDATE,
        DELETE
    }

    public static void setSpanString(String welcome, String name, TextView textView) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan,0,name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder,TextView.BufferType.SPANNABLE);
    }

    public static void setSpanStringColor(String welcome, String name, TextView textView, int color) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan,0,name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color),0,name.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder,TextView.BufferType.SPANNABLE);
    }

    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus){
            case 0:
                return "Menunggu Proses Pembayaran";
            case 1:
                return "Menunggu Konfirmasi Toko";
            case 2:
                return "Pesanan Sedang Dikemas";
            case 3:
                return "Produk Dikirim";
            case 4:
                return "Selesai";
            case -1:
                return "Dibatalkan";
            default:
                return "Unk";
        }
    }

    public static void showNotification(Context context, int id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if(intent != null){
            pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        }
        String NOTIFICATION_CHANNEL_ID = "foodorderingsystems";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "FOOD ORDER FAISAL RIZA",NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("FOOD ORDER SYSTEM MUHAMMAD FAISAL RIZA");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_restaurant_menu_black_24dp));
        if(pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id,notification);


    }

    public static void updateToken(Context context, String newToken) {
        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REF)
                .child(Common.currentServerUser.getUid())
                .setValue(new TokenModel(Common.currentServerUser.getPhone(),newToken))
                .addOnFailureListener(e -> {
                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public static String createTopicOrder() {
        return new StringBuilder("/topics/new_order").toString();
    }

    public static String formatPrice(double price) {
        if(price != 0){
            DecimalFormat df = new DecimalFormat("#,###");
            df.setRoundingMode(RoundingMode.UP);
            String finalPrice = new StringBuilder(df.format(price)).toString();
            return  finalPrice.replace(",",".");
        }else{
            return "0";
        }

    }
}
