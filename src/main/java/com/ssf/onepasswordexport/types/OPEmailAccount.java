package com.ssf.onepasswordexport.types;

import java.util.Map;

public class OPEmailAccount extends OnePasswordItem{

    public OPEmailAccount(){

    }
    public OPEmailAccount(Map<String, Object> data){
        super(data);

    }
    @Override
    public String getCategory() {
        return "EMAIL_ACCOUNT";
    }
    @Override
    String getUrl(){
        return getValueById("pop_server");
    }
    @Override
    String getPassword() {
        return getValueById("pop_password");
    }
    @Override
    String getUsername() {
        return getValueById("pop_username");
    }
}
