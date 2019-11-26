package cert.aiops.pega.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ProvinceUtil {
    private Logger logger= LoggerFactory.getLogger(ProvinceUtil.class);

    private HashMap<String,String>  provinces;
    private final String __NONAME="unknown";

    public ProvinceUtil(){
        provinces=new HashMap<>();
        init();
    }

    private void init(){
        provinces.put("国家", "gj");
        provinces.put("北京","bj");
        provinces.put("上海","sh");
        provinces.put("江苏","js");
        provinces.put("浙江","zj");
        provinces.put("安徽","ah");
        provinces.put("福建","fj");
        provinces.put("江西","jx");
        provinces.put("湖南","hnx");
        provinces.put("山东","sd");
        provinces.put("河南","hny");
        provinces.put("内蒙","nmg");
        provinces.put("湖北","hbe");
        provinces.put("宁夏","nx");
        provinces.put("新疆","xj");
        provinces.put("广东","gd");
        provinces.put("西藏","xz");
        provinces.put("海南","hnq");
        provinces.put("广西","gx");
        provinces.put("四川","sc");
        provinces.put("河北","hbj");
        provinces.put("贵州","gz");
        provinces.put("重庆","cq");
        provinces.put("山西","sxj");
        provinces.put("云南","yn");
        provinces.put("辽宁","ln");
        provinces.put("陕西","sxq");
        provinces.put("吉林","jl");
        provinces.put("甘肃","gs");
        provinces.put("黑龙","hlj");
        provinces.put("青海","qh");
        provinces.put("台湾","tw");
        provinces.put("香港","xg");
        provinces.put("澳门","am");
    }
    public String getShortName(String key){
        String value=provinces.get(key);
        if(value!=null)
            return value;
        logger.info("ProvinceUtil: find no key in provinces, key={}",key);
        return __NONAME;
    }

}
