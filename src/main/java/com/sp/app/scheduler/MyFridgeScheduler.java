package com.sp.app.scheduler;

import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.repository.MemberFridgeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyFridgeScheduler {

    private final MemberFridgeRepository memberFridgeRepository;

    //매일 새벽 3시에 만료 1주일 이상 지난 재료 자동 삭제(cron 초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void autoDeleteOldExpiredFridgeItems() {
        // 오늘 날짜에서 7일을 뺀 기준일 계산 (예: 오늘이 10일이면 3일 이전 데이터 대상)
        LocalDate thresholdDate = LocalDate.now().minusDays(7);

        int deletedCount = memberFridgeRepository.deleteExpiredItemsOlderThanWeek(thresholdDate);

        if (deletedCount > 0) {
            log.info("[스케줄러] 유통기한 1주일(7일) 이상 경과 재료 자동 삭제 완료. 기준일: {}, 삭제 건수: {}건", 
                     thresholdDate, deletedCount);
        }
    }
}