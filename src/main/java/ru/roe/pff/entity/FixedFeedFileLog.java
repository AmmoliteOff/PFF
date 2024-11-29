package ru.roe.pff.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
public class FixedFeedFileLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(cascade = CascadeType.ALL)
    private FeedFile feedFile;

    @CreationTimestamp
    @Column(nullable = false)
    private Instant invokedAt;

    public FixedFeedFileLog(FeedFile feedFile) {
        this.feedFile = feedFile;
    }
}
