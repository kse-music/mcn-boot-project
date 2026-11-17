package cn.hiboot.mcn.docs;

import cn.hiboot.mcn.core.model.result.RestResp;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "示例接口")
public class OpenApiExample {
    @GetMapping("/api/greeting")
    @Operation(summary = "读取欢迎信息")
    public RestResp<String> greeting() {
        return RestResp.ok("Hello, MCN Boot!");
    }

    @Hidden
    @GetMapping("/api/internal-example")
    public RestResp<String> hiddenFromDocs() {
        return RestResp.ok("hidden");
    }
}
