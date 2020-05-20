package nameless.common.feign;

import feign.Request;

@FunctionalInterface
public interface HttpLoggable {
    boolean loggable(Request request);
}
