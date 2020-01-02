package cert.aiops.pega.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPAddrUtil {
    public static boolean isIPAddress(String ipaddr) {
        boolean flag = false;
        Pattern pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
        Matcher m = pattern.matcher(ipaddr.trim());
        flag = m.matches();
        return flag;
    }

    public static List<String> invalidIPAddress(String[] ips){
        List<String> invalidIps=new ArrayList<>();
        for(String ip:ips){
            if(isIPAddress(ip)==false)
                invalidIps.add(ip);
        }
        return invalidIps;
    }
}
