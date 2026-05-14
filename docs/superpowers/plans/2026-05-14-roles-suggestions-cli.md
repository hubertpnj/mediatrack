# Roles, Suggestions & CLI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `UserRole` enum + `suspended` flag to `UserAccount`, a JSONB-backed `Suggestion` entity with submit/approve/reject workflow, and a Spring Shell CLI that enforces role boundaries.

**Architecture:** `UserRole` (USER/MODERATOR/ADMIN) replaces the raw `user_role` string in `UserAccount`. A `Suggestion` table stores proposed entity data as JSONB; approval materializes the record into the target table. Spring Shell runs under the `cli` Spring profile — disables web, Redis, Kafka, and OAuth2; requires only PostgreSQL.

**Tech Stack:** Java 25, Spring Boot 3.5, Spring Data JPA, Hibernate 6 (`@JdbcTypeCode` for JSONB), Flyway, Spring Shell 3.3.3, Mockito 5

---

## File Map

### New files
| File | Responsibility |
| --- | --- |
| `backend/src/main/java/hpnj/mediatrack/domain/enums/UserRole.java` | USER / MODERATOR / ADMIN enum |
| `backend/src/main/java/hpnj/mediatrack/domain/enums/SuggestionEntityType.java` | Entity type enum for suggestions |
| `backend/src/main/java/hpnj/mediatrack/domain/enums/SuggestionStatus.java` | PENDING / APPROVED / REJECTED |
| `backend/src/main/java/hpnj/mediatrack/domain/suggestion/Suggestion.java` | Suggestion JPA entity (JSONB proposed_data) |
| `backend/src/main/resources/db/migration/V2__add_role_and_suspended.sql` | Alter user_account columns + create suggestion table |
| `backend/src/main/java/hpnj/mediatrack/repository/UserAccountRepository.java` | JPA repo — findByEmail, existsByEmail |
| `backend/src/main/java/hpnj/mediatrack/repository/MediaRepository.java` | JPA repo for Media |
| `backend/src/main/java/hpnj/mediatrack/repository/PartyRepository.java` | JPA repo for Party |
| `backend/src/main/java/hpnj/mediatrack/repository/MediaGroupRepository.java` | JPA repo for MediaGroup |
| `backend/src/main/java/hpnj/mediatrack/repository/SuggestionRepository.java` | JPA repo — findByStatus |
| `backend/src/main/java/hpnj/mediatrack/config/PasswordEncoderConfig.java` | BCryptPasswordEncoder bean (no profile) |
| `backend/src/main/java/hpnj/mediatrack/service/UserDetailsServiceImpl.java` | UserDetailsService backed by UserAccountRepository |
| `backend/src/main/java/hpnj/mediatrack/service/SuggestionService.java` | submit / list / approve (+ materialize) / reject |
| `backend/src/main/java/hpnj/mediatrack/service/UserService.java` | createUser / findAll / findByIdOrEmail / changeRole / suspend / delete |
| `backend/src/main/resources/application-cli.yml` | CLI profile — disables web / Redis / Kafka / OAuth2 |
| `backend/src/main/java/hpnj/mediatrack/cli/CliConfiguration.java` | AuthenticationManager bean (@Profile("cli")) |
| `backend/src/main/java/hpnj/mediatrack/cli/CliSecurityContext.java` | In-memory logged-in user for CLI session |
| `backend/src/main/java/hpnj/mediatrack/cli/AuthCommands.java` | login / logout / whoami shell commands |
| `backend/src/main/java/hpnj/mediatrack/cli/MediaCommands.java` | media list / show / add / delete |
| `backend/src/main/java/hpnj/mediatrack/cli/PersonCommands.java` | person list / show / add |
| `backend/src/main/java/hpnj/mediatrack/cli/GroupCommands.java` | group list / add |
| `backend/src/main/java/hpnj/mediatrack/cli/SuggestionCommands.java` | suggestion list / show / approve / reject |
| `backend/src/main/java/hpnj/mediatrack/cli/UserCommands.java` | user list / show / create / role / suspend / delete |

### Modified files
| File | Change |
| --- | --- |
| `backend/src/main/java/hpnj/mediatrack/domain/user/UserAccount.java` | Replace `String userRole` → `UserRole role`; add `boolean suspended`; fix `getAuthorities()` and `isAccountNonLocked()` |
| `backend/pom.xml` | Add `spring-shell-starter 3.3.3` |

### Test files
| File | What it tests |
| --- | --- |
| `backend/src/test/java/hpnj/mediatrack/service/SuggestionServiceTest.java` | submit, approve (+ materialization), reject, approve throws on non-PENDING |
| `backend/src/test/java/hpnj/mediatrack/service/UserServiceTest.java` | createUser (hashes password), changeRole, suspend, createUser throws on duplicate email |

---

## Task 1: UserRole enum

**Files:**
- Create: `backend/src/main/java/hpnj/mediatrack/domain/enums/UserRole.java`

- [ ] **Step 1: Create enum**

```java
package hpnj.mediatrack.domain.enums;

public enum UserRole {
    USER, MODERATOR, ADMIN
}
```

- [ ] **Step 2: Commit**

```bash
git add backend/src/main/java/hpnj/mediatrack/domain/enums/UserRole.java
git commit -m "feat: add UserRole enum (USER/MODERATOR/ADMIN)"
```

---

## Task 2: Update UserAccount

**Files:**
- Modify: `backend/src/main/java/hpnj/mediatrack/domain/user/UserAccount.java`

Replaces `String userRole` with `UserRole role`; adds `boolean suspended`; `getAuthorities()` now prefixes `"ROLE_"`; `isAccountNonLocked()` returns `!suspended`.

- [ ] **Step 1: Rewrite UserAccount.java**

