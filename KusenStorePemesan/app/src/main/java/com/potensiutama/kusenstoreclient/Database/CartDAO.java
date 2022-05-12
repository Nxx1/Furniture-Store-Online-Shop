package com.potensiutama.kusenstoreclient.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface CartDAO {
    @Query("SELECT *FROM Cart WHERE uid=:uid")
    Flowable<List<CartItem>> getAllCart(String uid);

    @Query("SELECT SUM(produkQuantity) from Cart WHERE uid=:uid")
    Single<Integer> countItemInCart(String uid);

    @Query("SELECT SUM((produkPrice) * produkQuantity) FROM Cart WHERE uid=:uid")
    Single<Double> sumPriceInCart(String uid);

    @Query("SELECT SUM((produkOngkir) * produkQuantity) FROM Cart WHERE uid=:uid")
    Single<Double> sumOngkirInCart(String uid);

    @Query("SELECT * FROM Cart WHERE produkId=:produkId AND uid=:uid")
    Single<CartItem> getItemInCart(String produkId, String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrReplaceAll(CartItem... cartItems);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Single<Integer> updateCartItems(CartItem cartItems);

    @Delete
    Single<Integer> deleteCartItem(CartItem cartItems);

    @Query("DELETE FROM Cart WHERE uid=:uid")
    Single<Integer> cleanCart(String uid);

    @Query("SELECT * FROM Cart WHERE produkId=:produkId AND uid=:uid")
    Single<CartItem> getItemWithAllOptionsInCart(String uid, String produkId);
}
