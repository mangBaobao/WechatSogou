package cert.aiops.pega.util;

import cert.aiops.pega.bean.PegaEnum;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class TabSerializer {
    private static void addValuesFromMap(Map<?,?> obj, StringJoiner joiner){

        for( Object key: obj.keySet()){
            Object value = obj.get(key);
            if(value == null)
                continue;
            addValue(key, joiner);
            addValue(value,joiner);
        }
    }

    public static String objectsFromMapToList(Map<?,?> obj, StringJoiner joiner){
        String head="(";
        String end=")";
        String delimiter="),(";
        String replacable=",),(,";
        String replaceableH="(,";
        String replaceableE=",)";
        for( Object key: obj.keySet()){
            Object value = obj.get(key);
            if(value == null)
                continue;
            joiner.add(head);
            addValue(key, joiner);
            addValue(value,joiner);
            joiner.add(end);
        }
       String result=joiner.toString();
        result=result.replace(replacable,delimiter).replace(replaceableH,head).replace(replaceableE,end);
        return result;
    }


    public static  void addValuesFromList(List<?> obj, StringJoiner joiner){
        String head="(";
        String end="),";
        for(int i = 0 ; i<obj.size();i++){
            Object value = obj.get(i);
            addValue(value, joiner);
        }
    }

    public static  String addObjectsFromLists(List<?> obj, StringJoiner joiner){
        String head="(";
        String end =")";
       String delimiter="),(";
       String replacable=",),(,";
        for(int i = 0 ; i<obj.size();i++){
            Object value = obj.get(i);
         //   joiner.add(head);
            addValue(value, joiner);
            if(i != obj.size()-1)
                joiner.add(delimiter);

        }
        String result=head+joiner.toString()+end;
        result=result.replace(replacable,delimiter);
        return result;
    }

    public static void addValue(Object value, StringJoiner joiner){
        if(value == null)
            return;
        if(value instanceof List){
            addValuesFromList((List<?>)value,joiner);
        }else if(value instanceof  Map){
            addValuesFromMap((Map<?,?>)value, joiner);
        }else if (value instanceof  TabSerializable){
            joiner.add(((TabSerializable)value).toTabbedString());
        }else if(value instanceof String){
            joiner.add("'"+String.valueOf(value)+"'");
        }else if(value instanceof Date){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            joiner.add("'"+formatter.format((Date)value)+"'");
//        }else if(value instanceof PegaEnum.Net){
//            joiner.add("'"+((PegaEnum.Net) value).name()+"'");
        }else if(value instanceof PegaEnum.State){
            joiner.add("'"+((PegaEnum.State)value).name()+"'");
        }else if(value instanceof PegaEnum.Avail){
            joiner.add("'"+((PegaEnum.Avail)value).name()+"'");
        }else if(value instanceof PegaEnum.ObjectState){
            joiner.add("'"+((PegaEnum.ObjectState)value).name()+"'");
        } else if(value instanceof String){
            joiner.add("'"+String.valueOf(value)+"'");
        }else {
            joiner.add(String.valueOf(value));
        }

    }
}