```java
package hpnj.mediatrack.domain.user;

import hpnj.mediatrack.domain.Auditable;
import hpnj.mediatrack.domain.enums.UserRole;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_account")
public class UserAccount extends Auditable implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean suspended = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    @Override
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    @Override
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isSuspended() { return suspended; }
    public void setSuspended(boolean suspended) { this.suspended = suspended; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return !suspended; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }
}
```

- [ ] **Step 2: Commit**

```bash
git add backend/src/main/java/hpnj/mediatrack/domain/user/UserAccount.java
git commit -m "feat: replace String userRole with UserRole enum, add suspended field"
```

---

## Task 3: Flyway migration V2

**Files:**
- Create: `backend/src/main/resources/db/migration/V2__add_role_and_suspended.sql`

Renames `user_role` → `role`, converts existing string values, adds `suspended`, creates `suggestion` table.

- [ ] **Step 1: Create migration**

```sql
-- Align user_account.user_role values with new UserRole enum (USER/MODERATOR/ADMIN)
UPDATE user_account SET user_role = 'USER'      WHERE user_role = 'ROLE_USER';
UPDATE user_account SET user_role = 'MODERATOR' WHERE user_role = 'ROLE_MODERATOR';
UPDATE user_account SET user_role = 'ADMIN'     WHERE user_role = 'ROLE_ADMIN';

ALTER TABLE user_account RENAME COLUMN user_role TO role;
ALTER TABLE user_account ALTER COLUMN role SET DEFAULT 'USER';

ALTER TABLE user_account ADD COLUMN suspended BOOLEAN NOT NULL DEFAULT FALSE;

-- Suggestion workflow
CREATE TABLE suggestion (
    id            UUID        NOT NULL,
    entity_type   VARCHAR(20) NOT NULL,
    proposed_data JSONB       NOT NULL,
    status        VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    submitted_by  UUID        NOT NULL,
    reviewed_by   UUID,
    review_note   TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_suggestion              PRIMARY KEY (id),
    CONSTRAINT fk_suggestion_submitted_by FOREIGN KEY (submitted_by) REFERENCES user_account (id),
    CONSTRAINT fk_suggestion_reviewed_by  FOREIGN KEY (reviewed_by)  REFERENCES user_account (id)
);

CREATE INDEX idx_suggestion_status       ON suggestion (status);
CREATE INDEX idx_suggestion_entity_type  ON suggestion (entity_type);
CREATE INDEX idx_suggestion_submitted_by ON suggestion (submitted_by);
```

- [ ] **Step 2: Commit**

```bash
git add backend/src/main/resources/db/migration/V2__add_role_and_suspended.sql
git commit -m "feat: migration V2 — rename role column, add suspended, create suggestion table"
```

---

## Task 4: Suggestion enums + entity

**Files:**
- Create: `backend/src/main/java/hpnj/mediatrack/domain/enums/SuggestionEntityType.java`
- Create: `backend/src/main/java/hpnj/mediatrack/domain/enums/SuggestionStatus.java`
- Create: `backend/src/main/java/hpnj/mediatrack/domain/suggestion/Suggestion.java`

- [ ] **Step 1: Create SuggestionEntityType**

```java
package hpnj.mediatrack.domain.enums;

public enum SuggestionEntityType {
    MOVIE, GAME, BOOK, ALBUM, PERSON, ORGANIZATION, MEDIA_GROUP
}
```

- [ ] **Step 2: Create SuggestionStatus**

```java
package hpnj.mediatrack.domain.enums;

public enum SuggestionStatus {
    PENDING, APPROVED, REJECTED
}
```

- [ ] **Step 3: Create Suggestion entity**

`@JdbcTypeCode(SqlTypes.JSON)` maps `proposed_data JSONB` to `Map<String, Object>` via Hibernate 6.

```java
package hpnj.mediatrack.domain.suggestion;

import hpnj.mediatrack.domain.Auditable;
import hpnj.mediatrack.domain.enums.SuggestionEntityType;
import hpnj.mediatrack.domain.enums.SuggestionStatus;
import hpnj.mediatrack.domain.user.UserAccount;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "suggestion")
public class Suggestion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private SuggestionEntityType entityType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "proposed_data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> proposedData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SuggestionStatus status = SuggestionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private UserAccount submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserAccount reviewedBy;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public SuggestionEntityType getEntityType() { return entityType; }
    public void setEntityType(SuggestionEntityType entityType) { this.entityType = entityType; }

    public Map<String, Object> getProposedData() { return proposedData; }
    public void setProposedData(Map<String, Object> proposedData) { this.proposedData = proposedData; }

    public SuggestionStatus getStatus() { return status; }
    public void setStatus(SuggestionStatus status) { this.status = status; }

    public UserAccount getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(UserAccount submittedBy) { this.submittedBy = submittedBy; }

    public UserAccount getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UserAccount reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
}
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/hpnj/mediatrack/domain/enums/SuggestionEntityType.java \
        backend/src/main/java/hpnj/mediatrack/domain/enums/SuggestionStatus.java \
        backend/src/main/java/hpnj/mediatrack/domain/suggestion/Suggestion.java
git commit -m "feat: add Suggestion entity and enums"
```

---

## Task 5: Repositories

**Files:**
- Create: `backend/src/main/java/hpnj/mediatrack/repository/UserAccountRepository.java`
- Create: `backend/src/main/java/hpnj/mediatrack/repository/MediaRepository.java`
- Create: `backend/src/main/java/hpnj/mediatrack/repository/PartyRepository.java`
- Create: `backend/src/main/java/hpnj/mediatrack/repository/MediaGroupRepository.java`
- Create: `backend/src/main/java/hpnj/mediatrack/repository/SuggestionRepository.java`

