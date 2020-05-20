package nameless.common.config;

import feign.Client;
import feign.Feign;
import feign.Retryer;
import nameless.common.feign.InvocationHandlerFactoryImpl;
import nameless.common.feign.LoggingClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class FeignConfig {
    @Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder() {
        return Feign.builder()
                .client(feignClient())
                .invocationHandlerFactory(new InvocationHandlerFactoryImpl("test")).retryer(new Retryer.Default());
    }

    @Bean
    public Client feignClient() {
        return new LoggingClient(null, null, request -> true);
    }
}
