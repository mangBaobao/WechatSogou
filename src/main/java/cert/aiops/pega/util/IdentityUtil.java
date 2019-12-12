package cert.aiops.pega.util;

import cert.aiops.pega.config.PegaConfiguration;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

public class IdentityUtil {
    private static final  String __SPLITTER ="_";
    private static final String __REGISTER_KEY_PREFIX ="ip:";
    private static final String __ACROSS_SPLITTER="-";
    private static final String __COLON=":";
    private static final String __COMMA=",";
    private static final String __EQUAL="=";

    public static String generateFileName(long systemId,long headId,long tailId){
        return systemId+ __SPLITTER +headId+ __SPLITTER +tailId;
    }
    public static long getHeadIdFromIdentity(String key){
        String[] words=key.split(__SPLITTER);
        return Long.valueOf(words[2]);
    }

    public static String generateFuzzyIdentity(long epoch,long systemId){
        return epoch+ __SPLITTER +systemId+"*";
    }

    public static ArrayList<String> sortKeysByHeadId(Set<String> keys){
        ArrayList<String> sortedKeys= new ArrayList<>(keys);
        Collections.sort(sortedKeys,new Comparator(){

            @Override
            public int compare(Object o1, Object o2) {
                String[] key1=((String)o1).split(__SPLITTER);
                String[] key2=((String)o2).split(__SPLITTER);
                long head1=Long.valueOf(key1[2]);
                long head2=Long.valueOf(key2[2]);
                if(head1<head2)
                    return -1;
                else if(head1==head2)
                    return 0;
                else
                    return 1;
            }
        });
        return sortedKeys;
    }

    public static String generateRegisterKey(String ip){return __REGISTER_KEY_PREFIX +ip;}
    public static String generateRegisterName(String net,String shortName,String ip){
        return net+__ACROSS_SPLITTER+shortName+__ACROSS_SPLITTER+ip;
    }
    public static String generateEpochIdentity(long epoch, long systemId){
        return epoch+ __SPLITTER +systemId;
    }

    public static String[] unpackClaim(String value){return value.split(__COLON); }

    public static  String[] unpackNoMatchedIPException(String issueId){
        String[] phases=issueId.split(__SPLITTER);
        if(phases[1].equals(String.valueOf(PegaEnum.RegistrationExceptionCode.NotFoundMatchedIp.getValue())))
            return phases[0].split(__COMMA);
        else
            return null;
    }

    public static String unpackNotFoundUuidException(String issueId){
        String[] phases=issueId.split(__SPLITTER);
        if(phases[1].equals(String.valueOf(PegaEnum.RegistrationExceptionCode.NotFoundUuid.getValue()))) {
           String[] factors=phases[0].split(__ACROSS_SPLITTER);
           return factors[2];
        }
        else
            return null;
    }

    public static ArrayList<String> unpackUuidNotMatchedException(String reason){
        String[] seperated=reason.split(__COMMA);
        ArrayList<String> phases=new ArrayList<>();
        for(int i=0;i<seperated.length;i++){
            String[] t=seperated[i].split((__EQUAL));
            for(int j=0;j<t.length;j++)
                phases.add(t[j]);
        }
        return phases;
    }

    public static byte[] generateUuidInputString(String ip){
        PegaConfiguration pegaConfiguration=SpringContextUtil.getBean(PegaConfiguration.class);
        String workingNet=pegaConfiguration.getWorkingNet();
        String uuid= workingNet+__SPLITTER+ip;
        return uuid.getBytes();
    }
}