- [ ] **Step 1: UserAccountRepository**

```java
package hpnj.mediatrack.repository;

import hpnj.mediatrack.domain.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByEmail(String email);
    Optional<UserAccount> findByUsername(String username);
    boolean existsByEmail(String email);
}
```

- [ ] **Step 2: MediaRepository**

```java
package hpnj.mediatrack.repository;

import hpnj.mediatrack.domain.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {
}
```

- [ ] **Step 3: PartyRepository**

```java
package hpnj.mediatrack.repository;

import hpnj.mediatrack.domain.party.Party;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PartyRepository extends JpaRepository<Party, UUID> {
}
```

- [ ] **Step 4: MediaGroupRepository**

```java
package hpnj.mediatrack.repository;

import hpnj.mediatrack.domain.group.MediaGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaGroupRepository extends JpaRepository<MediaGroup, UUID> {
}
```

- [ ] **Step 5: SuggestionRepository**

```java
package hpnj.mediatrack.repository;

import hpnj.mediatrack.domain.enums.SuggestionStatus;
import hpnj.mediatrack.domain.suggestion.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SuggestionRepository extends JpaRepository<Suggestion, UUID> {
    List<Suggestion> findByStatus(SuggestionStatus status);
}
```

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/hpnj/mediatrack/repository/
git commit -m "feat: add JPA repositories"
```

---

## Task 6: PasswordEncoderConfig + UserDetailsService

**Files:**
- Create: `backend/src/main/java/hpnj/mediatrack/config/PasswordEncoderConfig.java`
- Create: `backend/src/main/java/hpnj/mediatrack/service/UserDetailsServiceImpl.java`

`PasswordEncoderConfig` has no profile restriction so it is available both in CLI and future REST API profiles. `UserDetailsServiceImpl` looks users up by email (the `login --email` convention).

- [ ] **Step 1: Create PasswordEncoderConfig**

```java
package hpnj.mediatrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 2: Create UserDetailsServiceImpl**

```java
package hpnj.mediatrack.service;

import hpnj.mediatrack.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountRepository repository;

    public UserDetailsServiceImpl(UserAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email: " + email));
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/hpnj/mediatrack/config/PasswordEncoderConfig.java \
        backend/src/main/java/hpnj/mediatrack/service/UserDetailsServiceImpl.java
git commit -m "feat: add PasswordEncoderConfig and UserDetailsService"
```

---

## Task 7: SuggestionService (TDD)

**Files:**
- Create: `backend/src/test/java/hpnj/mediatrack/service/SuggestionServiceTest.java`
- Create: `backend/src/main/java/hpnj/mediatrack/service/SuggestionService.java`

- [ ] **Step 1: Write failing tests**

```java
package hpnj.mediatrack.service;

import hpnj.mediatrack.domain.enums.SuggestionEntityType;
import hpnj.mediatrack.domain.enums.SuggestionStatus;
import hpnj.mediatrack.domain.suggestion.Suggestion;
import hpnj.mediatrack.domain.user.UserAccount;
import hpnj.mediatrack.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock SuggestionRepository suggestionRepository;
    @Mock MediaRepository mediaRepository;
    @Mock PartyRepository partyRepository;
    @Mock MediaGroupRepository mediaGroupRepository;
    @Mock UserAccountRepository userAccountRepository;
    @InjectMocks SuggestionService suggestionService;

    @Test
    void submit_createsPendingSuggestion() {
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        when(userAccountRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(suggestionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> data = Map.of("title", "Inception", "releaseYear", 2010);
        suggestionService.submit(user.getId(), SuggestionEntityType.MOVIE, data);

        ArgumentCaptor<Suggestion> captor = ArgumentCaptor.forClass(Suggestion.class);
        verify(suggestionRepository).save(captor.capture());
        Suggestion saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(SuggestionStatus.PENDING);
        assertThat(saved.getEntityType()).isEqualTo(SuggestionEntityType.MOVIE);
        assertThat(saved.getProposedData()).isEqualTo(data);
    }

    @Test
    void approve_changesStatusAndMaterializesMovie() {
        UserAccount reviewer = new UserAccount();
        reviewer.setId(UUID.randomUUID());

        Suggestion suggestion = new Suggestion();
        suggestion.setId(UUID.randomUUID());
        suggestion.setStatus(SuggestionStatus.PENDING);
        suggestion.setEntityType(SuggestionEntityType.MOVIE);
        suggestion.setProposedData(Map.of("title", "Inception", "releaseYear", 2010));

        when(suggestionRepository.findById(suggestion.getId())).thenReturn(Optional.of(suggestion));
        when(userAccountRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(suggestionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        suggestionService.approve(suggestion.getId(), reviewer.getId());

        assertThat(suggestion.getStatus()).isEqualTo(SuggestionStatus.APPROVED);
        assertThat(suggestion.getReviewedBy()).isEqualTo(reviewer);
        verify(mediaRepository).save(any());
    }

    @Test
    void reject_changesStatusWithNote() {
        UserAccount reviewer = new UserAccount();
        reviewer.setId(UUID.randomUUID());

        Suggestion suggestion = new Suggestion();
        suggestion.setId(UUID.randomUUID());
        suggestion.setStatus(SuggestionStatus.PENDING);

        when(suggestionRepository.findById(suggestion.getId())).thenReturn(Optional.of(suggestion));
        when(userAccountRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(suggestionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        suggestionService.reject(suggestion.getId(), reviewer.getId(), "Duplicate entry");

        assertThat(suggestion.getStatus()).isEqualTo(SuggestionStatus.REJECTED);
        assertThat(suggestion.getReviewNote()).isEqualTo("Duplicate entry");
        assertThat(suggestion.getReviewedBy()).isEqualTo(reviewer);
        verify(mediaRepository, never()).save(any());
    }

    @Test
    void approve_throwsWhenNotPending() {
        Suggestion suggestion = new Suggestion();
        suggestion.setId(UUID.randomUUID());
        suggestion.setStatus(SuggestionStatus.APPROVED);

        when(suggestionRepository.findById(suggestion.getId())).thenReturn(Optional.of(suggestion));

        assertThatThrownBy(() -> suggestionService.approve(suggestion.getId(), UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not PENDING");
    }
}
```

