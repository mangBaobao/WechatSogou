package cert.aiops.pega.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FilterResponse implements Serializable {

    public FilterResponse(){
        responses=new ArrayList<>();
    }

    private List<SystemQueryResponse> responses;

    public List<SystemQueryResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<SystemQueryResponse> responses) {
        this.responses = responses;
    }

    public void addReponse(SystemQueryResponse response){
        responses.add(response);
    }
}
