package ru.roe.pff.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Entity
@Data
@AllArgsConstructor
public class ErrorSolve {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String value;
}
