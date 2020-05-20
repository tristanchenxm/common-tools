package nameless.common.feign;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.InvocationHandlerFactory;
import feign.Target;
import nameless.common.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static feign.Util.checkNotNull;

/**
 * loggable Feign InvocationHandler. Log input & output of feign interface
 */
public class FeignInvocationHandler implements InvocationHandler {
    private final static Logger logger = LoggerFactory.getLogger(FeignInvocationHandler.class);

    private final Target target;
    private final Map<Method, InvocationHandlerFactory.MethodHandler> dispatch;
    private final String environment;

    FeignInvocationHandler(Target target, Map<Method, InvocationHandlerFactory.MethodHandler> dispatch, String environment) {
        this.target = checkNotNull(target, "target");
        this.dispatch = checkNotNull(dispatch, "dispatch for %s", target);
        this.environment = environment;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("equals".equals(method.getName())) {
            try {
                Object otherHandler = args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
                return equals(otherHandler);
            } catch (IllegalArgumentException e) {
                return false;
            }
        } else if ("hashCode".equals(method.getName())) {
            return hashCode();
        } else if ("toString".equals(method.getName())) {
            return toString();
        }
        Object o = dispatch.get(method).invoke(args);
        logInvocation(proxy, method, args, o);
        return o;
    }

    private void logInvocation(Object proxy, Method method, Object[] args, Object o) throws JsonProcessingException {
        if (!loggable(proxy, method)) {
            return;
        }

        List<String> logMsg = new LinkedList<>();
        // log class and method name
        logMsg.add(method.getDeclaringClass().getName() + "." + method.getName());
        // log parameters
        logParameters(method, args, logMsg);
        logMsg.add("==>");
        // log return value
        logMsg.add(JsonUtil.toString(o));
        logger.info(String.join(" ", logMsg));
    }

    private boolean loggable(Object proxy, Method method) {
        LogInvocation logInvocation = method.getAnnotation(LogInvocation.class);
        if (logInvocation == null) {
            Class<?>[] interfaces = proxy.getClass().getInterfaces();
            for (Class<?> iter : interfaces) {
                logInvocation = iter.getAnnotation(LogInvocation.class);
                if (logInvocation != null) {
                    break;
                }

                for (Class<?> superClass = iter.getSuperclass();
                     superClass != null && logInvocation == null && superClass != method.getDeclaringClass();) {
                    logInvocation = superClass.getAnnotation(LogInvocation.class);
                    superClass = superClass.getSuperclass();
                }
            }
        }
        if (logInvocation == null) {
            return false;
        }
        if (logInvocation.environments().length == 0) {
            return true;
        }
        for (String supportedEnvironment : logInvocation.environments()) {
            if (supportedEnvironment.equals(environment)) {
                return true;
            }
        }
        return false;
    }

    private void logParameters(Method method, Object[] args, List<String> logMsg) throws JsonProcessingException {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Parameter parameter = parameters[i];
            if (arg == null) {
                logMsg.add("null");
            } else if (ClassUtils.isPrimitiveOrWrapper(arg.getClass()) || String.class == arg.getClass()) {
                logMsg.add(arg.toString());
            } else {
                logMsg.add(JsonUtil.toString(arg));
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FeignInvocationHandler) {
            FeignInvocationHandler other = (FeignInvocationHandler) obj;
            return target.equals(other.target);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public String toString() {
        return target.toString();
    }
}
