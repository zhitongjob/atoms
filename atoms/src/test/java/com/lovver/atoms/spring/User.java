package com.lovver.atoms.spring;

import java.io.Serializable;

//缓存对象
public class User implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;

    public User(){
    }

    public User(String name){
        this.name= name;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}