package cert.aiops.pega.util;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
@Deprecated
@Component
public class TemporaryCacheKeyGenerator implements KeyGenerator {
    //todo  input triggered task object and output the redis key
    @Override
    public Object generate(Object o, Method method, Object... objects) {
        return null;
    }
}
