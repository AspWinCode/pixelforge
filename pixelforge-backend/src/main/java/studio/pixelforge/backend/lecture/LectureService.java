package studio.pixelforge.backend.lecture;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.organization.Organization;
import studio.pixelforge.backend.organization.OrganizationRepository;
import studio.pixelforge.backend.user.User;
import studio.pixelforge.backend.user.UserRepository;

import java.util.List;

@Service
public class LectureService {

    // Пока одна школа — та же схема, что и в остальном проекте.
    private static final Long HARDCODED_ORG_ID = 1L;

    private final LectureRepository lectureRepository;
    private final LectureCardRepository lectureCardRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public LectureService(LectureRepository lectureRepository,
                           LectureCardRepository lectureCardRepository,
                           LectureProgressRepository lectureProgressRepository,
                           OrganizationRepository organizationRepository,
                           UserRepository userRepository) {
        this.lectureRepository = lectureRepository;
        this.lectureCardRepository = lectureCardRepository;
        this.lectureProgressRepository = lectureProgressRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Lecture create(String title) {
        Organization organization = organizationRepository.findById(HARDCODED_ORG_ID).orElseThrow();
        return lectureRepository.save(new Lecture(organization, title));
    }

    @Transactional
    public LectureCard addCard(Long lectureId, CardType cardType, String content) {
        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new EntityNotFoundException("Lecture not found: " + lectureId));

        int nextPosition = lectureCardRepository.findByLecture_IdOrderByPositionAsc(lectureId).size();
        return lectureCardRepository.save(new LectureCard(lecture, nextPosition, cardType, content));
    }

    @Transactional(readOnly = true)
    public List<Lecture> listAll() {
        return lectureRepository.findByOrganization_Id(HARDCODED_ORG_ID);
    }

    @Transactional(readOnly = true)
    public List<LectureCard> getCards(Long lectureId) {
        return lectureCardRepository.findByLecture_IdOrderByPositionAsc(lectureId);
    }

    @Transactional(readOnly = true)
    public boolean isCompleted(Long lectureId, Long userId) {
        return lectureProgressRepository.findByLecture_IdAndUser_Id(lectureId, userId).isPresent();
    }

    // Идемпотентно — повторная отметка "прочитано" для уже пройденной
    // лекции просто ничего не делает, а не падает ошибкой уникальности.
    @Transactional
    public void markCompleted(Long lectureId, Long userId) {
        if (lectureProgressRepository.findByLecture_IdAndUser_Id(lectureId, userId).isPresent()) {
            return;
        }
        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new EntityNotFoundException("Lecture not found: " + lectureId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        lectureProgressRepository.save(new LectureProgress(lecture, user));
    }
}