- [ ] **Step 2: Run — expect compilation failure**

```bash
cd backend && mvn test -Dtest=SuggestionServiceTest 2>&1 | tail -10
```
Expected: `COMPILATION ERROR — cannot find symbol: SuggestionService`

- [ ] **Step 3: Implement SuggestionService**

```java
package hpnj.mediatrack.service;

import hpnj.mediatrack.domain.enums.SuggestionEntityType;
import hpnj.mediatrack.domain.enums.SuggestionStatus;
import hpnj.mediatrack.domain.group.MediaGroup;
import hpnj.mediatrack.domain.enums.MediaGroupType;
import hpnj.mediatrack.domain.media.*;
import hpnj.mediatrack.domain.party.Organization;
import hpnj.mediatrack.domain.party.Person;
import hpnj.mediatrack.domain.suggestion.Suggestion;
import hpnj.mediatrack.domain.user.UserAccount;
import hpnj.mediatrack.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final MediaRepository mediaRepository;
    private final PartyRepository partyRepository;
    private final MediaGroupRepository mediaGroupRepository;
    private final UserAccountRepository userAccountRepository;

    public SuggestionService(SuggestionRepository suggestionRepository,
                              MediaRepository mediaRepository,
                              PartyRepository partyRepository,
                              MediaGroupRepository mediaGroupRepository,
                              UserAccountRepository userAccountRepository) {
        this.suggestionRepository = suggestionRepository;
        this.mediaRepository = mediaRepository;
        this.partyRepository = partyRepository;
        this.mediaGroupRepository = mediaGroupRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public Suggestion submit(UUID submitterId, SuggestionEntityType entityType, Map<String, Object> proposedData) {
        UserAccount submitter = userAccountRepository.findById(submitterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + submitterId));
        Suggestion suggestion = new Suggestion();
        suggestion.setSubmittedBy(submitter);
        suggestion.setEntityType(entityType);
        suggestion.setProposedData(proposedData);
        suggestion.setStatus(SuggestionStatus.PENDING);
        return suggestionRepository.save(suggestion);
    }

    @Transactional(readOnly = true)
    public List<Suggestion> list(SuggestionStatus status) {
        return status == null ? suggestionRepository.findAll() : suggestionRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Optional<Suggestion> findById(UUID id) {
        return suggestionRepository.findById(id);
    }

    @Transactional
    public void approve(UUID suggestionId, UUID reviewerId) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new IllegalArgumentException("Suggestion not found: " + suggestionId));
        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new IllegalStateException("Suggestion is not PENDING: " + suggestion.getStatus());
        }
        UserAccount reviewer = userAccountRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found: " + reviewerId));
        materialize(suggestion);
        suggestion.setStatus(SuggestionStatus.APPROVED);
        suggestion.setReviewedBy(reviewer);
        suggestionRepository.save(suggestion);
    }

    @Transactional
    public void reject(UUID suggestionId, UUID reviewerId, String note) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new IllegalArgumentException("Suggestion not found: " + suggestionId));
        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new IllegalStateException("Suggestion is not PENDING: " + suggestion.getStatus());
        }
        UserAccount reviewer = userAccountRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found: " + reviewerId));
        suggestion.setStatus(SuggestionStatus.REJECTED);
        suggestion.setReviewedBy(reviewer);
        suggestion.setReviewNote(note);
        suggestionRepository.save(suggestion);
    }

    private void materialize(Suggestion suggestion) {
        Map<String, Object> data = suggestion.getProposedData();
        switch (suggestion.getEntityType()) {
            case MOVIE -> {
                Movie e = new Movie();
                e.setTitle((String) data.get("title"));
                e.setReleaseYear(toInt(data.get("releaseYear")));
                mediaRepository.save(e);
            }
            case GAME -> {
                Game e = new Game();
                e.setTitle((String) data.get("title"));
                e.setReleaseYear(toInt(data.get("releaseYear")));
                mediaRepository.save(e);
            }
            case BOOK -> {
                Book e = new Book();
                e.setTitle((String) data.get("title"));
                e.setReleaseYear(toInt(data.get("releaseYear")));
                mediaRepository.save(e);
            }
            case ALBUM -> {
                Album e = new Album();
                e.setTitle((String) data.get("title"));
                e.setReleaseYear(toInt(data.get("releaseYear")));
                mediaRepository.save(e);
            }
            case PERSON -> {
                Person e = new Person();
                e.setName((String) data.get("name"));
                partyRepository.save(e);
            }
            case ORGANIZATION -> {
                Organization e = new Organization();
                e.setName((String) data.get("name"));
                partyRepository.save(e);
            }
            case MEDIA_GROUP -> {
                MediaGroup e = new MediaGroup();
                e.setName((String) data.get("name"));
                e.setGroupType(MediaGroupType.valueOf((String) data.get("groupType")));
                mediaGroupRepository.save(e);
            }
        }
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        return Integer.parseInt(value.toString());
    }
}
```

- [ ] **Step 4: Run — expect PASS**

```bash
cd backend && mvn test -Dtest=SuggestionServiceTest 2>&1 | tail -10
```
Expected: `Tests run: 4, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/hpnj/mediatrack/service/SuggestionService.java \
        backend/src/test/java/hpnj/mediatrack/service/SuggestionServiceTest.java
git commit -m "feat: add SuggestionService with TDD (submit/approve/reject/materialize)"
```

