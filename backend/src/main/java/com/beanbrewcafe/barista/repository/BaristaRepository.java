package com.beanbrewcafe.barista.repository;

import com.beanbrewcafe.barista.model.Barista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BaristaRepository extends JpaRepository<Barista, Long> {

    List<Barista> findByStatus(Barista.BaristaStatus status);

    @Query("SELECT b FROM Barista b WHERE b.status = 'AVAILABLE' OR b.status = 'BUSY' ORDER BY b.currentWorkload ASC")
    List<Barista> findActiveBaristasOrderByWorkload();

    @Query("SELECT AVG(b.currentWorkload) FROM Barista b WHERE b.status != 'OFFLINE'")
    Double getAverageWorkload();
}