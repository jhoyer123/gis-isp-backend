package gis_isp.twofactor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TwoFactorRepository extends JpaRepository<UserTwoFactorAuthEntity, UUID> {
}