---

## Task 8: UserService (TDD)

**Files:**
- Create: `backend/src/test/java/hpnj/mediatrack/service/UserServiceTest.java`
- Create: `backend/src/main/java/hpnj/mediatrack/service/UserService.java`

- [ ] **Step 1: Write failing tests**

```java
package hpnj.mediatrack.service;

import hpnj.mediatrack.domain.enums.UserRole;
import hpnj.mediatrack.domain.user.UserAccount;
import hpnj.mediatrack.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserAccountRepository userAccountRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    @Test
    void createUser_savesWithHashedPassword() {
        when(passwordEncoder.encode("secret")).thenReturn("$hashed$");
        when(userAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.createUser("jan", "jan@example.com", "secret", UserRole.USER);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        UserAccount saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("$hashed$");
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(saved.getEmail()).isEqualTo("jan@example.com");
    }

    @Test
    void createUser_throwsOnDuplicateEmail() {
        when(userAccountRepository.existsByEmail("jan@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("jan", "jan@example.com", "pass", UserRole.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void changeRole_updatesRole() {
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setRole(UserRole.USER);
        when(userAccountRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.changeRole(user.getId(), UserRole.MODERATOR);

        assertThat(user.getRole()).isEqualTo(UserRole.MODERATOR);
    }

    @Test
    void suspend_setsSuspendedTrue() {
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setSuspended(false);
        when(userAccountRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.suspend(user.getId());

        assertThat(user.isSuspended()).isTrue();
    }
}
```

- [ ] **Step 2: Run — expect compilation failure**

```bash
cd backend && mvn test -Dtest=UserServiceTest 2>&1 | tail -10
```
Expected: `COMPILATION ERROR — cannot find symbol: UserService`

- [ ] **Step 3: Implement UserService**

```java
package hpnj.mediatrack.service;

import hpnj.mediatrack.domain.enums.UserRole;
import hpnj.mediatrack.domain.user.UserAccount;
import hpnj.mediatrack.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount createUser(String username, String email, String rawPassword, UserRole role) {
        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userAccountRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserAccount> findAll() {
        return userAccountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<UserAccount> findByIdOrEmail(String idOrEmail) {
        try {
            return userAccountRepository.findById(UUID.fromString(idOrEmail));
        } catch (IllegalArgumentException e) {
            return userAccountRepository.findByEmail(idOrEmail);
        }
    }

    @Transactional
    public void changeRole(UUID userId, UserRole newRole) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setRole(newRole);
        userAccountRepository.save(user);
    }

    @Transactional
    public void suspend(UUID userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setSuspended(true);
        userAccountRepository.save(user);
    }

    @Transactional
    public void delete(UUID userId) {
        userAccountRepository.deleteById(userId);
    }
}
```

- [ ] **Step 4: Run — expect PASS**

```bash
cd backend && mvn test -Dtest=UserServiceTest 2>&1 | tail -10
```
Expected: `Tests run: 4, Failures: 0, Errors: 0`

- [ ] **Step 5: Run all tests**

```bash
cd backend && mvn test 2>&1 | tail -15
```
Expected: all pass.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/hpnj/mediatrack/service/UserService.java \
        backend/src/test/java/hpnj/mediatrack/service/UserServiceTest.java
git commit -m "feat: add UserService with TDD (createUser/changeRole/suspend/delete)"
```

---

## Task 9: Spring Shell + CLI profile

**Files:**
- Modify: `backend/pom.xml`
- Create: `backend/src/main/resources/application-cli.yml`
- Create: `backend/src/main/java/hpnj/mediatrack/cli/CliConfiguration.java`
- Create: `backend/src/main/java/hpnj/mediatrack/cli/CliSecurityContext.java`

- [ ] **Step 1: Add spring-shell-starter to pom.xml**

Inside `<dependencies>` in `backend/pom.xml`:

```xml
<!-- Spring Shell (CLI profile) -->
<dependency>
    <groupId>org.springframework.shell</groupId>
    <artifactId>spring-shell-starter</artifactId>
    <version>3.3.3</version>
</dependency>
```

- [ ] **Step 2: Create application-cli.yml**

```yaml
spring:
  main:
    web-application-type: none
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
      - org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration
      - org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
      - org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration

shell:
  interactive:
    enabled: true
```

- [ ] **Step 3: Create CliSecurityContext**

```java
package hpnj.mediatrack.cli;

import hpnj.mediatrack.domain.enums.UserRole;
import hpnj.mediatrack.domain.user.UserAccount;
import org.springframework.stereotype.Component;

@Component
public class CliSecurityContext {

    private UserAccount currentUser;

    public void login(UserAccount user) { this.currentUser = user; }
    public void logout() { this.currentUser = null; }
    public UserAccount getCurrentUser() { return currentUser; }
    public boolean isLoggedIn() { return currentUser != null; }

