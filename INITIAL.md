## FEATURE:

Chcem pridat moznost, aby sme vedeli priradit dany task k userovi, resp assignut ho k tasku, aby bolo jasne
,ze tento user ho ma spravit. Bude to nieco ine ako teraz co uz mame userId pri task, to je ze iba tento user vytvoril dany task.
Ale chcem aby tam bolo nieco nove, resp user ktory ma dany task urobit.

Bude treba novy stlpec do DB, a aj v JDBC a aj v JPA verzii na to reagovat.
Budes musiet zmenit schema.sql v resources, a tiez kludne updatni data.sql.

Tiez mame testy @TaskIntegrationTest, ktore treba upravit, resp vytvorit nove.

Toto policko je volitelne, resp moze existovat task, ktory este nema prideleneho usera.
User sa moze zmenit aj na nejakeho ineho, alebo znova na null.

## EXAMPLES:

### Domain Model
- **Task domain model**: `src/main/java/sk/streetofcode/taskmanagementsystem/domain/Task.java:8` - Immutable domain object s `@Value` anotaciou. Aktualne ma polia: id, userId, projectId (nullable), name, description, status, createdAt.
- Nove pole `assignedUserId` (Long, nullable) by malo byt podobne ako `projectId` na riadku 11.

### JPA Implementation
- **TaskEntity**: `src/main/java/sk/streetofcode/taskmanagementsystem/implementation/jpa/entity/TaskEntity.java:16` - JPA entita s @ManyToOne vztahmi.
- Pozri sa ako je implementovany `@ManyToOne` vztah s UserEntity na riadku 22-24 (pre user ktory vytvoril task).
- Pozri sa ako je implementovany nullable `@ManyToOne` vztah s ProjectEntity na riadku 26-29 (nullable=true).
- Novy vztah `assignedUser` by mal byt podobny ako project - `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "assigned_user_id", nullable = true)`, a `@Setter` aby sa dal menit.

- **TaskServiceJpaImpl**: `src/main/java/sk/streetofcode/taskmanagementsystem/implementation/jpa/service/TaskServiceJpaImpl.java:31` - Service implementacia s `@Profile("jpa")`.
- Pozri metodu `add()` na riadku 46 - ukazuje ako sa vytvara UserEntity a ProjectEntity pred ulozenim TaskEntity.
- Bude treba podobny pattern pre assignedUser.

- **TaskJpaRepository**: `src/main/java/sk/streetofcode/taskmanagementsystem/implementation/jpa/repository/TaskJpaRepository.java` - Spring Data JPA repository interface.

### JDBC Implementation
- **TaskRowMapper**: `src/main/java/sk/streetofcode/taskmanagementsystem/implementation/jdbc/mapper/TaskRowMapper.java:11` - Mapper pre JDBC result set.
- Pozri riadok 17 ako sa mapuje nullable projectId: `rs.getObject("project_id") != null ? rs.getLong("project_id"): null`
- Novy `assigned_user_id` by mal mat identicky pattern.

- **TaskServiceJdbcImpl**: `src/main/java/sk/streetofcode/taskmanagementsystem/implementation/jdbc/service/TaskServiceJdbcImpl.java:20` - Service implementacia s `@Profile("jdbc")`.

- **TaskJdbcRepository**: `src/main/java/sk/streetofcode/taskmanagementsystem/implementation/jdbc/repository/TaskJdbcRepository.java` - Repository s JdbcTemplate a SQL queries.

### Database Schema
- **schema.sql**: `src/main/resources/schema.sql:20` - Definicia task tabulky zacina na riadku 20.
- Aktualne stlpce: id, user_id, project_id (nullable), name, description, status, created_at.
- Pridaj novy stlpec `assigned_user_id bigint` (nullable), plus FOREIGN KEY constraint na users(id) podobne ako na riadku 30.

- **data.sql**: `src/main/resources/data.sql:14` - INSERT statements pre testove data.
- Updatni INSERT statements aby obsahovali hodnoty pre novy stlpec (niektore null, niektore s konkretnym user ID).

