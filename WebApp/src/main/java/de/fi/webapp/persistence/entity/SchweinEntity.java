package de.fi.webapp.persistence.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

// JPA- Annotationen ergaenzen

public class SchweinEntity {

    private UUID id;
    private String name;
    private int gewicht;
}
