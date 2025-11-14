package org.buddhi.jmeterwrapper.repository;

import org.buddhi.jmeterwrapper.model.TestExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestExecutionRepository extends JpaRepository<TestExecution, Long> {
    List<TestExecution> findByStatus(String status);
    Optional<TestExecution> findByPid(Long pid);
    List<TestExecution> findAllByOrderByStartTimeDesc();
}