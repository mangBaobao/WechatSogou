package cert.aiops.pega.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableCaching
@PropertySource("classpath:utility.properties")
public class  CacheConfigurer  {

    @Autowired
    private Environment environment;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(){
        RedisStandaloneConfiguration redisConf = new RedisStandaloneConfiguration();
        redisConf.setHostName(environment.getProperty("spring.redis.host"));
        redisConf.setPort(environment.getProperty("spring.redis.port",Integer.class));
        redisConf.setPassword(RedisPassword.of(environment.getProperty("spring.redis.password")));
        redisConf.setDatabase(environment.getProperty("spring.redis.database",Integer.class));
        return new LettuceConnectionFactory(redisConf);
    }

//    @Bean
//    public RedisConnectionFactory redisConnectionFactory(){
//        Map<String,Object> source=new HashMap<>();
//        source.put("spring.redis.cluster.nodes",environment.getProperty("spring.redis.cluster.nodes"));
//        source.put("spring.redis.cluster.timeout",environment.getProperty("spring.redis.cluster.timeout"));
//        source.put("spring.redis.lettuce.pool.min-idle",environment.getProperty("spring.redis.lettuce.pool.min-idle",Integer.class));
//        source.put("spring.redis.lettuce.pool.max-idle",environment.getProperty("spring.redis.lettuce.pool.max-idle",Integer.class));
//        source.put("spring.redis.lettuce.pool.max-wait",environment.getProperty("spring.redis.lettuce.pool.max-wait"));
//        source.put("spring.redis.lettuce.pool.max-active",environment.getProperty("spring.redis.lettuce.pool.max-active",Integer.class));
//        RedisClusterConfiguration clusterConfiguration;
//        clusterConfiguration=new RedisClusterConfiguration(new MapPropertySource("RedisClusterConfiguration",source));
//        return  new LettuceConnectionFactory(clusterConfiguration);
//    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration(){
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(environment.getProperty("spring.cache.redis.time-to-live",Integer.class)))
                .disableCachingNullValues();
    }

    @Bean
    public RedisCacheManager cacheManager(){
        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(cacheConfiguration())
                .transactionAware()
                .build();
    }
    @Bean(name="redisTemplate")
    public RedisTemplate<String,Object>  RedisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        return  redisTemplate;
    }
}
