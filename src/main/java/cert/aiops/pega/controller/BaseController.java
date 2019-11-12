package cert.aiops.pega.controller;

import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class BaseController {
    @Autowired
    private HttpServletRequest request;
    protected Date getNowDate()  {
      return new Date();
    }

    public HttpServletRequest getRequestServlet(){
        return request;
    }

public String  getRequesterIp(){
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length()==0|| "unknown".equalsIgnoreCase(ip)){
            ip=request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length()==0|| "unknown".equalsIgnoreCase(ip)){
            ip=request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length()==0|| "unknown".equalsIgnoreCase(ip)){
            ip=request.getRemoteAddr();
            if(ip.equals("127.0.0.1")){
                InetAddress inet=null;
                try{
                    inet=InetAddress.getLocalHost();
                }catch (Exception e){
                    e.printStackTrace();
                }
                ip=inet.getHostAddress();
            }
        }
        if(ip!=null && ip.length()>15){
            if(ip.indexOf(",")>0){
                ip=ip.substring(0,ip.indexOf(","));
            }
        }
        return ip;
}
}
