package com.ssf.onepasswordexport.types;

import java.util.Map;

public class OPWirelessRouter extends OnePasswordItem{

    public OPWirelessRouter() {
    }

    public OPWirelessRouter(Map<String, Object> itemMap){
        super(itemMap);
    }

    @Override
    public String getCategory() {
        return "WIRELESS_ROUTER";
    }

    @Override
    public String[] getExtraDataKeysTags(){
        return new String[]{
                "name", "Username",
                "password", "Password",
                "server", "IP address",
                "network_name", "Network name",
                "wireless_security", "Wireless security",
                "wireless_password", "Wireless network password"
        };
    }

}
