package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "scenario_actions")
public class ScenarioAction {
    @EmbeddedId
    private ScenarioActionId id;

    @ManyToOne
    @MapsId("scenarioId")
    @JoinColumn(name = "scenario_id")
    @NotNull
    private Scenario scenario;

    @ManyToOne
    @MapsId("sensorId")
    @JoinColumn(name = "sensor_id")
    @NotNull
    private Sensor sensor;

    @ManyToOne
    @MapsId("actionId")
    @JoinColumn(name = "action_id")
    @NotNull
    private Action action;
}
