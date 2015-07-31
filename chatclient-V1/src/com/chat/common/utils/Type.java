package com.chat.common.utils;

public enum Type {
    ALL_USERS(0), MSG(1), SIGNOUT(2), ERROR(3), INFO(4);

    private int val;

    Type(int value){
        this.val = value;
    }

    public int getVal() {
        return val;
    }
}