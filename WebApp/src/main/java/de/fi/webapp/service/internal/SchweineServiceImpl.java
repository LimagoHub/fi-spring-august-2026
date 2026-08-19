package de.fi.webapp.service.internal;

import de.fi.webapp.persistence.repository.SchweinRepository;
import de.fi.webapp.service.SchweineService;
import de.fi.webapp.service.exception.AlreadyExistsException;
import de.fi.webapp.service.exception.NotFoundException;
import de.fi.webapp.service.exception.SchweineServiceException;
import de.fi.webapp.service.mapper.SchweinMapper;
import de.fi.webapp.service.model.Schwein;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SchweineServiceImpl implements SchweineService {

    private final SchweinRepository repo;
    private final SchweinMapper mapper;

    @Override
    public void speichern(final Schwein schwein) {
        try {
            if (schwein == null) throw new SchweineServiceException("Schwein darf nicht null sein");
            if (repo.existsById(schwein.getId())) throw new AlreadyExistsException("Schwein existiert bereits");
            repo.save(mapper.convert(schwein));
        } catch (AlreadyExistsException | SchweineServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new SchweineServiceException("Fehler beim Speichern", e);
        }
    }

    @Override
    public void aendern(final Schwein schwein) {
        try {
            if (schwein == null) throw new SchweineServiceException("Schwein darf nicht null sein");
            if (! repo.existsById(schwein.getId())) throw new NotFoundException("Schwein konnte nicht gefunden werden");
            repo.save(mapper.convert(schwein));
        } catch (NotFoundException | SchweineServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new SchweineServiceException("Fehler beim Aendern", e);
        }
    }

    @Override
    public void loeschen(final UUID uuid) {
        try {
            if (! repo.existsById(uuid)) throw new NotFoundException("Schwein konnte nicht gefunden werden");
            repo.deleteById(uuid);
        } catch (NotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new SchweineServiceException("Fehler beim Loeschen", e);
        }
    }

    @Override
    public Optional<Schwein> findeNachId(final UUID uuid) {
        try {
            return repo.findById(uuid).map(mapper::convert);
        } catch (RuntimeException e) {
            throw new SchweineServiceException("Fehler beim Suchen", e);
        }
    }

    @Override
    public Iterable<Schwein> findeAlle() {
        try {
            return mapper.convert(repo.findAll());
        } catch (RuntimeException e) {
            throw new SchweineServiceException("Fehler beim Suchen", e);
        }
    }

    @Override
    public void fuettern(final UUID uuid) {
        try {
            Schwein schwein = repo.findById(uuid)
                    .map(mapper::convert)
                    .orElseThrow(() -> new NotFoundException("Schwein konnte nicht gefunden werden"));
            schwein.fuettern();
            repo.save(mapper.convert(schwein));
        } catch (NotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new SchweineServiceException("Fehler beim Fuettern", e);
        }
    }
}
