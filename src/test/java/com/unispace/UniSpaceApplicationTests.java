package com.unispace;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UniSpaceApplicationTests {

    @Test
    void contextLoads() {
        // 스프링 컨텍스트가 정상적으로 로드되는지 검증 (CI 에서 H2 로 실행)
    }
}
