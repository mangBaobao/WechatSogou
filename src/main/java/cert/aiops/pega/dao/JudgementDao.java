package cert.aiops.pega.dao;

import cert.aiops.pega.bean.Judgement;
import cert.aiops.pega.util.ClickhouseUtil;
import cert.aiops.pega.util.PegaEnum;
import cert.aiops.pega.util.TabSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringJoiner;

@Component
@PropertySource("classpath:utility.properties")
public class JudgementDao {

    @Value("${spring.database.clickhouse}")
    private String database;

    public void storeJudgement(Judgement judgement){
        String sql="insert into "+database+".judgement_history(issue_id,exception_code,status,action_type,content,update_time) values ("+ judgement.toTabbedString()+")";
        ClickhouseUtil.getInstance().exeSql(sql);
    }

    public void storeJudgementList(ArrayList<Judgement> judgements){
        StringJoiner joiner = new StringJoiner(",");
        String result= TabSerializer.addObjectsFromLists(judgements,joiner);
        String sql="insert into "+database+".judgement_history(issue_id,exception_code,status,action_type,content,update_time) values "+result;
        ClickhouseUtil.getInstance().exeSql(sql);
    }

    public ArrayList<Judgement> queryJudgementByTime(Date time){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql="select * from pega_test.judgement_history where update_time<'"+formatter.format(time)+"'";
        ResultSet results = ClickhouseUtil.getInstance().exeSql(sql);
       return turnData2Object(results);
    }

    private ArrayList<Judgement> turnData2Object(ResultSet results){
        ArrayList<Judgement> judgements=new ArrayList<>();
        Judgement judgement;
        try{
            while(results.next()){
                judgement=new Judgement();
                judgement.setStatus(PegaEnum.IssueStatus.valueOf(results.getString("status")));
                judgement.setIssueId(results.getString("issue_id"));
                judgement.setExceptionCode(PegaEnum.RegistrationExceptionCode.valueOf(results.getString("exception_code")));
                judgement.setContent(results.getString("content"));
                judgement.setUpdateTime(results.getTime("update_time"));
                judgement.setActionType(PegaEnum.ActionType.valueOf(results.getString("action_type")));
                judgements.add(judgement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return judgements;
    }

    public ArrayList<Judgement> queryJudgementById(String issueId){
        String sql="select * from pega_test.judgement_history where issue_id='"+issueId+"'";
        ResultSet results = ClickhouseUtil.getInstance().exeSql(sql);
      return turnData2Object(results);
    }
}
