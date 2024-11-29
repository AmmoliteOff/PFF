package ru.roe.pff.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.roe.pff.enums.ErrorType;

import java.util.UUID;

@NoArgsConstructor
@Entity
@Data
@AllArgsConstructor
public class FileError {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(cascade = CascadeType.MERGE)
    private FeedFile feedFile;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @OneToOne(cascade = CascadeType.ALL)
    private ErrorSolve errorSolve;

    @Column
    @Enumerated(value = EnumType.STRING)
    private ErrorType errorType;

    @Column(nullable = false)
    private Integer rowIndex;

    @Column(nullable = false)
    private Integer columnIndex;

    @Column(nullable = false)
    private Boolean suppressed;
}
