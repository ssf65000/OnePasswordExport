package com.ssf.onepasswordexport.types;

import com.ssf.onepasswordexport.OPExport;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public abstract class OnePasswordItem {
    static final String TAG_NAME = "Name";
    static final String TAG_URL = "URL";
    static final String TAG_USERNAME = "Username";
    static final String TAG_PASSWORD = "Password";
    static final String TAG_OTP = "OTP";
    public static final String TAG_NOTES = "Notes";


    private static final OnePasswordItem [] xs_availableTypes = new OnePasswordItem[]{
        new OPLogin(), new OPServer(), new OPEmailAccount(), new OPBankAccount(), new OPCreditCard(), new OPWirelessRouter()
    };
    Map<String, Object> _csvOutData = null;
    Map<String, Object> _itemMap;
    String _tags;
    String _name;
    String _url;
    String _userName;
    String _password;
    String _otp;
    String _notes;

    OnePasswordItem(){

    }
    OnePasswordItem(Map<String, Object> itemMap){
        this._itemMap = itemMap;
        _tags = String.valueOf(itemMap.get("tags"));
        _name = (String) _itemMap.get("title");
        _url = getUrl();
        _userName = getUsername();
        _password = getPassword();
        _otp = getOtp();
        _notes = getNotes();

    }
    public static OnePasswordItem getFromMap(Map<String, Object> itemMap){
        String category = (String) itemMap.get("category");
        if(category == null)
            throw new RuntimeException("Empty category for item with ID = "+itemMap.get("id"));
        for(OnePasswordItem op : xs_availableTypes){
            if(op.getCategory().equalsIgnoreCase(category))
                 return createItem(op, itemMap);
        }
        OPUnsupportedCategory ret = new OPUnsupportedCategory(itemMap);
        ret.setCategory(category);
        return ret;
        //throw new RuntimeException("Unsupported category '"+category+"'");
    }

    private static OnePasswordItem createItem(OnePasswordItem op, Map<String, Object> itemMap) {
        try {
            Constructor<? extends OnePasswordItem> cons = op.getClass().getConstructor(Map.class);
            return cons.newInstance(itemMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create an instance of "+op.getClass().getSimpleName()+" : "+e.getMessage());
        }
    }

    public abstract String getCategory();

    public String[] getExtraDataKeysTags(){
        return new String[]{};
    }

    public List<String> getHeaders(){
        Set<String> ret = new LinkedHashSet<>(List.of(TAG_NAME, TAG_USERNAME, TAG_PASSWORD, TAG_URL, TAG_OTP, TAG_NOTES));
        String[] kt = getExtraDataKeysTags();
        for(int i=0;i<kt.length;i+=2)
            ret.add(kt[i+1]);
        return new ArrayList<>(ret);
    }

    public Map<String, Object> getCsvData(){
        if(_csvOutData != null && !_csvOutData.isEmpty())
            return _csvOutData;
        _csvOutData = new HashMap<>();
        _csvOutData.put(TAG_NAME, _name);
        _csvOutData.put(TAG_PASSWORD, _password);
        _csvOutData.put(TAG_URL, _url);
        _csvOutData.put(TAG_USERNAME, _userName);
        _csvOutData.put(TAG_OTP, _otp);
        _csvOutData.put(TAG_NOTES, _notes);
        String [] extraKeysTags = getExtraDataKeysTags();
        for(int i = 0; i< extraKeysTags.length; i+=2){
            String type = getTypeById(extraKeysTags[i]);
            String value = getValueById(extraKeysTags[i]);
            value = formatValue(type, value);
            if(value != null)
                _csvOutData.put(extraKeysTags[i+1], value);
        }

        return _csvOutData;
    }

    private String formatValue(String type, String value) {
        if(value == null || value.isEmpty())
            return value;
        switch(type.toUpperCase()){
            case "STRING":
            case "CONCEALED":
            case "MENU":
            case "CREDIT_CARD_NUMBER":
            case "URL":
            case "OTP":
                return value;
            case "PHONE":
                return formatPhone(value);
            case "DATE":
                return formatDate(value);
            case "MONTH_YEAR":
                return formatMonthYear(value);
        }
        OPExport.log(">>> Unknown data type '"+type+"' for value '"+value+"'");
        return value;
    }

    private String formatPhone(String value) {
        if(value.length() == 10){//8005551234 -> (800)555-1234
            return "("+value.substring(0,3)+")"+value.substring(3,6)+"-"+value.substring(6);
        }
        return value;
    }

    private String formatMonthYear(String value) {
        if(value.length() == 4)// yymm
            return value.substring(0,2)+"/"+value.substring(2);
        if(value.length() == 6)// yyyymm
            return value.substring(0,4)+"/"+value.substring(4);
        return value;
    }

    private String formatDate(String value) {
        try {
            long ts = Long.parseLong(value) * 1000;
            return LocalDate.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault()).toString();
        }catch(Exception ex){
            OPExport.err("Error parsing date '"+value+"' : "+ex.getMessage());
            return value;
        }

    }


    public String getName() {
        return _name;
    }
    public String getCsvOutputCategory(){
        return getCategory()+" "+_tags;
    }
    String getUrl() {
        List<Map<String, Object>> lst = (List<Map<String, Object>>) _itemMap.getOrDefault("urls", new ArrayList<>());
        for(Map<String, Object> urlmap : lst){
            Boolean primary = (Boolean) urlmap.getOrDefault("primary", false);
            if(primary){
                String url = (String) urlmap.get("href");
                return truncateUrl(url);
            }
        }
        return null;
    }

    String truncateUrl(String url) {
        if(OPExport.xs_truncateUrl && url != null){
            try {
                URI u = new URI(url);
                return u.getScheme()+"://"+u.getHost();
            } catch (URISyntaxException e) {
                OPExport.log("Invalid URI syntax : "+url);
                return url;
            }
        }
        return url;
    }

    String getUsername() {
        List<Map<String, Object>> lst = getFields();
        for(Map<String, Object> field : lst){
            if("USERNAME".equalsIgnoreCase((String) field.get("purpose")))
                return (String) field.get("value");
        }
        return null;
    }
    String getPassword() {
        List<Map<String, Object>> lst = getFields();
        for(Map<String, Object> field : lst){
            if("PASSWORD".equalsIgnoreCase((String) field.get("purpose"))) {
                return (String)field.get("value");
            }
        }
        return null;
    }
    String getOtp() {
        Map<String, Object> field = getFieldByType("OTP");
        if(field == null)
            return null;
        String otp = (String) field.get("value");
        if(otp == null)
            return null;
        int idx = otp.indexOf("secret=");
        if(idx != -1)
            otp = otp.substring(idx+7);
        idx = otp.indexOf('&');
        if(idx != -1)
            otp = otp.substring(0,idx);
        return otp;
    }
    String getNotes(){
        Map<String, Object> fld = getFieldById("notesPlain");
        if(fld != null)
            return (String) fld.get("value");
        return null;
    }

    List<Map<String, Object>> getFields(){
        return (List<Map<String, Object>>)_itemMap.getOrDefault("fields", new ArrayList<>());
    }


    Map<String, Object> getFieldById(String id){
        return getFieldById(id, getFields());
    }
    Map<String, Object> getFieldById(String id, List<Map<String, Object>> fields){
        if(fields == null || id == null)
            return null;
        for(Map<String, Object> m : fields){
            String fieldId = (String) m.get("id");
            if(id.equalsIgnoreCase(fieldId))
                return m;
        }
        return null;
    }
    String getValueById(String id){
        Map<String, Object> fld = getFieldById(id);
        if(fld == null)
            return null;
        return (String) fld.get("value");
    }
    String getTypeById(String id){
        Map<String, Object> fld = getFieldById(id);
        if(fld == null)
            return null;
        return (String) fld.get("type");
    }

    Map<String, Object> getFieldByType(String type){
        return getFieldByType(type, getFields());
    }
    Map<String, Object> getFieldByType(String type, List<Map<String, Object>> fields){
        if(fields == null || type == null)
            return null;
        for(Map<String, Object> m : fields){
            String fieldType = (String) m.get("type");
            if(type.equalsIgnoreCase(fieldType))
                return m;
        }
        return null;
    }
}
