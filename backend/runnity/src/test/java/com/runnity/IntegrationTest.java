package com.runnity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * ✅ MySQL 연결 테스트
     */
    @Test
    void testDatabaseConnection() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2); // 2초 타임아웃
            System.out.println("✅ MySQL 연결 성공: " + connection.getMetaData().getURL());
            assertThat(valid).isTrue();
        }
    }


    /**
     * ✅ Redis 연결 테스트
     */
    @Test
    void testRedisConnection() {
        String ping = redisTemplate.getConnectionFactory().getConnection().ping();
        System.out.println("✅ Redis 연결 성공: " + ping);
        assertThat(ping).isEqualToIgnoringCase("PONG");
    }
}
