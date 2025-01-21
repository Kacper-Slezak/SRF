package com.srf.utils;

import com.srf.models.User;

public class UserSingleton {
    private static UserSingleton instance;

    private User user;

    private UserSingleton() {}

    static {
        try{
            instance = new UserSingleton();
        } catch (Exception e){
            throw new RuntimeException("Singleton exception");
        }
    }

    public static UserSingleton getInstance(){
        return instance;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
