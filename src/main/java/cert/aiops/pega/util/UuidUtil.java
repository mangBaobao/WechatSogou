package cert.aiops.pega.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidUtil {
    private Logger logger= LoggerFactory.getLogger(UuidUtil.class);

    private final  String __PREFIX="ALLOC";
    public String generateUuid(byte[] key){
        String uuid=UUID.nameUUIDFromBytes(key).toString();
        String allo_uuid=__PREFIX+"_"+uuid;
        logger.info("UuidUtil:generate uuid={} by key={}",allo_uuid,new String(key));
        return allo_uuid;
    }
}
