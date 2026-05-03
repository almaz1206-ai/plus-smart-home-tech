package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.analyzer.model.enums.ConditionOperation;
import ru.practicum.analyzer.model.enums.ConditionType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "conditions")
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConditionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConditionOperation operation;

    @Column
    private Integer value;
}
