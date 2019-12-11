package cert.aiops.pega.service;

import cert.aiops.pega.bean.Judgement;
import cert.aiops.pega.dao.JudgementDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JudgementService {
    @Autowired
    private JudgementDao dao;

    public void storeJudgement(Judgement judgement){
        dao.storeJudgement(judgement);
    }
}
