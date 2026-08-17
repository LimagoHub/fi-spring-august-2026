package de.fi.webapp.presentation.controller;

import de.fi.webapp.presentation.dto.PersonDto;
import de.fi.webapp.presentation.exception.IdMismatchException;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/personen")
public class PersonenController {


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
    public ResponseEntity<PersonDto> getPerson(@PathVariable UUID id) {
        if(id.toString().endsWith("1")){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(PersonDto.builder().id(id).vorname("Max").nachname("Mustermann").build());
    }

    @GetMapping(path="", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Iterable<PersonDto>> getPersonen(
            @RequestParam(required = false, defaultValue = "Fritz") String vorname,
            @RequestParam(required = false, defaultValue = "Schmitt")String nachname
    ) {
        System.out.printf("Vorname = %s, Nachname = %s\n", vorname, nachname);
        var list = List.of(
            PersonDto.builder().id(UUID.randomUUID()).vorname("John").nachname("Doe").build()
                ,PersonDto.builder().id(UUID.randomUUID()).vorname("John").nachname("Rambo").build()
                ,PersonDto.builder().id(UUID.randomUUID()).vorname("John").nachname("McClaine").build()
                ,PersonDto.builder().id(UUID.randomUUID()).vorname("John").nachname("Wayne").build()
                ,PersonDto.builder().id(UUID.randomUUID()).vorname("John").nachname("Wick").build()
                ,PersonDto.builder().id(UUID.randomUUID()).vorname("John Boy").nachname("Walton").build()
        );

        return ResponseEntity.ok(list);
    }

    @DeleteMapping(path="/{id}")
    public ResponseEntity<Void> loeschePerson(@PathVariable UUID id){
        if(id.toString().endsWith("1")){
            return ResponseEntity.notFound().build();
        }
        System.out.println("Person mit der ID: " + id + " wurde gelöscht!");
        return ResponseEntity.ok().build();

    }

    @PostMapping(path="",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> einfuegen(@Valid @RequestBody PersonDto personDto, UriComponentsBuilder uriBuilder) {
        UriComponents uriComponents = uriBuilder.path("/v1/personen/{id}").buildAndExpand(personDto.getId());
        return ResponseEntity.created(uriComponents.toUri()).build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> update(@PathVariable UUID id,@Valid @RequestBody PersonDto personDto){
        if( id.equals(personDto.getId()) ) throw new IdMismatchException("Upps");
        return ResponseEntity.ok().build();
    }
}
