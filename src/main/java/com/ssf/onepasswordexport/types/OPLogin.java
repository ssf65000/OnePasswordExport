package com.ssf.onepasswordexport.types;

import java.util.*;

public class OPLogin extends OnePasswordItem{
    static final String TAG_SQ = "Question";
    static final String TAG_SA = "Answer";


    static int xs_securityQuestionsCount = 0;

    Map<String, String> _securityQuestions = new HashMap<>();
    public OPLogin(){

    }
    public OPLogin(Map<String, Object> itemMap) {
        super(itemMap);
        parseSecurityQuestions();
    }

    @Override
    public String getCategory() {
        return "LOGIN";
    }

    @Override
    public List<String> getHeaders() {
        List<String> ret = super.getHeaders();
        for(int i = 0; i< xs_securityQuestionsCount; i++){
            ret.add(TAG_SQ+" "+i);
            ret.add(TAG_SA+" "+i);
        }
        return ret;
    }
    private void parseSecurityQuestions() {
        List<Map<String, Object>> lst = (List<Map<String, Object>>) _itemMap.getOrDefault("fields", new ArrayList<>());
        for(Map<String, Object> field : lst){
            Map<String, Object> sect = (Map<String, Object>) field.get("section");
            if(sect == null)
                continue;
            if("security questions".equalsIgnoreCase((String) sect.get("id"))){
                _securityQuestions.put((String) field.get("label"), (String) field.get("value"));
            }
        }
        if(_securityQuestions.size() > xs_securityQuestionsCount)
            xs_securityQuestionsCount = _securityQuestions.size();
    }
    @Override
    public Map<String, Object> getCsvData(){
        Map<String, Object> ret = super.getCsvData();
        ret.put(TAG_OTP, _otp);
        int i=0;
        for (Map.Entry<String, String> e : _securityQuestions.entrySet()) {
            ret.put(TAG_SQ + " " + i, e.getKey());
            ret.put(TAG_SA + " " + i, e.getValue());
            i++;
        }
        return ret;
    }
}
