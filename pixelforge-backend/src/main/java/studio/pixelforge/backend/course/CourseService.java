package studio.pixelforge.backend.course;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.organization.Organization;
import studio.pixelforge.backend.organization.OrganizationRepository;

import java.util.List;

@Service
public class CourseService {

    // Единственный tenant, как и во всём остальном проекте.
    private static final Long ORG_ID = 1L;

    private final CourseRepository courseRepository;
    private final CourseNodeRepository courseNodeRepository;
    private final OrganizationRepository organizationRepository;

    public CourseService(CourseRepository courseRepository,
                          CourseNodeRepository courseNodeRepository,
                          OrganizationRepository organizationRepository) {
        this.courseRepository = courseRepository;
        this.courseNodeRepository = courseNodeRepository;
        this.organizationRepository = organizationRepository;
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
        return courseRepository.save(course);
    }

    @Transactional
    public Course update(Long id, UpdateCourseRequest request) {
        Course course = getById(id);
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
        return course;
    }

    @Transactional
    public void delete(Long id) {
        Course course = getById(id);
        courseRepository.delete(course);
    }

    @Transactional
    public Course archive(Long id) {
        Course course = getById(id);
        course.setStatus(CourseStatus.ARCHIVED);
        return course;
    }

    @Transactional
    public Course unarchive(Long id) {
        Course course = getById(id);
        if (course.getStatus() == CourseStatus.ARCHIVED) {
            course.setStatus(CourseStatus.DRAFT);
        }
        return course;
    }

    @Transactional(readOnly = true)
    public CourseTreeResponse tree(Long id) {
        Course course = getById(id);
        List<CourseNode> nodes = courseNodeRepository.findByCourse_IdOrderBySortOrderAsc(id);
        return CourseTreeResponse.build(course, nodes);
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
