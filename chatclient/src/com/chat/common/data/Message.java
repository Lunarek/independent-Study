package com.chat.common.data;

import com.chat.common.utils.EncryptionUtils;
import com.chat.common.utils.Type;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Message implements Serializable {
    public static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    ;
    private String msg;
    private Type type;
    private String username;
    private String time;

    public void setMsg(String msg) {
        this.msg = EncryptionUtils.encrypt(msg);
    }

    public String getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = sdf.format(time);
    }

    public String getUsername() {
        if(username==null || username.isEmpty())
            return "";
        return username;
    }

    public String getMsg() {
        return EncryptionUtils.decrypt(msg);
    }

    public Type getType() {
        return type;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Message() {
        this.time = sdf.format(new Date());
    }


    public Message(String msg, Type type) {
        this();
        this.setMsg(msg);
        this.type = type;
    }

    public Message(String msg, String username) {
        this();
        this.setMsg(msg);
        this.type = Type.INFO;
        this.username = username;
    }

    public Message(Exception e, String msg) {
        this();
        this.type = Type.ERROR;
        this.setMsg(msg + ":" + e.getMessage());
    }

    public Message(String msg) {
        this();
        this.type = Type.INFO;
        this.setMsg(msg);
    }

    @Override
    public String toString(){
        return "<" + this.getTime() + " - " + this.getType().name() +" #"+this.getUsername()+" > " + this.getMsg() +
                "\n";
    }
}
