package com.potensiutama.kusenstoreclient.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import androidx.navigation.NavController;
import androidx.room.FtsOptions;

import com.bumptech.glide.Glide;
import com.potensiutama.kusenstoreclient.Database.CartItem;
import com.potensiutama.kusenstoreclient.HomeActivity;
import com.potensiutama.kusenstoreclient.R;
import com.potensiutama.kusenstoreclient.model.CategoryModel;
import com.potensiutama.kusenstoreclient.model.OrderModel;
import com.potensiutama.kusenstoreclient.model.ProdukModel;
import com.potensiutama.kusenstoreclient.model.TokenModel;
import com.potensiutama.kusenstoreclient.model.UserModel;
import com.google.firebase.database.FirebaseDatabase;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.potensiutama.kusenstoreclient.ui.akunsaya.AkunSayaActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;

public class Common {

    public static final String USER_REFERENCESER = "Users";
    public static final String REKENING_REF = "Rekening";
    public static final String AKUN_SAYA_REF = "Informasi";
    public static final String TOKO_SAYA_REF = "PENGATURANTOKO";
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String CATEGORY_REF = "Kategori";
    public static final String ORDER_REF = "Orders";
    public static final String NOTI_TITLE = "title";
    public static final String NOTI_CONTENT = "content";
    public static final String TOKEN_REF = "Tokens";
    public static final String IS_OPEN_ACTIVITY_NEW_ORDER = "IsOpenActivityNewOrder";
    public static final String FILE_PRINT = "orderclient.pdf";
    public static CategoryModel categorySelected;
    public static ProdukModel selectedFood;
    public static UserModel currentUser;
    public static final String PRODUK_REF = "Produk";

    public static List<ProdukModel> daftarProduk;
    public static ProdukModel selectedProduk;
    public static OrderModel orderModelPembayaran;

    public static Double selectedLatitude;
    public static Double selectedLongitude;

    public static HomeActivity homeActivity;

    public static AkunSayaActivity akunSayaActivity;

    public static String formatPrice(double price) {
        if(price != 0){
            DecimalFormat df = new DecimalFormat("#,###");
            //df.setRoundingMode(RoundingMode.UP);
            String finalPrice = new StringBuilder(df.format(price)).toString();
            return  finalPrice.replace(",",".");
        }else{
            return "0";
        }

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

    public static String createOrderNumber() {
        return new StringBuilder()
                .append(System.currentTimeMillis())
                .append(Math.abs(new Random().nextInt()))
                .toString();
    }

    public static String getDateOfWeek(int i) {
        switch (i)
        {
            case 1:
                return "Senin";
            case 2:
                return "Selasa";
            case 3:
                return "Rabu";
            case 4:
                return "Kamis";
            case 5:
                return "Jum'at";
            case 6:
                return "Sabtu";
            case 7:
                return "Minggu";
                default:
                    return "Unk";
        }
    }

    public static String converStatusToText(int orderStatus) {
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
                .child(Common.currentUser.getUid())
                .setValue(new TokenModel(Common.currentUser.getPhone(),newToken))
                .addOnFailureListener(e -> {
                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public static String createTopicOrder() {
        String TOPIC = "/topics/new_order";
        return TOPIC;
    }
    public static String getAppPath(Context context) {

        File dir = new File(android.os.Environment.getExternalStorageDirectory()
                + File.separator
                + context.getResources().getString(R.string.app_name)
                + File.separator
        );
        if(!dir.exists())
            dir.mkdir();

        return dir.getPath()+File.separator;
    }

    public static Observable<CartItem> getBitmapFromUrl(Context context, CartItem cartItem, Document document)  {
        return Observable.fromCallable(() -> {
            Bitmap bitmap = Glide.with(context)
                    .asBitmap()
                    .load(cartItem.getProdukImage())
                    .submit().get();

            Image image = Image.getInstance(bitmapToByteArray(bitmap));
            image.scaleAbsolute(80, 80);
            document.add(image);
            return cartItem;
        });

    }

    private static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
        return stream.toByteArray();
    }
}
