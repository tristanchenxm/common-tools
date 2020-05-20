package nameless.common.service;

import nameless.common.dto.Echo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;


public interface EchoService {
    @GetMapping("/get")
    Map<String, Object> getEcho(@RequestParam("code") int code, @RequestParam("message") String message);

    @PostMapping("/post")
    Map<String, Object> postEcho(@RequestBody Echo input);
}
