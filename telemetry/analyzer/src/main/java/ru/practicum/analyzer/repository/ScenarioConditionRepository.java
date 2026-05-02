package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.model.ScenarioCondition;
import ru.practicum.analyzer.model.ScenarioConditionId;

public interface ScenarioConditionRepository extends JpaRepository<ScenarioCondition, ScenarioConditionId> {
    @Modifying
    @Query(value = "DELETE FROM ScenarioCondition sc WHERE sc.scenario.id = :scenarioId")
    void deleteByIdScenarioId(@Param("scenarioId") Long scenarioId);


}
