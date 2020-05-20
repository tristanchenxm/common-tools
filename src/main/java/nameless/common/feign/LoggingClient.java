package nameless.common.feign;

import feign.Client;
import feign.Request;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import static feign.Util.UTF_8;

/**
 * Feign Client implementation. Log http request and response
 *
 * <pre> {@code
 *     @Bean
 *     public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory,
 *                               SpringClientFactory clientFactory) {
 *         return new LoadBalancerFeignClient(new LoggingClient(null, null, request -> true),
 *                 cachingFactory, clientFactory);
 *     }
 * }
 * </pre>
 */
public class LoggingClient extends Client.Default {
    private static final Logger log = LoggerFactory.getLogger(LoggingClient.class);
    private static final int MAX_LOGGING_SIZE = 1000;
    private final HttpLoggable loggableSetting;

    public LoggingClient(SSLSocketFactory sslContextFactory, HostnameVerifier hostnameVerifier, HttpLoggable loggableSetting) {
        super(sslContextFactory, hostnameVerifier);
        this.loggableSetting = loggableSetting;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        if (!loggableSetting.loggable(request)) {
            return super.execute(request, options);
        }

        List<String> logMsg = new LinkedList<>();
        logMsg.add(request.httpMethod().name());
        logMsg.add(request.url());
        if (request.httpMethod() != Request.HttpMethod.GET) {
            logMsg.add("-d");
            byte[] bodyByte = request.body();
            if (loggabel(request.headers()) && bodyByte != null && bodyByte.length > 0) {
                String body = new String(bodyByte);
                if (body.length() > MAX_LOGGING_SIZE) {
                    logMsg.add(body.substring(0, MAX_LOGGING_SIZE) + "...");
                } else {
                    logMsg.add(body);
                }
            }
        }

        try {
            Response response = super.execute(request, options);
            logMsg.add("***RESPONSE***");
            logMsg.add(Integer.toString(response.status()));
            if (response.reason() != null) {
                logMsg.add(response.reason());
            }
            if (loggabel(response.headers())) {
                Response.Body body = response.body();
                if (body.isRepeatable()) {
                    String bodyString = body.toString();
                    if (bodyString.length() > MAX_LOGGING_SIZE) {
                        logMsg.add(bodyString.substring(0, MAX_LOGGING_SIZE) + "...");
                    } else {
                        logMsg.add(bodyString);
                    }
                } else {
                    byte[] bytes = StreamUtils.copyToByteArray(body.asInputStream());
                    Response originalResponse = response;
                    response = Response.builder()
                            .status(response.status())
                            .reason(response.reason())
                            .headers(response.headers())
                            .request(response.request())
                            .body(new InputStreamBody(bytes))
                            .build();
                    originalResponse.close();
                    logMsg.add(new String(bytes));
                }
            }
            log.info(String.join(" ", logMsg));
            return response;
        } catch (Exception e) {
            logMsg.add("Exception: " + e.getMessage());
            log.info(String.join(" ", logMsg));
            throw e;
        }
    }

    private boolean loggabel(Map<String, Collection<String>> headers) {
        List<String> contentTypes = new ArrayList<>();
        Collection<String> contentTypeHeader = headers.get("content-type");
        if (contentTypeHeader != null && contentTypeHeader.size() > 0){
            contentTypes.addAll(contentTypeHeader);
        }

        if (contentTypes.size() == 0) {
            return true;
        }
        for (String ct : contentTypes) {
            if (ct.startsWith("application/json") || ct.startsWith("text")) {
                return true;
            }
        }
        return false;
    }

    private static final class InputStreamBody implements Response.Body {

        private final InputStream inputStream;
        private final Integer length;

        private InputStreamBody(byte[] bytes) {
            this.inputStream = new ByteArrayInputStream(bytes);
            this.length = bytes.length;
        }


        @Override
        public Integer length() {
            return length;
        }

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public InputStream asInputStream() {
            return inputStream;
        }

        @Override
        public Reader asReader() {
            return new InputStreamReader(inputStream, UTF_8);
        }

        public Reader asReader(Charset charset) {
            return new InputStreamReader(inputStream, charset);
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }
    }
}
