package com.inventory.scheduler;

import com.inventory.repository.BusinessRepository;
import com.inventory.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PredictionRefreshJob {

    private final BusinessRepository businessRepository;
    private final PredictionService predictionService;

    // Runs once a day at 2 AM server time. Also safe to trigger manually via
    // POST /api/predictions/generate for demo purposes.
    @Scheduled(cron = "0 0 2 * * *")
    public void refreshAllPredictions() {
        businessRepository.findAll().forEach(business -> {
            try {
                predictionService.generateForBusiness(business.getId(), 30);
                log.info("Refreshed demand predictions for business {}", business.getId());
            } catch (Exception e) {
                log.error("Failed to refresh predictions for business {}", business.getId(), e);
            }
        });
    }
}