### API Layer
- **TaskAddRequest**: `src/main/java/sk/streetofcode/taskmanagementsystem/api/request/TaskAddRequest.java:10` - Request DTO pre vytvorenie tasku.
- Pridaj nove pole `Long assignedUserId` (nullable).

- **TaskEditRequest**: Podobne ako TaskAddRequest, bude potrebovat nove pole pre assignedUserId.

- **TaskService interface**: `src/main/java/sk/streetofcode/taskmanagementsystem/api/TaskService.java` - Service interface ktory implementuju oba profily.

- **TaskController**: `src/main/java/sk/streetofcode/taskmanagementsystem/controller/TaskController.java` - REST controller.

### Tests
- **TaskIntegrationTests**: `src/test/java/sk/streetofcode/taskmanagementsystem/TaskIntegrationTests.java:21` - Integracne testy pre Task API.
- Pozri test `insertWithoutProjectId()` na riadku 106 - ukazuje ako testovat optional pole (projectId).
- Vytvor podobne testy pre assignedUserId:
  - Test vytvorenia tasku bez assigned user (null)
  - Test vytvorenia tasku s assigned user
  - Test zmeny assigned user (z null na user, z user na ineho user, z user na null)
- Pozri test `assign()` na riadku 242 - ukazuje pattern pre assign endpoint (ale ten je pre project, nie user).
- Pozri `generateRandomTask()` metodu na riadku 309 - bude potrebne upravit aby generovala tasks s/bez assignedUserId.

## DOCUMENTATION:

### External Resources
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- Spring Data JDBC: https://spring.io/projects/spring-data-jdbc

### Internal Resources
- **CLAUDE.md**: `CLAUDE.md` - Kompletny project guide.
  - Pozri sekciu "Multi-Implementation Pattern with Spring Profiles" - vysvetluje ako funguju JPA a JDBC profily.
  - Pozri sekciu "Common Patterns When Adding Features" - checklist co treba urobit pri pridavani novych features.
  - Pozri sekciu "Testing" - informacie o integracnych testoch.

### Key Patterns Already in Codebase
- **Nullable Foreign Key**: Task uz ma nullable `project_id` - pouzit ten isty pattern.
- **ManyToOne Relationships**: TaskEntity uz ma vztahy s UserEntity a ProjectEntity - pouzit ten isty pattern.
- **Profile-Based Implementations**: Obe service implementacie (`@Profile("jpa")` a `@Profile("jdbc")`) musia byt updatnute.
- **RowMapper Pattern**: TaskRowMapper uz mapuje nullable projectId - pouzit identicky pattern.

## OTHER CONSIDERATIONS:

### Testing Requirements
- Aplikacia bezi v **dvoch profiloch** (`jpa` a `jdbc`), takze implementacia musi fungovat pre **OBA**.
- Testy musia prejst pre oba profily.
- Run tests: `./mvnw test -Dtest=TaskIntegrationTests`
- Run with JPA profile: `./mvnw spring-boot:run -Dspring-boot.run.profiles=jpa`
- Run with JDBC profile: `./mvnw spring-boot:run -Dspring-boot.run.profiles=jdbc`

### Database
- DB: H2 in-memory (`jdbc:h2:mem:testdb`)
- Schema nie je auto-generated (`spring.jpa.hibernate.ddl-auto=none`)
- Vsetky zmeny musia byt v `schema.sql` a `data.sql`

### Design Patterns to Follow
1. **Immutable Domain Models**: Pouzivat `@Value` pre domain objekty (Task.java)
2. **Service Interface Pattern**: Controllers zavisia na interface, nie implementacii
3. **Nullable Relationships**: Assignee je optional - moze byt null
4. **Validation**: UserService.get() by mal validovat ci user existuje pred assignmentom

### Edge Cases
- Task moze mat assigned user, ktory je **rozny** od user ktory task vytvoril (userId != assignedUserId)
- Task moze mat null assigned user (este nie je prideleny nikomu)
- Assigned user sa moze zmenit na ineho usera alebo na null
- Treba validovat ci assigned user existuje v DB (pouzit UserService.get())
