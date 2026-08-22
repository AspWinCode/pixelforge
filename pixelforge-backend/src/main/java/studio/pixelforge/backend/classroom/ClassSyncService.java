package studio.pixelforge.backend.classroom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.lms.SyncClassRequest;
import studio.pixelforge.backend.organization.Organization;
import studio.pixelforge.backend.organization.OrganizationRepository;
import studio.pixelforge.backend.user.User;
import studio.pixelforge.backend.user.UserRepository;
import studio.pixelforge.backend.user.UserRole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClassSyncService {

    private static final Long HARDCODED_ORG_ID = 1L;

    private final ClassEntityRepository classEntityRepository;
    private final ClassMemberRepository classMemberRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public ClassSyncService(ClassEntityRepository classEntityRepository,
                             ClassMemberRepository classMemberRepository,
                             UserRepository userRepository,
                             OrganizationRepository organizationRepository) {
        this.classEntityRepository = classEntityRepository;
        this.classMemberRepository = classMemberRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public ClassEntity sync(SyncClassRequest request) {
        Organization organization = organizationRepository.findById(HARDCODED_ORG_ID)
            .orElseThrow();

        ClassEntity classEntity = classEntityRepository
            .findByOrganization_IdAndLmsClassId(HARDCODED_ORG_ID, request.lmsClassId())
            .orElseGet(() -> new ClassEntity(organization, request.lmsClassId(), request.className()));
        classEntity.setName(request.className());
        classEntity = classEntityRepository.save(classEntity);

        Set<User> newMembers = new HashSet<>();
        for (SyncClassRequest.Member m : request.members()) {
            User user = userRepository.findByOrganization_IdAndLmsUserId(HARDCODED_ORG_ID, m.lmsUserId())
                .orElseGet(() -> new User(organization, m.lmsUserId(), UserRole.valueOf(m.role().toUpperCase()), m.fullName()));
            user.setFullName(m.fullName());
            newMembers.add(userRepository.save(user));
        }

        List<ClassMember> existing = classMemberRepository.findByClassEntity_Id(classEntity.getId());
        Set<Long> newMemberIds = newMembers.stream().map(User::getId).collect(Collectors.toSet());

        for (ClassMember cm : existing) {
            if (!newMemberIds.contains(cm.getUser().getId())) {
                classMemberRepository.delete(cm);
            }
        }

        Set<Long> existingMemberIds = existing.stream().map(cm -> cm.getUser().getId()).collect(Collectors.toSet());
        for (User user : newMembers) {
            if (!existingMemberIds.contains(user.getId())) {
                classMemberRepository.save(new ClassMember(classEntity, user));
            }
        }

        return classEntity;
    }
}
