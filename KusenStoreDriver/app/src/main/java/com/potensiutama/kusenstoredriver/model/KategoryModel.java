package com.potensiutama.kusenstoredriver.model;

import java.util.List;

public class KategoryModel {
    private String menu_id,name,image;
    List<MakananModel> foods;

    public KategoryModel() {
    }

    public String getMenu_id() {
        return menu_id;
    }

    public void setMenu_id(String menu_id) {
        this.menu_id = menu_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<MakananModel> getFoods() {
        return foods;
    }

    public void setFoods(List<MakananModel> foods) {
        this.foods = foods;
    }
}
