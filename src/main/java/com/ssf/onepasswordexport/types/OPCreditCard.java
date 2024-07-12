package com.ssf.onepasswordexport.types;

import java.util.Map;

public class OPCreditCard extends OnePasswordItem{

    public OPCreditCard() {

    }
    public OPCreditCard(Map<String, Object> item){
        super(item);
    }

    @Override
    public String getCategory() {
        return "CREDIT_CARD";
    }

    @Override
    public String[] getExtraDataKeysTags(){
        return new String[]{
                "cardholder", "Name on card",
                "type", "Card type",
                "ccnum", "Card number",
                "cvv", "Verification number",
                "expiry", "Expiration date",
                "validFrom", "Valid from",
                "bank", "Bank name",
                "phoneLocal", "Local phone number",
                "phoneTollFree", "Toll free number",
                "website", "Website",
                "pin", "PIN number",
                "creditLimit", "Credit limit"
        };
    }
}
