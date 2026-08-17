package de.fi.webapp.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonDto {

    @NotNull
    private UUID id;

    @NotNull
    @Size(min=2, max=50)
    private String vorname;

    @NotNull
    @Size(min=2, max=50)
    private String nachname;
}
