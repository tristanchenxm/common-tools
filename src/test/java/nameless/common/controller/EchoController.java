package nameless.common.controller;

import nameless.common.dto.Echo;
import nameless.common.service.EchoService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class EchoController implements EchoService {
    @Override
    public Map<String, Object> getEcho(int code, String message) {
        return new HashMap<String, Object>() {{
            put("code", code);
            put("message", message);
        }};
    }

    @Override
    public Map<String, Object> postEcho(@RequestBody Echo input) {
        return new HashMap<String, Object>() {{
            put("code", input.getCode());
            put("message", input.getMessage());
        }};
    }
}
