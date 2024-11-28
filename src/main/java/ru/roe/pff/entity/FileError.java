package ru.roe.pff.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.roe.pff.enums.ErrorType;

import java.util.UUID;

@NoArgsConstructor
@Entity
@Data
public class FileError {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private FileRequest fileRequest;

    @Column(nullable = false)
    private String error;

    @Column
    @Enumerated(value = EnumType.STRING)
    private ErrorType errorType;

    @Column(nullable = false)
    private Integer rowIndex;

    @Column(nullable = false)
    private Integer columnIndex;
}
