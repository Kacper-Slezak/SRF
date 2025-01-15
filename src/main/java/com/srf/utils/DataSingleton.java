package com.srf.utils;

import com.srf.models.User;

public class DataSingleton {
    private static DataSingleton instance;

    private User user;

    private DataSingleton() {}

    static {
        try{
            instance = new DataSingleton();
        } catch (Exception e){
            throw new RuntimeException("Singleton exception");
        }
    }

    public static DataSingleton getInstance(){
        return instance;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
