package cert.aiops.pega.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import ru.yandex.clickhouse.ClickHouseConnection;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;

import javax.annotation.PostConstruct;
import java.sql.*;

@Component
public class ClickhouseUtil {
    Logger logger = LoggerFactory.getLogger(ClickhouseUtil.class);

    @Autowired
    Environment environment;

    private static ClickhouseUtil clickhouseUtil;
    private  ClickHouseConnection conn = null;
    @PostConstruct
    public void init() {
        clickhouseUtil = this;
        initiateConn();
    }

    public static ClickhouseUtil getInstance() {
        return clickhouseUtil;
    }

    public Connection getConn(){
        if(conn!=null)
            return this.conn;
        else{
            initiateConn();
            return this.conn;
        }
    }

    private  void initiateConn() {
        ClickHouseProperties properties = new ClickHouseProperties();
        properties.setUser(environment.getProperty("spring.database.username"));
        properties.setPassword(environment.getProperty("spring.database.password"));
        properties.setDatabase(environment.getProperty("spring.database.clickhouse"));
        properties.setSocketTimeout(environment.getProperty("spring.database.socket-timeout", int.class));
        ClickHouseDataSource clickHouseDataSource = new ClickHouseDataSource(environment.getProperty("spring.database.address"), properties);

        try {
            conn = clickHouseDataSource.getConnection();
            logger.info("create clickhouse connection successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }


    public ResultSet exeSql(String sql) {
        logger.info("clickhouse execute sql:{}", sql);
        Connection conn = getConn();
        try {
            Statement statement = conn.createStatement();
            ResultSet results = statement.executeQuery(sql);
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
