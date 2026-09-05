package studio.pixelforge.backend.course;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.organization.Organization;
import studio.pixelforge.backend.organization.OrganizationRepository;
import studio.pixelforge.backend.portal.CourseStatusChangedEvent;

import java.util.List;

@Service
public class CourseService {

    // Единственный tenant, как и во всём остальном проекте.
    private static final Long ORG_ID = 1L;

    private final CourseRepository courseRepository;
    private final CourseNodeRepository courseNodeRepository;
    private final NodeTaskRepository nodeTaskRepository;
    private final OrganizationRepository organizationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CourseService(CourseRepository courseRepository,
                          CourseNodeRepository courseNodeRepository,
                          NodeTaskRepository nodeTaskRepository,
                          OrganizationRepository organizationRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.courseRepository = courseRepository;
        this.courseNodeRepository = courseNodeRepository;
        this.nodeTaskRepository = nodeTaskRepository;
        this.organizationRepository = organizationRepository;
        this.eventPublisher = eventPublisher;
    }

    private void publishVisibility(Course course, String event) {
        eventPublisher.publishEvent(new CourseStatusChangedEvent(
            event, course.getId(), course.getSlug(), course.getTitle(),
            course.getDescription(), course.getStatus().name()));
    }

    @Transactional(readOnly = true)
    public List<Course> list() {
        return courseRepository.findByOrganization_IdOrderBySortOrderAsc(ORG_ID);
    }

    @Transactional(readOnly = true)
    public Course getById(Long id) {
        return courseRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Course not found: " + id));
    }

    @Transactional
    public Course create(CreateCourseRequest request) {
        Organization org = organizationRepository.findById(ORG_ID)
            .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + ORG_ID));

        Course course = new Course(org, request.title());
        course.setDescription(request.description());
        if (request.status() != null) {
            course.setStatus(request.status());
        }
        if (request.sortOrder() != null) {
            course.setSortOrder(request.sortOrder());
        }
        course.setSlug(resolveSlug(request.slug(), request.title(), null));
        Course saved = courseRepository.save(course);
        if (saved.getStatus() == CourseStatus.PUBLISHED) {
            publishVisibility(saved, "published");
        }
        return saved;
    }

    @Transactional
    public Course update(Long id, UpdateCourseRequest request) {
        Course course = getById(id);
        CourseStatus before = course.getStatus();
        if (request.title() != null) {
            course.setTitle(request.title());
        }
        if (request.description() != null) {
            course.setDescription(request.description());
        }
        if (request.status() != null) {
            course.setStatus(request.status());
        }
        if (request.sortOrder() != null) {
            course.setSortOrder(request.sortOrder());
        }
        // slug: null — не трогать; "" — пересобрать из (нового) title;
        // непустая строка — использовать как есть (с проверкой уникальности).
        if (request.slug() != null) {
            String basis = request.slug().isBlank() ? course.getTitle() : request.slug();
            course.setSlug(resolveSlug(request.slug().isBlank() ? null : request.slug(), basis, id));
        }
        emitVisibilityChange(course, before, course.getStatus());
        return course;
    }

    @Transactional
    public void delete(Long id) {
        Course course = getById(id);
        // Снимок до удаления — событие несёт его, строки уже не будет.
        CourseStatusChangedEvent deleted = new CourseStatusChangedEvent(
            "deleted", course.getId(), course.getSlug(), course.getTitle(),
            course.getDescription(), course.getStatus().name());
        courseRepository.delete(course);
        eventPublisher.publishEvent(deleted);
    }

    @Transactional
    public Course archive(Long id) {
        Course course = getById(id);
        CourseStatus before = course.getStatus();
        course.setStatus(CourseStatus.ARCHIVED);
        emitVisibilityChange(course, before, CourseStatus.ARCHIVED);
        return course;
    }

    @Transactional
    public Course unarchive(Long id) {
        Course course = getById(id);
        CourseStatus before = course.getStatus();
        if (course.getStatus() == CourseStatus.ARCHIVED) {
            course.setStatus(CourseStatus.DRAFT);
        }
        emitVisibilityChange(course, before, course.getStatus());
        return course;
    }

    // Витрина портала видит только PUBLISHED-курсы. Событие шлём только на
    // смене видимости: стал PUBLISHED -> published; перестал -> unpublished.
    private void emitVisibilityChange(Course course, CourseStatus before, CourseStatus after) {
        if (before != CourseStatus.PUBLISHED && after == CourseStatus.PUBLISHED) {
            publishVisibility(course, "published");
        } else if (before == CourseStatus.PUBLISHED && after != CourseStatus.PUBLISHED) {
            publishVisibility(course, "unpublished");
        }
    }

    @Transactional(readOnly = true)
    public CourseTreeResponse tree(Long id) {
        Course course = getById(id);
        List<CourseNode> nodes = courseNodeRepository.findByCourse_IdOrderBySortOrderAsc(id);
        List<NodeTask> nodeTasks = nodeTaskRepository.findByNode_Course_IdOrderBySortOrderAsc(id);
        return CourseTreeResponse.build(course, nodes, nodeTasks);
    }

    // explicitSlug задан и непустой -> используем как есть, проверив
    // уникальность; иначе генерируем из titleForSlug и подбираем свободный.
    private String resolveSlug(String explicitSlug, String titleForSlug, Long excludeId) {
        if (explicitSlug != null && !explicitSlug.isBlank()) {
            String candidate = SlugGenerator.slugify(explicitSlug);
            boolean taken = excludeId == null
                ? courseRepository.existsBySlug(candidate)
                : courseRepository.existsBySlugAndIdNot(candidate, excludeId);
            if (taken) {
                throw new IllegalStateException("Course slug already in use: " + candidate);
            }
            return candidate;
        }
        return SlugGenerator.uniqueSlug(titleForSlug, candidate -> excludeId == null
            ? courseRepository.existsBySlug(candidate)
            : courseRepository.existsBySlugAndIdNot(candidate, excludeId));
    }
}
