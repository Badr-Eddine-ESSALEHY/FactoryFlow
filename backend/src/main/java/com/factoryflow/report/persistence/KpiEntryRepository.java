package com.factoryflow.report.persistence;
import com.factoryflow.report.domain.KpiEntry;
import org.springframework.data.jpa.repository.JpaRepository;
public interface KpiEntryRepository extends JpaRepository<KpiEntry, Long> { }
