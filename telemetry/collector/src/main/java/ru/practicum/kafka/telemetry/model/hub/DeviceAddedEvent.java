package ru.practicum.kafka.telemetry.model.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.kafka.telemetry.model.hub.enums.DeviceType;
import ru.practicum.kafka.telemetry.model.hub.enums.HubEventType;

@Getter
@Setter
@ToString(callSuper = true)
public class DeviceAddedEvent extends HubEvent {
    @NotBlank
    String id;

    @NotNull
    DeviceType deviceType;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED_EVENT;
    }
}
