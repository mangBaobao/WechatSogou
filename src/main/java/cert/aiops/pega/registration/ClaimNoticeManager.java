package cert.aiops.pega.registration;


import cert.aiops.pega.util.IdentityUtil;
import cert.aiops.pega.util.RedisClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ClaimNoticeManager {
    private Logger logger= LoggerFactory.getLogger(ClaimNoticeManager.class);
    private final String __CHECKIN="checkin";
    private long lastRoundTime =-1;
    private Set<ZSetOperations.TypedTuple<String>> claims;
    private HashMap<String,ClaimNotice> claimNotices;
    private   Set<ZSetOperations.TypedTuple<String>> lastRoundResultSet;
    private HashMap<String,ClaimNotice> lastRoundClaimNotices;
    @Autowired
    private RedisClientUtil redisClientUtil;
    public ClaimNoticeManager(){
        claims=new HashSet<>();
        claimNotices=new HashMap<>();
        lastRoundClaimNotices=new HashMap<>();
        lastRoundResultSet=new HashSet<>();
    }


    private  void  receiveLastRoundClaims(long currentTime){
        if(lastRoundTime <0)
            lastRoundResultSet=redisClientUtil.getSetwithRange(this.__CHECKIN,0,-1);
        else
        {
            lastRoundResultSet=redisClientUtil.getSetwithRange(this.__CHECKIN, lastRoundTime,currentTime);
        }
        lastRoundTime =currentTime;
        claims.addAll(lastRoundResultSet);
    }

    protected Set<ZSetOperations.TypedTuple<String>> getTotalClaimSet(){
        if(claims.size()==0)
            receiveLastRoundClaims(System.currentTimeMillis());
        return claims;
    }

    public HashMap<String,ClaimNotice> getClaimNotices(){
        if(claimNotices.size()==0) {
            receiveLastRoundClaims(System.currentTimeMillis());
            fullSyncClaimNotices();
        }
        return claimNotices;
    }

    public HashMap<String,ClaimNotice> getLastRoundClaimNotices(){
        if(lastRoundClaimNotices.size()==0){
            receiveLastRoundClaims(System.currentTimeMillis());
            incSyncClaimNotices();
        }
        return lastRoundClaimNotices;
    }


    public void incSyncClaimNotices(){
        syncClaimNotices(lastRoundResultSet,lastRoundClaimNotices);
        claimNotices.putAll(lastRoundClaimNotices);
    }

    public void fullSyncClaimNotices(){
        syncClaimNotices(claims,claimNotices);
    }

    private void syncClaimNotices(  Set<ZSetOperations.TypedTuple<String>>  claims,HashMap<String,ClaimNotice> results){

        Iterator<ZSetOperations.TypedTuple<String>> iterator=claims.iterator();
        ClaimNotice notice;
        ZSetOperations.TypedTuple<String> member;
        String[] splittedNotice;
        while(iterator.hasNext()){
            member=iterator.next();
            notice=new ClaimNotice();
            notice.setClaimTime(new Date(new Double(member.getScore()).longValue()));
            splittedNotice= IdentityUtil.unpackClaim(member.getValue());
            notice.setIp(splittedNotice[0]);
            notice.setUuid(splittedNotice[1]);
            results.put(notice.getIp(),notice);
        }
    }
}
