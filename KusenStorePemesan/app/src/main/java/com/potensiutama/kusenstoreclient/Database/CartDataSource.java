package com.potensiutama.kusenstoreclient.Database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface CartDataSource {
    Flowable<List<CartItem>> getAllCart(String uid);

    Single<Integer> countItemInCart(String uid);

    Single<Double> sumPriceInCart(String uid);

    Single<Double> sumOngkirInCart(String uid);

    Single<CartItem> getItemInCart(String foodId, String uid);

    Completable insertOrReplaceAll(CartItem... cartItems);

    Single<Integer> updateCartItems(CartItem cartItems);

    Single<Integer> deleteCartItem(CartItem cartItems);

    Single<Integer> cleanCart(String uid);

    Single<CartItem> getItemWithAllOptionsInCart(String uid, String foodId);
}
