package com.hendisantika.springbootresilience4jdemo;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

@SpringBootApplication
@Log4j2
public class SpringbootResilience4jDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootResilience4jDemoApplication.class, args);
    }

    @Service
    class CircuitBreakerTestService {

        public void timeout() {
            log.info("Entering in service ...");

            throw new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT);
        }

        @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "serviceA")
        public void timeoutWithCircuitBreaker() {
            timeout();
        }
    }
}
