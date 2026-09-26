package com.cybAlert.cybAlert.business.incident;

import com.cybAlert.cybAlert.business.audit.AuditService;
import com.cybAlert.cybAlert.business.alert.AlertEntity;
import com.cybAlert.cybAlert.business.user.UserRepository;
import com.cybAlert.cybAlert.infrastructure.alert.SpringDataAlertRepository;
import com.cybAlert.cybAlert.infrastructure.incident.SpringDataIncidentCommentRepository;
import com.cybAlert.cybAlert.infrastructure.incident.SpringDataIncidentHistoryRepository;
import com.cybAlert.cybAlert.infrastructure.incident.SpringDataIncidentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class IncidentService {

    private final SpringDataIncidentRepository incidents;
    private final SpringDataAlertRepository alerts;
    private final SpringDataIncidentCommentRepository comments;
    private final SpringDataIncidentHistoryRepository history;
    private final UserRepository users;
    private final AuditService audit;

    public IncidentService(SpringDataIncidentRepository incidents,
                           SpringDataAlertRepository alerts,
                           SpringDataIncidentCommentRepository comments,
                           SpringDataIncidentHistoryRepository history,
                           UserRepository users, AuditService audit) {
        this.incidents = incidents;
        this.alerts = alerts;
        this.comments = comments;
        this.history = history;
        this.users = users;
        this.audit = audit;
    }

    @Transactional
    public IncidentEntity create(UUID actorId, String title, String description,
                                 AlertEntity.Severity severity, IncidentEntity.Priority priority,
                                 Set<UUID> alertIds) {
        List<AlertEntity> found = alerts.findAllById(alertIds);
        if (found.size() != alertIds.size() || found.isEmpty()) {
            throw new IllegalArgumentException("Toutes les alertes doivent exister");
        }
        IncidentEntity incident = incidents.save(new IncidentEntity(actorId, title, description,
                severity, priority, new HashSet<>(found)));
        history.save(new IncidentHistoryEntity(incident.getId(), actorId, "CREATED", title));
        audit.record(actorId, "INCIDENT_CREATED", "INCIDENT", incident.getId().toString());
        return incident;
    }

    public IncidentEntity findById(UUID id) {
        return incidents.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incident introuvable : " + id));
    }

    public Page<IncidentEntity> findAll(IncidentEntity.Status status, Pageable pageable) {
        return status == null ? incidents.findAll(pageable) : incidents.findByStatus(status, pageable);
    }

    @Transactional
    public IncidentEntity changeStatus(UUID id, UUID actorId, IncidentEntity.Status status) {
        IncidentEntity incident = findById(id);
        incident.changeStatus(status);
        history.save(new IncidentHistoryEntity(id, actorId, "STATUS_CHANGED", status.name()));
        audit.record(actorId, "INCIDENT_STATUS_CHANGED", "INCIDENT", id.toString());
        return incidents.save(incident);
    }

    @Transactional
    public IncidentEntity assign(UUID id, UUID actorId, UUID assigneeId) {
        if (users.findById(assigneeId).isEmpty()) {
            throw new IllegalArgumentException("Utilisateur introuvable : " + assigneeId);
        }
        IncidentEntity incident = findById(id);
        incident.assign(assigneeId);
        history.save(new IncidentHistoryEntity(id, actorId, "ASSIGNED", assigneeId.toString()));
        audit.record(actorId, "INCIDENT_ASSIGNED", "INCIDENT", id.toString());
        return incidents.save(incident);
    }

    @Transactional
    public IncidentCommentEntity comment(UUID id, UUID actorId, String content) {
        findById(id);
        IncidentCommentEntity comment = comments.save(
                new IncidentCommentEntity(id, actorId, content));
        history.save(new IncidentHistoryEntity(id, actorId, "COMMENTED", "Commentaire ajouté"));
        audit.record(actorId, "INCIDENT_COMMENTED", "INCIDENT", id.toString());
        return comment;
    }

    public List<IncidentCommentEntity> comments(UUID id) {
        findById(id);
        return comments.findByIncidentIdOrderByCreatedAtAsc(id);
    }

    public List<IncidentHistoryEntity> history(UUID id) {
        findById(id);
        return history.findByIncidentIdOrderByCreatedAtAsc(id);
    }
}
