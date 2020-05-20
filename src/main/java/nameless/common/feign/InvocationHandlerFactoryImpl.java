package nameless.common.feign;

import feign.InvocationHandlerFactory;
import feign.Target;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * example:
 * <pre> {@code
 *     {@literal @}Bean
 *     {@literal @}Scope("prototype")
 *     public Feign.Builder feignBuilder(Retryer retryer, {@literal @}Value("environment") String environment) {
 *         return Feign.builder().invocationHandlerFactory(new InvocationHandlerFactoryImpl(environment)).retryer(retryer);
 *     }
 * }
 * </pre>
 */
public class InvocationHandlerFactoryImpl implements InvocationHandlerFactory {
    private final String environment;

    public InvocationHandlerFactoryImpl(String environment) {
        this.environment = environment;
    }
    @Override
    public InvocationHandler create(Target target, Map<Method, MethodHandler> dispatch) {
        return new FeignInvocationHandler(target, dispatch, environment);
    }
}
