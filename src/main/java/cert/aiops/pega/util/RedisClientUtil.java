package cert.aiops.pega.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisClientUtil {
    private static RedisClientUtil redisClientUtil;

    Logger logger = LoggerFactory.getLogger(RedisClientUtil.class);

    @PostConstruct
    public void init(){
        redisClientUtil = this;
    }
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Resource(name="stringRedisTemplate")
    ValueOperations<String,String> valOpsStr;

   @Resource
    RedisTemplate<String , Object>  redisTemplate;
//    @Resource(name="redisTemplate")
//    ValueOperations<Object,Object>  valOpsObj;

    public static RedisClientUtil  getInstance(){
        if(redisClientUtil == null)
            return null;
        return redisClientUtil;
    }


    public void delStr(String key){
        stringRedisTemplate.delete(key);
    }


    public boolean expire(String key, long time){
        try{
            Boolean result=false;
            if(time >0)
                result = stringRedisTemplate.expire(key,time, TimeUnit.SECONDS);
            return result;
        }catch (Exception e){
            logger.error("fail to expire string key:{}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public long getExpire(String key){
        return redisTemplate.getExpire(key,TimeUnit.SECONDS);
    }

    public boolean expireObj(String key, long time){
        try{
            Boolean result=false;
            if(time>0)
                result=redisTemplate.expire(key,time,TimeUnit.SECONDS);
            return result;
        }catch (Exception e){
            logger.error("fail to expire object key:{}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Set<String> getFuzzyKeys(String keyPattern){
        return stringRedisTemplate.keys(keyPattern);
    }

    public void setStr(String key,String value){
        valOpsStr.set(key,value);
    }

    public String getStr(String key){
        String value = valOpsStr.get(key);
        return value;
    }

    public void addList(String key, Object value){
        redisTemplate.opsForList().rightPush(key,value);
    }

    public void batchAddList(String key, List values){
        redisTemplate.opsForList().rightPushAll(key,values);
    }
    /*
    used for response paging
     */
    public List getListPage(String key, int start, int end){
        return redisTemplate.opsForList().range(key,start,end);

    }

    /*
    fetch size of list in (key,list{}) in cache
     */
    public long getListSize(String key){
        return redisTemplate.opsForList().size(key);
    }

    public void addSetSingle(String key,String value,long score){
        stringRedisTemplate.opsForZSet().add(key,value,score);
    }

    public void addSetMultiple(String key, Set<ZSetOperations.TypedTuple<String>>value){
        stringRedisTemplate.opsForZSet().add(key,value);
    }

    public Set<ZSetOperations.TypedTuple<String>> getSetwithRange(String key, long begin, long end){
        return stringRedisTemplate.opsForZSet().rangeByScoreWithScores(key,begin,end);
    }


}
