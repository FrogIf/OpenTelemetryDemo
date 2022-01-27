package sch.frog.opentelemetry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sch.frog.opentelemetry.model.TestData;

public interface TestDataRepository extends JpaRepository<TestData, Integer> {
    
}
