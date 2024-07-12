package com.ssf.onepasswordexport.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OPUnsupportedCategory extends OnePasswordItem{
    List<String> _fieldIds = new ArrayList<>();
    String _category;

    public OPUnsupportedCategory(Map<String, Object> itemMap) {
        super(itemMap);
        List<Map<String, Object>> flds = getFields();
        for(Map<String, Object> fld : flds){
            Object value = fld.get("value");
            if(value == null)
                continue;
            _fieldIds.add((String) fld.get("id"));
        }
    }

    @Override
    public String getCategory() {
        return _category;
    }
    public void setCategory(String category) {
        _category = category;
    }
    @Override
    public String[] getExtraDataKeysTags(){
        String[] ret = new String[_fieldIds.size() * 2];
        int i=0;
        for(String fieldId : _fieldIds){
            ret[i++] = fieldId;
            ret[i++] = fieldId;
        }
        return ret;
    }

}
