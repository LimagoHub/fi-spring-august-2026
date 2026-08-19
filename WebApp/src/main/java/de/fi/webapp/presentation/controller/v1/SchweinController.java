package de.fi.webapp.presentation.controller.v1;

import de.fi.webapp.aspect.Benchmark;
import de.fi.webapp.presentation.dto.SchweinDto;
import de.fi.webapp.presentation.exception.IdMismatchException;
import de.fi.webapp.presentation.mapper.SchweinDtoMapper;
import de.fi.webapp.service.SchweineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/v1/schweine")
@RequiredArgsConstructor
@Benchmark
public class SchweinController {

    private final SchweineService service;
    private final SchweinDtoMapper mapper;

    @GetMapping(path="/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<SchweinDto> getSchwein(@PathVariable UUID id) {
        return ResponseEntity.of(service.findeNachId(id).map(mapper::convert));
    }

    @GetMapping(path="", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Iterable<SchweinDto>> getSchweine() {
        return ResponseEntity.ok(mapper.convert(service.findeAlle()));
    }

    @DeleteMapping(path="/{id}")
    public ResponseEntity<Void> loescheSchwein(@PathVariable UUID id) {
        service.loeschen(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path="",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> einfuegen(@Valid @RequestBody SchweinDto schweinDto, UriComponentsBuilder uriBuilder) {
        service.speichern(mapper.convert(schweinDto));
        UriComponents uriComponents = uriBuilder.path("/v1/schweine/{id}").buildAndExpand(schweinDto.getId());
        return ResponseEntity.created(uriComponents.toUri()).build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> update(@PathVariable UUID id, @Valid @RequestBody SchweinDto schweinDto) {
        if (! id.equals(schweinDto.getId())) throw new IdMismatchException("ID mismatch");
        service.aendern(mapper.convert(schweinDto));
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/{id}/fuetterungen")
    public ResponseEntity<Void> fuettern(@PathVariable UUID id) {
        service.fuettern(id);
        return ResponseEntity.ok().build();
    }
}
