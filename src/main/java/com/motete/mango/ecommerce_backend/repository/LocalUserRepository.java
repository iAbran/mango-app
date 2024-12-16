package com.motete.mango.ecommerce_backend.repository;

import com.motete.mango.ecommerce_backend.model.LocalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocalUserRepository extends JpaRepository<LocalUser, Long> {

    Optional<LocalUser> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);
}
