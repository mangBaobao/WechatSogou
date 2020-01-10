package cert.aiops.pega.channels;

import cert.aiops.pega.bean.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Component
public class MessageManager {
    Logger logger = LoggerFactory.getLogger(MessageManager.class);
    private HashMap<Long, ArrayList<Message>> messagesByChannelId;



}
