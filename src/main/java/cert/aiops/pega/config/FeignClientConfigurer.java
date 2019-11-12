package cert.aiops.pega.config;

import feign.Logger;
import feign.codec.Decoder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;
@Configuration
public class FeignClientConfigurer  {

    @Bean
    Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }

    public class JczyMessageConverter extends  MappingJackson2HttpMessageConverter {
        public JczyMessageConverter() {
            List<MediaType> mediaTypes = new ArrayList<>();
            mediaTypes.add(MediaType.valueOf(MediaType.TEXT_PLAIN_VALUE+";charset=UTF-8"));
            setSupportedMediaTypes(mediaTypes);
        }
    }

    public ObjectFactory<HttpMessageConverters> feignHttpMessageConverter(){
        final HttpMessageConverters httpMessageConverters=new HttpMessageConverters(new JczyMessageConverter());
        return new ObjectFactory<HttpMessageConverters>() {
            @Override
            public HttpMessageConverters getObject() throws BeansException {
                return httpMessageConverters;
            }
        };
    }

    @Bean
    public Decoder feignDecode(){
//        FeignClientConfigurer converter=new FeignClientConfigurer();
//        ObjectFactory<HttpMessageConverters> objectFactory=()->new HttpMessageConverters(converter);
//        return new SpringDecoder(objectFactory);
        return new ResponseEntityDecoder(new SpringDecoder(feignHttpMessageConverter()));
    }
}
