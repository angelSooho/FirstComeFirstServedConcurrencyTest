package com.example.firstcomefirstservedconcurrencytest.service;

import com.example.firstcomefirstservedconcurrencytest.IntegrationTestSupport;
import com.example.firstcomefirstservedconcurrencytest.exception.CouponStockOverException;
import com.example.firstcomefirstservedconcurrencytest.repository.CouponRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ApplyServiceTest extends IntegrationTestSupport {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.delete("coupon_count");
        redisTemplate.delete("applied_user");
    }

    @AfterEach
    void tearDown() {
        couponRepository.deleteAllInBatch();
    }

    @DisplayName("쿠폰을 1번 응모한다.")
    @Test
    void applyCouponJustOne() throws Exception {
        //when
        int couponStock = 100;
        applyService.apply(1L, couponStock);

        Thread.sleep(2000);

        //then
        long count = couponRepository.count();
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("쿠폰을 동시에 1000명이 응모한다. 그러나 쿠폰이 100개이므로 100개가 응모되고 900개는 실패한다.")
    @Test
    void applyCouponsSameTime() throws Exception {
        //when
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);

        int couponStock = 100;
        for (int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.execute(() -> {
                try {
                    applyService.apply(userId, couponStock);
                } catch (CouponStockOverException e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Thread.sleep(2000);

        //then
        long count = couponRepository.count();
        assertThat(count).isEqualTo(couponStock);
        assertThat(errorCount.get()).isEqualTo(threadCount - couponStock);
    }

    @DisplayName("쿠폰을 동시에 1000명이 응모한다. 쿠폰은 1번만 응모할 수 있다.")
    @Test
    void applyCouponsSameTimeOnlyOne() throws Exception {
        //when
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        int couponStock = 100;
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    applyService.apply(1L, couponStock);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Thread.sleep(2000);

        //then
        long count = couponRepository.count();
        assertThat(count).isEqualTo(1);
    }
}