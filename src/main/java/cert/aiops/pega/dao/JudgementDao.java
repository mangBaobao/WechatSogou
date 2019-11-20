package cert.aiops.pega.dao;

import cert.aiops.pega.bean.Judgement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.ArrayList;

@Component
@PropertySource("classpath:utility.properties")
public class JudgementDao {

    @Value("${spring.database.clickhouse}")
    private String database;

    public void addJudgement(Judgement judgement){

    }

    public ArrayList<Judgement> getJudgementByTime(Time time){
        return null;
    }

    public Judgement getJudgementById(String issueId){
        return null;
    }
}
