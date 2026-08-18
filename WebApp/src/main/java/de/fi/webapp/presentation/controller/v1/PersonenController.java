package de.fi.webapp.presentation.controller.v1;

import ch.qos.logback.core.encoder.EchoEncoder;
import de.fi.webapp.persistence.repository.PersonenRepository;
import de.fi.webapp.presentation.dto.PersonDto;
import de.fi.webapp.presentation.exception.IdMismatchException;
import de.fi.webapp.presentation.mapper.PersonDtoMapper;
import de.fi.webapp.service.PersonenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/personen")
@RequiredArgsConstructor
public class PersonenController {

    private final PersonenService service;
    private final PersonDtoMapper mapper;

   /* @Operation(summary = "Liefert eine Person")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Person gefunden",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PersonDto.class)) }),
            @ApiResponse(responseCode = "400", description = "ungueltige ID",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Person nicht gefunden",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "internal server error",
                    content = @Content)})
*/

    @GetMapping(path="/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<PersonDto> getPerson(@PathVariable UUID id) throws Exception{
      return ResponseEntity.of(service.findeNachId(id).map(mapper::convert));
    }

    @GetMapping(path="", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Iterable<PersonDto>> getPersonen(
            @RequestParam(required = false, defaultValue = "Fritz") String vorname,
            @RequestParam(required = false, defaultValue = "Schmitt")String nachname
    ) throws Exception{
        System.out.printf("Vorname = %s, Nachname = %s\n", vorname, nachname);

        return ResponseEntity.ok(mapper.convert(service.findeAlle()));
    }

    @DeleteMapping(path="/{id}")
    public ResponseEntity<Void> loeschePerson(@PathVariable UUID id) throws Exception{
        service.loeschen(id);
        return ResponseEntity.ok().build();

    }

    @PostMapping(path="",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> einfuegen(@Valid @RequestBody PersonDto personDto, UriComponentsBuilder uriBuilder) throws Exception {
        service.speichern(mapper.convert(personDto));
        UriComponents uriComponents = uriBuilder.path("/v1/personen/{id}").buildAndExpand(personDto.getId());
        return ResponseEntity.created(uriComponents.toUri()).build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> update(@PathVariable UUID id,@Valid @RequestBody PersonDto personDto) throws Exception{
        if (! id.equals(personDto.getId())) throw new IdMismatchException("ID mismatch");
        service.aendern(mapper.convert(personDto));
        return ResponseEntity.ok().build();
    }
}
