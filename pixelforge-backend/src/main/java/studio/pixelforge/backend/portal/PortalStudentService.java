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
        return userRepository.findByOrganization_IdAndExternalRef(ORG_ID, claims.externalRef())
            .map(user -> {
                // Имя в кабинете могло измениться — подтягиваем актуальное.
                if (claims.fullName() != null && !claims.fullName().equals(user.getFullName())) {
                    user.setFullName(claims.fullName());
                }
                return user;
            })
            .orElseGet(() -> {
                Organization org = organizationRepository.findById(ORG_ID)
                    .orElseGet(() -> organizationRepository.save(new Organization("PixelForge")));

                User user = new User(org, claims.externalRef(), UserRole.STUDENT,
                    claims.fullName() != null ? claims.fullName() : "Ученик");
                user.setExternalRef(claims.externalRef());
                return userRepository.save(user);
            });
    }
}