    public boolean hasRole(UserRole minimumRole) {
        return currentUser != null && currentUser.getRole().ordinal() >= minimumRole.ordinal();
    }
}
```

- [ ] **Step 4: Create CliConfiguration**

```java
package hpnj.mediatrack.cli;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("cli")
public class CliConfiguration {

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                        PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
```

- [ ] **Step 5: Verify compile**

```bash
cd backend && mvn compile -q 2>&1 | tail -10
```
Expected: no errors.

- [ ] **Step 6: Commit**

```bash
git add backend/pom.xml \
        backend/src/main/resources/application-cli.yml \
        backend/src/main/java/hpnj/mediatrack/cli/CliConfiguration.java \
        backend/src/main/java/hpnj/mediatrack/cli/CliSecurityContext.java
git commit -m "feat: add Spring Shell, CLI profile config and CliSecurityContext"
```

---

## Task 10: AuthCommands

**Files:**
- Create: `backend/src/main/java/hpnj/mediatrack/cli/AuthCommands.java`

- [ ] **Step 1: Create AuthCommands**

```java
package hpnj.mediatrack.cli;

import hpnj.mediatrack.domain.user.UserAccount;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class AuthCommands {

    private final AuthenticationManager authenticationManager;
    private final CliSecurityContext ctx;

    public AuthCommands(AuthenticationManager authenticationManager, CliSecurityContext ctx) {
        this.authenticationManager = authenticationManager;
        this.ctx = ctx;
    }

    @ShellMethod(key = "login", value = "Log in with email and password")
    public String login(@ShellOption String email, @ShellOption String password) {
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            UserAccount user = (UserAccount) auth.getPrincipal();
            ctx.login(user);
            return "Logged in as " + user.getEmail() + " [" + user.getRole() + "]";
        } catch (BadCredentialsException e) {
            return "Login failed: invalid credentials.";
        }
    }

    @ShellMethod(key = "logout", value = "Log out")
    public String logout() {
        if (!ctx.isLoggedIn()) return "Not logged in.";
        ctx.logout();
        return "Logged out.";
    }

    @ShellMethod(key = "whoami", value = "Show current user")
    public String whoami() {
        if (!ctx.isLoggedIn()) return "Not logged in.";
        UserAccount user = ctx.getCurrentUser();
        return user.getEmail() + " [" + user.getRole() + "]";
    }
}
```

- [ ] **Step 2: Compile check**

```bash
cd backend && mvn compile -q 2>&1 | tail -10
```

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/hpnj/mediatrack/cli/AuthCommands.java
git commit -m "feat: add CLI login/logout/whoami commands"
```

---

## Task 11: MediaCommands

**Files:**
- Create: `backend/src/main/java/hpnj/mediatrack/cli/MediaCommands.java`

- [ ] **Step 1: Create MediaCommands**

```java
package hpnj.mediatrack.cli;

import hpnj.mediatrack.domain.enums.UserRole;
import hpnj.mediatrack.domain.media.*;
import hpnj.mediatrack.repository.MediaRepository;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.UUID;
import java.util.stream.Collectors;

@ShellComponent
public class MediaCommands {

    private final MediaRepository mediaRepository;
    private final CliSecurityContext ctx;

    public MediaCommands(MediaRepository mediaRepository, CliSecurityContext ctx) {
        this.mediaRepository = mediaRepository;
        this.ctx = ctx;
    }

    @ShellMethod(key = "media list", value = "List media [--type MOVIE|GAME|BOOK|ALBUM]")
    public String list(@ShellOption(defaultValue = "") String type) {
        if (!ctx.isLoggedIn()) return "Login required.";
        var all = mediaRepository.findAll();
        var result = type.isBlank() ? all : all.stream()
                .filter(m -> m.getClass().getSimpleName().equalsIgnoreCase(type))
                .toList();
        if (result.isEmpty()) return "No media found.";
        return result.stream()
                .map(m -> m.getId() + "  " + m.getClass().getSimpleName() + "  " + m.getTitle()
                        + "  (" + m.getReleaseYear() + ")")
                .collect(Collectors.joining("\n"));
    }

    @ShellMethod(key = "media show", value = "Show media details")
    public String show(@ShellOption String id) {
        if (!ctx.isLoggedIn()) return "Login required.";
        return mediaRepository.findById(UUID.fromString(id))
                .map(m -> "ID:     " + m.getId()
                        + "\nType:   " + m.getClass().getSimpleName()
                        + "\nTitle:  " + m.getTitle()
                        + "\nYear:   " + m.getReleaseYear()
                        + "\nRating: " + m.getAverageRating())
                .orElse("Not found: " + id);
    }

    @ShellMethod(key = "media add", value = "Add media (MODERATOR+)")
    public String add(@ShellOption String type,
                      @ShellOption String title,
                      @ShellOption(defaultValue = "0") int year) {
        if (!ctx.hasRole(UserRole.MODERATOR)) return "Access denied. Requires MODERATOR.";
        Media entity = switch (type.toUpperCase()) {
            case "MOVIE" -> new Movie();
            case "GAME"  -> new Game();
            case "BOOK"  -> new Book();
            case "ALBUM" -> new Album();
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
        entity.setTitle(title);
        entity.setReleaseYear(year == 0 ? null : year);
        Media saved = mediaRepository.save(entity);
        return "Created: " + saved.getId() + "  " + type.toUpperCase() + "  " + title;
    }

    @ShellMethod(key = "media delete", value = "Delete media (MODERATOR+)")
    public String delete(@ShellOption String id) {
        if (!ctx.hasRole(UserRole.MODERATOR)) return "Access denied. Requires MODERATOR.";
        UUID uuid = UUID.fromString(id);
        if (!mediaRepository.existsById(uuid)) return "Not found: " + id;
        mediaRepository.deleteById(uuid);
        return "Deleted: " + id;
    }
}
```

- [ ] **Step 2: Compile + commit**

```bash
cd backend && mvn compile -q && \
git add backend/src/main/java/hpnj/mediatrack/cli/MediaCommands.java && \
git commit -m "feat: add CLI media commands (list/show/add/delete)"
```

---

## Task 12: PersonCommands

**Files:**
- Create: `backend/src/main/java/hpnj/mediatrack/cli/PersonCommands.java`

- [ ] **Step 1: Create PersonCommands**

