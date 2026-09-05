package studio.pixelforge.backend.portal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.organization.Organization;
import studio.pixelforge.backend.organization.OrganizationRepository;
import studio.pixelforge.backend.user.User;
import studio.pixelforge.backend.user.UserRepository;
import studio.pixelforge.backend.user.UserRole;

// Заводит/находит ученика, пришедшего по SSO из кабинета. В отличие от
// ростер-синка LMS (AuthController#ssoLogin) здесь допустимо создание
// аккаунта на лету: кабинет — доверенный источник, а ученик может ни разу
// не состоять в классе PixelForge.
@Service
public class PortalStudentService {

    // Тот же единственный tenant, что и в ClassSyncService/AuthController.
    private static final Long ORG_ID = 1L;

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public PortalStudentService(UserRepository userRepository,
                                 OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public User findOrCreate(PortalSsoClaims claims) {
        return findOrCreate(claims.externalRef(), claims.fullName());
    }

    // externalRef — "lp-student-{N}". fullName может быть null (например,
    // при зачислении на курс до первого захода ученика — §8.1).
    @Transactional
    public User findOrCreate(String externalRef, String fullName) {
        return userRepository.findByOrganization_IdAndExternalRef(ORG_ID, externalRef)
            .map(user -> {
                if (fullName != null && !fullName.isBlank() && !fullName.equals(user.getFullName())) {
                    user.setFullName(fullName);
                }
                return user;
            })
            .orElseGet(() -> {
                Organization org = organizationRepository.findById(ORG_ID)
                    .orElseGet(() -> organizationRepository.save(new Organization("PixelForge")));

                User user = new User(org, externalRef, UserRole.STUDENT,
                    fullName != null && !fullName.isBlank() ? fullName : "Ученик");
                user.setExternalRef(externalRef);
                return userRepository.save(user);
            });
    }
}
