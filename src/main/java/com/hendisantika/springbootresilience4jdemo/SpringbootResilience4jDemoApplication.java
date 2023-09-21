package com.hendisantika.springbootresilience4jdemo;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.util.function.Consumer;
import java.util.stream.IntStream;

@SpringBootApplication
@Log4j2
public class SpringbootResilience4jDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootResilience4jDemoApplication.class, args);
    }

    private static final Consumer<Runnable> consumer = runnable -> IntStream.range(0, 5).forEach(value -> {
        try {
            runnable.run();
//        } catch (CircuitBreakerOpenException e) {
        } catch (CallNotPermittedException e) {
            log.warn("Circuit breaker applied");
        } catch (Exception e) {
            log.warn("Exception in method");
        }
    });

    @Bean
    CircuitBreaker defaultCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker("default");
    }

    @Bean
    ApplicationRunner applicationRunner(CircuitBreakerTestService circuitBreakerTestService,
                                        CircuitBreaker defaultCircuitBreaker) {
        return applicationArguments -> {

            log.info("Running without circuit breaker ...");
            consumer.accept(() -> circuitBreakerTestService.timeout());

            log.info("Running with circuit breaker using annotations ...");
            consumer.accept(() -> circuitBreakerTestService.timeoutWithCircuitBreaker());

            log.info("Running with default circuit breaker using manual invocation ...");
            Runnable decoratedRunnable = CircuitBreaker.decorateRunnable(defaultCircuitBreaker,
                    () -> circuitBreakerTestService.timeout());
            consumer.accept(() -> decoratedRunnable.run());
        };
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
