package cert.aiops.pega.util;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Deprecated
@Component
public class RoutineCacheKeyGenerator implements KeyGenerator {
    //todo input routine task object and output redis key
    @Override
    public Object generate(Object o, Method method, Object... objects) {
        return null;
    }
}
