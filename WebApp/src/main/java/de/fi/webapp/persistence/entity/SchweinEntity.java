package de.fi.webapp.persistence.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity

public class SchweinEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false)
    private int gewicht;
}
