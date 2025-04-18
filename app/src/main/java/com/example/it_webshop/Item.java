package com.example.it_webshop;

public class Item {
    private String id;
    private String name;
    private String info;
    private String price;
    private int imageResource;
    private int cartedCount;

    public Item(int imageResource, String info, String name, String price, int cartedCount) {
        this.imageResource = imageResource;
        this.info = info;
        this.name = name;
        this.price = price;
        this.cartedCount = cartedCount;
    }

    public Item() {
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getInfo() {
        return info;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public int getCartedCount() {
        return cartedCount;
    }
    public String _getId(){
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
