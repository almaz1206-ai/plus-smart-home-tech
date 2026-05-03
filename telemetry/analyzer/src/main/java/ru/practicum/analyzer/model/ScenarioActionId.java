package ru.practicum.analyzer.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ScenarioActionId implements Serializable {
    @NotNull
    private Long scenarioId;

    @NotNull
    private String sensorId;

    @NotNull
    private Long actionId;
}
