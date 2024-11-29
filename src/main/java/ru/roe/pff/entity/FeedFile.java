package ru.roe.pff.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@EqualsAndHashCode
public class FeedFile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private Integer rowsCount;

    @CreationTimestamp
    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private String link;

    public FeedFile(String fileName, Integer rowsCount, String link) {
        this.fileName = fileName;
        this.rowsCount = rowsCount;
        this.link = link;
    }
}
