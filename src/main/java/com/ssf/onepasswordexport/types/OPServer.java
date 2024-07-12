package com.ssf.onepasswordexport.types;

import java.util.Map;

public class OPServer extends OnePasswordItem{
    public OPServer(){
        super();
    }
    public OPServer(Map<String, Object> itemMap){
        super(itemMap);
    }

    @Override
    public String getCategory() {
        return "SERVER";
    }

    @Override
    String getUrl(){
        return getValueById("url");
    }
    @Override
    String getPassword() {
        return getValueById("password");
    }
    @Override
    String getUsername() {
        return getValueById("username");
    }
}
