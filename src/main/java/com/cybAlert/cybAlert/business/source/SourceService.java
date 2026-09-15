package com.cybAlert.cybAlert.business.source;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SourceService {

    private final SourceRepository repository;

    public SourceService(SourceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SourceEntity create(String hostname, String ipAddress, String macAddress,
                               String operatingSystem, SourceEntity.Type type,
                               String environment) {
        if (repository.existsByHostname(hostname.strip())) {
            throw new SourceAlreadyExistsException();
        }
        return repository.save(new SourceEntity(hostname.strip(), ipAddress.strip(), macAddress,
                operatingSystem, type, environment.strip()));
    }

    public SourceEntity findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new SourceNotFoundException(id));
    }

    public Page<SourceEntity> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional
    public SourceEntity update(UUID id, String hostname, String ipAddress, String macAddress,
                               String operatingSystem, SourceEntity.Type type,
                               String environment) {
        SourceEntity source = findById(id);
        if (!source.getHostname().equalsIgnoreCase(hostname)
                && repository.existsByHostname(hostname.strip())) {
            throw new SourceAlreadyExistsException();
        }
        source.update(hostname.strip(), ipAddress.strip(), macAddress, operatingSystem,
                type, environment.strip());
        return repository.save(source);
    }

    @Transactional
    public SourceEntity heartbeat(UUID id) {
        SourceEntity source = findById(id);
        source.heartbeat();
        return repository.save(source);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(findById(id));
    }

    public static class SourceNotFoundException extends IllegalArgumentException {
        public SourceNotFoundException(UUID id) { super("Source introuvable : " + id); }
    }

    public static class SourceAlreadyExistsException extends IllegalArgumentException {
        public SourceAlreadyExistsException() { super("Ce nom de source est déjà utilisé"); }
    }
}