```java
package hpnj.mediatrack.cli;

import hpnj.mediatrack.domain.enums.UserRole;
import hpnj.mediatrack.domain.party.Person;
import hpnj.mediatrack.repository.PartyRepository;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.UUID;
import java.util.stream.Collectors;

@ShellComponent
public class PersonCommands {

    private final PartyRepository partyRepository;
    private final CliSecurityContext ctx;

    public PersonCommands(PartyRepository partyRepository, CliSecurityContext ctx) {
        this.partyRepository = partyRepository;
        this.ctx = ctx;
    }

    @ShellMethod(key = "person list", value = "List persons")
    public String list() {
        if (!ctx.isLoggedIn()) return "Login required.";
        var persons = partyRepository.findAll().stream()
                .filter(p -> p instanceof Person)
                .toList();
        if (persons.isEmpty()) return "No persons found.";
        return persons.stream()
                .map(p -> p.getId() + "  " + p.getName())
                .collect(Collectors.joining("\n"));
    }

    @ShellMethod(key = "person show", value = "Show person details")
    public String show(@ShellOption String id) {
        if (!ctx.isLoggedIn()) return "Login required.";
        return partyRepository.findById(UUID.fromString(id))
                .filter(p -> p instanceof Person)
                .map(p -> "ID:   " + p.getId() + "\nName: " + p.getName())
                .orElse("Person not found: " + id);
    }

    @ShellMethod(key = "person add", value = "Add a person (MODERATOR+)")
    public String add(@ShellOption String name) {
        if (!ctx.hasRole(UserRole.MODERATOR)) return "Access denied. Requires MODERATOR.";
        Person person = new Person();
        person.setName(name);
        var saved = partyRepository.save(person);
        return "Created: " + saved.getId() + "  " + name;
    }
}
```

- [ ] **Step 2: Compile + commit**

```bash
cd backend && mvn compile -q && \
git add backend/src/main/java/hpnj/mediatrack/cli/PersonCommands.java && \
git commit -m "feat: add CLI person commands (list/show/add)"
```

---

## Task 13: GroupCommands

**Files:**
- Create: `backend/src/main/java/hpnj/mediatrack/cli/GroupCommands.java`

- [ ] **Step 1: Create GroupCommands**

```java
package hpnj.mediatrack.cli;

import hpnj.mediatrack.domain.enums.MediaGroupType;
import hpnj.mediatrack.domain.enums.UserRole;
import hpnj.mediatrack.domain.group.MediaGroup;
import hpnj.mediatrack.repository.MediaGroupRepository;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.stream.Collectors;

@ShellComponent
public class GroupCommands {

    private final MediaGroupRepository mediaGroupRepository;
    private final CliSecurityContext ctx;

    public GroupCommands(MediaGroupRepository mediaGroupRepository, CliSecurityContext ctx) {
        this.mediaGroupRepository = mediaGroupRepository;
        this.ctx = ctx;
    }

    @ShellMethod(key = "group list", value = "List media groups [--type GENRE|FRANCHISE|UNIVERSE|SERIES|THEME]")
    public String list(@ShellOption(defaultValue = "") String type) {
        if (!ctx.isLoggedIn()) return "Login required.";
        var all = mediaGroupRepository.findAll();
        var result = type.isBlank() ? all : all.stream()
                .filter(g -> g.getGroupType().name().equalsIgnoreCase(type))
                .toList();
        if (result.isEmpty()) return "No groups found.";
        return result.stream()
                .map(g -> g.getId() + "  " + g.getGroupType() + "  " + g.getName())
                .collect(Collectors.joining("\n"));
    }

    @ShellMethod(key = "group add", value = "Add a media group (MODERATOR+)")
    public String add(@ShellOption String name, @ShellOption String type) {
        if (!ctx.hasRole(UserRole.MODERATOR)) return "Access denied. Requires MODERATOR.";
        MediaGroup group = new MediaGroup();
        group.setName(name);
        group.setGroupType(MediaGroupType.valueOf(type.toUpperCase()));
        var saved = mediaGroupRepository.save(group);
        return "Created: " + saved.getId() + "  " + type.toUpperCase() + "  " + name;
    }
}
```

- [ ] **Step 2: Compile + commit**

```bash
cd backend && mvn compile -q && \
git add backend/src/main/java/hpnj/mediatrack/cli/GroupCommands.java && \
git commit -m "feat: add CLI group commands (list/add)"
```

---

## Task 14: SuggestionCommands

**Files:**
- Create: `backend/src/main/java/hpnj/mediatrack/cli/SuggestionCommands.java`

- [ ] **Step 1: Create SuggestionCommands**

