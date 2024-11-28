package ru.roe.pff.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.roe.pff.enums.FileRequestType;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
public class FileRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    private FeedFile file;

    @OneToMany
    private List<FileError> errors;

    @Column
    @Enumerated(value = EnumType.STRING)
    private FileRequestType type;
}
