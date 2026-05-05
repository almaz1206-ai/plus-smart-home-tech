package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.ScenarioAction;
import ru.practicum.analyzer.model.ScenarioActionId;

public interface ScenarioActionRepository extends JpaRepository<ScenarioAction, ScenarioActionId> {
    @Modifying
    @Transactional
    @Query("DELETE FROM ScenarioAction sa WHERE sa.scenario.id = :scenarioId")
    void deleteByIdScenarioId(@Param("scenarioId") Long scenarioId);
}
