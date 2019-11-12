package cert.aiops.pega.util;

import java.util.*;

public class Sorter<T> {
    ArrayList<T> candidate;

    public void setCandidates(ArrayList<T> list){
        this.candidate=list;
    }

    public ArrayList<T> getSortedCandidates(){
//        Collection<T> collection=new ArrayList<>();
//        collection.addAll(candidate);
//        Object[] arrays=collection.toArray(new Object[collection.size()]);
//        return (ArrayList<T>) Arrays.asList(arrays);
        Collections.sort((List)candidate);
        return candidate;
    }
}
