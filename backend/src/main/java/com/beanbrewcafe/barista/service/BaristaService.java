package com.beanbrewcafe.barista.service;

import com.beanbrewcafe.barista.model.Barista;
import com.beanbrewcafe.barista.repository.BaristaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaristaService {

    private final BaristaRepository baristaRepository;
    private final PriorityQueueService priorityQueueService;

    public List<Barista> getAllBaristas() {
        return baristaRepository.findAll();
    }

    public List<Barista> getAvailableBaristas() {
        return baristaRepository.findByStatus(Barista.BaristaStatus.AVAILABLE);
    }

    @Transactional
    public void assignNextOrderToBarista(Long baristaId) {
        priorityQueueService.assignNextOrder(baristaId);
    }

    @Transactional
    public void setBaristaStatus(Long baristaId, Barista.BaristaStatus status) {
        Barista barista = baristaRepository.findById(baristaId)
                .orElseThrow(() -> new RuntimeException("Barista not found"));

        barista.setStatus(status);
        baristaRepository.save(barista);

        log.info("Set barista {} status to {}", barista.getName(), status);
    }
}