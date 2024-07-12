package com.ssf.onepasswordexport.types;

import java.util.Map;

public class OPBankAccount extends OnePasswordItem{

    public OPBankAccount(){
    }

    public OPBankAccount(Map<String, Object> itemMap) {
        super(itemMap);
    }
    @Override
    public String getCategory() {
        return "BANK_ACCOUNT";
    }
    public String[] getExtraDataKeysTags(){
        return new String[]{
                "bankName", "Bank name",
                "owner", "Name on account",
                "accountType", "Type",
                "routingNo", "Routing number",
                "accountNo", "Account number"
        };
    }
}
