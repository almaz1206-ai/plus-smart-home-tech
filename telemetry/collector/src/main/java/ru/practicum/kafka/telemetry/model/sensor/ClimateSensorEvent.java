package ru.practicum.kafka.telemetry.model.sensor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ClimateSensorEvent extends SensorEvent {
    @NotNull
    Integer temperatureC;

    @NotNull
    Integer humidity;

    @NotNull
    Integer co2Level;

    @Override
    public SensorEventType getType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}