```java
package hpnj.mediatrack.cli;

import hpnj.mediatrack.domain.enums.SuggestionStatus;
import hpnj.mediatrack.domain.enums.UserRole;
import hpnj.mediatrack.service.SuggestionService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.UUID;
import java.util.stream.Collectors;

@ShellComponent
public class SuggestionCommands {

    private final SuggestionService suggestionService;
    private final CliSecurityContext ctx;

    public SuggestionCommands(SuggestionService suggestionService, CliSecurityContext ctx) {
        this.suggestionService = suggestionService;
        this.ctx = ctx;
    }

    @ShellMethod(key = "suggestion list", value = "List suggestions [--status PENDING|APPROVED|REJECTED]")
    public String list(@ShellOption(defaultValue = "") String status) {
        if (!ctx.hasRole(UserRole.MODERATOR)) return "Access denied. Requires MODERATOR.";
        SuggestionStatus filter = status.isBlank() ? null : SuggestionStatus.valueOf(status.toUpperCase());
        var suggestions = suggestionService.list(filter);
        if (suggestions.isEmpty()) return "No suggestions found.";
        return suggestions.stream()
                .map(s -> {
                    Object label = s.getProposedData().getOrDefault("title",
                            s.getProposedData().getOrDefault("name", "-"));
                    return s.getId() + "  " + s.getEntityType() + "  " + s.getStatus() + "  " + label;
                })
                .collect(Collectors.joining("\n"));
    }

    @ShellMethod(key = "suggestion show", value = "Show suggestion details")
    public String show(@ShellOption String id) {
        if (!ctx.hasRole(UserRole.MODERATOR)) return "Access denied. Requires MODERATOR.";
        return suggestionService.findById(UUID.fromString(id))
                .map(s -> "ID:          " + s.getId()
                        + "\nType:        " + s.getEntityType()
                        + "\nStatus:      " + s.getStatus()
                        + "\nData:        " + s.getProposedData()
                        + "\nSubmitted by:" + s.getSubmittedBy().getEmail()
                        + (s.getReviewNote() != null ? "\nNote:        " + s.getReviewNote() : ""))
                .orElse("Not found: " + id);
    }

    @ShellMethod(key = "suggestion approve", value = "Approve a suggestion (MODERATOR+)")
    public String approve(@ShellOption String id) {
        if (!ctx.hasRole(UserRole.MODERATOR)) return "Access denied. Requires MODERATOR.";
        suggestionService.approve(UUID.fromString(id), ctx.getCurrentUser().getId());
        return "Approved: " + id;
    }

    @ShellMethod(key = "suggestion reject", value = "Reject a suggestion (MODERATOR+)")
    public String reject(@ShellOption String id,
                          @ShellOption(defaultValue = "") String note) {
        if (!ctx.hasRole(UserRole.MODERATOR)) return "Access denied. Requires MODERATOR.";
        suggestionService.reject(UUID.fromString(id), ctx.getCurrentUser().getId(),
                note.isBlank() ? null : note);
        return "Rejected: " + id;
    }
}
```

- [ ] **Step 2: Compile + commit**

```bash
cd backend && mvn compile -q && \
git add backend/src/main/java/hpnj/mediatrack/cli/SuggestionCommands.java && \
git commit -m "feat: add CLI suggestion commands (list/show/approve/reject)"
```

---

## Task 15: UserCommands

**Files:**
- Create: `backend/src/main/java/hpnj/mediatrack/cli/UserCommands.java`

- [ ] **Step 1: Create UserCommands**

```java
package hpnj.mediatrack.cli;

import hpnj.mediatrack.domain.enums.UserRole;
import hpnj.mediatrack.service.UserService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.UUID;
import java.util.stream.Collectors;

@ShellComponent
public class UserCommands {

    private final UserService userService;
    private final CliSecurityContext ctx;

    public UserCommands(UserService userService, CliSecurityContext ctx) {
        this.userService = userService;
        this.ctx = ctx;
    }

    @ShellMethod(key = "user list", value = "List all users (ADMIN)")
    public String list() {
        if (!ctx.hasRole(UserRole.ADMIN)) return "Access denied. Requires ADMIN.";
        var users = userService.findAll();
        if (users.isEmpty()) return "No users found.";
        return users.stream()
                .map(u -> u.getId() + "  " + u.getEmail() + "  " + u.getRole()
                        + (u.isSuspended() ? "  [SUSPENDED]" : ""))
                .collect(Collectors.joining("\n"));
    }

    @ShellMethod(key = "user show", value = "Show user by ID or email (ADMIN)")
    public String show(@ShellOption String idOrEmail) {
        if (!ctx.hasRole(UserRole.ADMIN)) return "Access denied. Requires ADMIN.";
        return userService.findByIdOrEmail(idOrEmail)
                .map(u -> "ID:        " + u.getId()
                        + "\nEmail:     " + u.getEmail()
                        + "\nUsername:  " + u.getUsername()
                        + "\nRole:      " + u.getRole()
                        + "\nSuspended: " + u.isSuspended())
                .orElse("Not found: " + idOrEmail);
    }

    @ShellMethod(key = "user create", value = "Create a user (ADMIN)")
    public String create(@ShellOption String username,
                          @ShellOption String email,
                          @ShellOption String password,
                          @ShellOption(defaultValue = "USER") String role) {
        if (!ctx.hasRole(UserRole.ADMIN)) return "Access denied. Requires ADMIN.";
        var user = userService.createUser(username, email, password, UserRole.valueOf(role.toUpperCase()));
        return "Created: " + user.getId() + "  " + email + "  [" + user.getRole() + "]";
    }

    @ShellMethod(key = "user role", value = "Change user role (ADMIN)")
    public String role(@ShellOption String id, @ShellOption String set) {
        if (!ctx.hasRole(UserRole.ADMIN)) return "Access denied. Requires ADMIN.";
        userService.changeRole(UUID.fromString(id), UserRole.valueOf(set.toUpperCase()));
        return "Role updated: " + id + " → " + set.toUpperCase();
    }

    @ShellMethod(key = "user suspend", value = "Suspend a user (ADMIN)")
    public String suspend(@ShellOption String id) {
        if (!ctx.hasRole(UserRole.ADMIN)) return "Access denied. Requires ADMIN.";
        userService.suspend(UUID.fromString(id));
        return "Suspended: " + id;
    }

    @ShellMethod(key = "user delete", value = "Delete a user (ADMIN)")
    public String delete(@ShellOption String id) {
        if (!ctx.hasRole(UserRole.ADMIN)) return "Access denied. Requires ADMIN.";
        userService.delete(UUID.fromString(id));
        return "Deleted: " + id;
    }
}
```

- [ ] **Step 2: Final compile + full test run**

```bash
cd backend && mvn compile -q 2>&1 | tail -10
cd backend && mvn test 2>&1 | tail -15
```
Expected: compile clean, all tests pass.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/hpnj/mediatrack/cli/UserCommands.java
git commit -m "feat: add CLI user admin commands (list/show/create/role/suspend/delete)"
```
