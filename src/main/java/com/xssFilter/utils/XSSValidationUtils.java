package com.xssFilter.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@UtilityClass
public class XSSValidationUtils {

    public final Pattern pattern = Pattern.compile("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.\\/?\\s]*$", Pattern.CASE_INSENSITIVE);


    public static boolean isValidURL(String uri, List<String> skipWords) {
        AtomicBoolean flag= new AtomicBoolean(false);
         String[]  urls=uri.split("\\/");

        Arrays.stream(urls).filter(e->!StringUtils.isEmpty(e)).forEach(url->{
            String val=String.valueOf(url);
            System.out.println("value:"+val);
           // if(skipWords.stream().anyMatch(val::equalsIgnoreCase)){
                if(skipWords.stream().anyMatch(p->val.toLowerCase().contains(p.toLowerCase()))){
                System.out.println("bad char found!!!!!");
                flag.set(true);
            }
            Matcher matcher = pattern.matcher(val);
            if (!matcher.matches()) {
                System.out.println("Invalid char found!!!!!");
                flag.set(true);
            }else{
                System.out.println("valid char found: "+val);
            }
        });
        return !flag.get();
    }

    public static boolean isValidRequestParam(String param, List<String> skipWords) {
        AtomicBoolean flag= new AtomicBoolean(false);
        String[]  paramList=param.split("&");

        Arrays.stream(paramList).filter(e->!StringUtils.isEmpty(e)).forEach(url->{
            String val=String.valueOf(url);
            System.out.println("value:"+val);
            if(skipWords.stream().anyMatch(val::equalsIgnoreCase)){
                System.out.println("bad char found!!!!!");
                flag.set(true);
            }
            Matcher matcher = pattern.matcher(val);
            if (!matcher.matches()) {
                System.out.println("Invalid char found!!!!!");
                flag.set(true);
            }else{
                System.out.println("valid char found: "+val);
            }
        });
        return !flag.get();
    }


    public static boolean isValidURLPattern(String uri, List<String> skipWords) {
        AtomicBoolean flag= new AtomicBoolean(false);
        String[]  urls=uri.split("\\/");

        try {
            Arrays.stream(urls).filter(e -> !StringUtils.isEmpty(e)).forEach(url -> {
                String val = String.valueOf(url);
                Map<String, Object> mapping = jsonToMap(new JSONObject(val));
                System.out.println("Map; " + mapping);
                mapping.forEach((key, value) -> {
                       System.out.println("key  "+key+"  value:"+value);
                    if (skipWords.stream().anyMatch(String.valueOf(value)::equalsIgnoreCase)) {
                        System.out.println("bad char found!!!!!");
                        flag.set(true);
                    }
                    Matcher matcher = pattern.matcher(String.valueOf(value));
                    if (!matcher.matches()) {
                        System.out.println(key + "  : Invalid char found!!!!!");
                        flag.set(true);
                    } else {
                        System.out.println("valid char found: " + String.valueOf(value));
                    }
                });

            });
        }catch(Exception ex){
            flag.set(true);
        }
        return !flag.get();
    }

    public static boolean isValidURLPattern2(String uri, List<String> skipWords) {
        AtomicBoolean flag= new AtomicBoolean(false);
        String[]  urls=uri.split("\\/");

        try {
            Arrays.stream(urls).filter(e -> !StringUtils.isEmpty(e)).forEach(url -> {
                String val = String.valueOf(url);
                Set<Object> contentSet = jsonToSet(new JSONObject(val));
                System.out.println("Map; " + contentSet);
                contentSet.forEach(item -> {
                    System.out.println("item: " + item);
                    if (skipWords.stream().anyMatch(String.valueOf(item)::equalsIgnoreCase)) {
                        System.out.println("bad char found!!!!!");
                        flag.set(true);
                    }
                    Matcher matcher = pattern.matcher(String.valueOf(item));
                    if (!matcher.matches()) {
                        System.out.println(item + "  : Invalid char found!!!!!");
                        flag.set(true);
                    } else {
                        System.out.println("valid char found: " + String.valueOf(item));
                    }
                });

            });
        }catch(Exception ex){
            log.error("Error: ", ex);
            flag.set(true);
        }
        return !flag.get();
    }

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json,retMap);
        }
        return retMap;
    }

    public static Set<Object> jsonToSet(JSONObject json) throws JSONException {
        Set<Object> retMap = new HashSet<>();

        if(json != JSONObject.NULL) {
            processMapToSet(json,retMap);
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object, Map<String, Object> map) throws JSONException {


        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

         //   System.out.println("key  "+key+"  value:"+ value);

            if(value instanceof JSONArray) {
                value = toList(key,(JSONArray) value,map);
            }else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value,map);
            }else {
                map.put(key, value);
            }
        }
        return map;
    }

    public static void processMapToSet(JSONObject object, Set<Object> set) throws JSONException {


        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            //   System.out.println("key  "+key+"  value:"+ value);

            if(value instanceof JSONArray) {
                processListToSet(key,(JSONArray) value,set);
            }else if(value instanceof JSONObject) {
                processMapToSet((JSONObject) value,set);
            }else {
                set.add(key);
                set.add(value);
            }
        }
    }


    public static List<Object> toList(String key,JSONArray array,Map<String, Object> map ) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList(key,(JSONArray) value,map);
            }else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value,map);
            }else{
                String mapValue=String.valueOf(value);
                if(map.containsKey(key)){
                    mapValue+=","+String.valueOf(map.get(key));
                }
                map.put(key, mapValue);
            }
            list.add(value);
        }
        return list;
    }

    public static void processListToSet(String key,JSONArray array,Set<Object> set) throws JSONException {
//        List<Object> list = new ArrayList<Object>();
        set.add(key);
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                processListToSet(key,(JSONArray) value,set);
            }else if(value instanceof JSONObject) {
                processMapToSet((JSONObject) value,set);
            }else{
                String itemStr=String.valueOf(value);
//                if(map.containsKey(key)){
//                    mapValue+=","+String.valueOf(map.get(key));
//                }
//                map.put(key, mapValue);
                set.add(itemStr);
            }
//            list.add(value);
        }
    }


    public static String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

}
