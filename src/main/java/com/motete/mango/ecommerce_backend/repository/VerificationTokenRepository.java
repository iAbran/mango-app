package com.motete.mango.ecommerce_backend.repository;

import com.motete.mango.ecommerce_backend.model.LocalUser;
import com.motete.mango.ecommerce_backend.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    void deleteByUser(LocalUser user);
}
