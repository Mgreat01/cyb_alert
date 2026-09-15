package com.cybAlert.cybAlert.business.source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SourceServiceTests {

    private SourceRepository repository;
    private SourceService service;

    @BeforeEach
    void setUp() {
        repository = mock(SourceRepository.class);
        service = new SourceService(repository);
        when(repository.save(any(SourceEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsAndNormalizesASource() {
        SourceEntity source = service.create(" server-01 ", " 10.0.0.1 ", null,
                "Linux", SourceEntity.Type.SERVER, " production ");

        assertThat(source.getHostname()).isEqualTo("server-01");
        assertThat(source.getIpAddress()).isEqualTo("10.0.0.1");
        assertThat(source.getStatus()).isEqualTo(SourceEntity.Status.UNKNOWN);
    }

    @Test
    void marksASourceOnlineOnHeartbeat() {
        UUID id = UUID.randomUUID();
        SourceEntity source = new SourceEntity("server-01", "10.0.0.1", null,
                "Linux", SourceEntity.Type.SERVER, "production");
        when(repository.findById(id)).thenReturn(Optional.of(source));

        service.heartbeat(id);

        assertThat(source.getStatus()).isEqualTo(SourceEntity.Status.ONLINE);
        assertThat(source.getLastSeenAt()).isNotNull();
    }

    @Test
    void rejectsADuplicateHostname() {
        when(repository.existsByHostname("server-01")).thenReturn(true);

        assertThatThrownBy(() -> service.create("server-01", "10.0.0.1", null,
                "Linux", SourceEntity.Type.SERVER, "production"))
                .isInstanceOf(SourceService.SourceAlreadyExistsException.class);
    }
}
