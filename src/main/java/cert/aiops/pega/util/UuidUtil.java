package cert.aiops.pega.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

//@Component
public class UuidUtil {
    private static Logger logger= LoggerFactory.getLogger(UuidUtil.class);

    private final  static  String  __PREFIX="ALLOC";

    public static String generateUuid(byte[] key){//key :ip and time
        String uuid=UUID.nameUUIDFromBytes(key).toString();
        String allo_uuid=__PREFIX+uuid;
        logger.info("UuidUtil:generate uuid={} by key={}",allo_uuid,new String(key));
        return allo_uuid;
    }

}
