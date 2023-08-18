package com.example.firstcomefirstservedconcurrencytest.service;

import com.example.firstcomefirstservedconcurrencytest.exception.CouponAlreadySaveException;
import com.example.firstcomefirstservedconcurrencytest.exception.CouponStockOverException;
import com.example.firstcomefirstservedconcurrencytest.prducer.CouponCreateProducer;
import com.example.firstcomefirstservedconcurrencytest.repository.AppliedUserRepository;
import com.example.firstcomefirstservedconcurrencytest.repository.CouponCountRepository;
import com.example.firstcomefirstservedconcurrencytest.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ApplyService {

    private final CouponRepository couponRepository;

    private final CouponCountRepository couponCountRepository;

    private final CouponCreateProducer couponCreateProducer;

    private final AppliedUserRepository appliedUserRepository;

    public void apply(Long userId, int couponStock) {
        Long apply = appliedUserRepository.add(userId);

        if (apply != 1) {
            throw new CouponAlreadySaveException("이미 응모한 사용자입니다.");
        }

        long count = couponCountRepository.increase();

        if (count > couponStock) {
            throw new CouponStockOverException("쿠폰이 모두 소진되었습니다.");
        }

        couponCreateProducer.create(userId);
    }
}
