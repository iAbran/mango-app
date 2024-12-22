package com.motete.mango.ecommerce_backend.service;

import com.motete.mango.ecommerce_backend.api.model.LoginBody;
import com.motete.mango.ecommerce_backend.api.model.RegistrationBody;
import com.motete.mango.ecommerce_backend.exception.EmailFailureException;
import com.motete.mango.ecommerce_backend.exception.UserAlreadyExistsException;
import com.motete.mango.ecommerce_backend.exception.UserNotVerifiedException;
import com.motete.mango.ecommerce_backend.model.LocalUser;
import com.motete.mango.ecommerce_backend.model.VerificationToken;
import com.motete.mango.ecommerce_backend.repository.LocalUserRepository;
import com.motete.mango.ecommerce_backend.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final LocalUserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EncryptionService encryptionService;
    private final JWTService jwtService;
    private final EmailService emailService;

    public UserService(LocalUserRepository userRepository,
                       VerificationTokenRepository verificationTokenRepository,
                       EncryptionService encryptionService,
                       JWTService jwtService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public void registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException, EmailFailureException {
        if (userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(
                registrationBody.getUsername(), registrationBody.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        LocalUser user = new LocalUser();
        user.setUsername(registrationBody.getUsername());
        user.setEmail(registrationBody.getEmail());
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());

        VerificationToken verificationToken = new VerificationToken();
        emailService.sendVerificationEmail(verificationToken);
        verificationTokenRepository.save(verificationToken);

        userRepository.save(user);
    }

    private VerificationToken createVerificationToken(LocalUser user) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(user));
        verificationToken.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        verificationToken.setUser(user);
        user.getVerificationTokens().add(verificationToken);

        return verificationToken;
    }

    public String loginUser(LoginBody loginBody) throws UserNotVerifiedException, EmailFailureException {
        Optional<LocalUser> userOptional = userRepository.findByUsernameIgnoreCase(loginBody.getUsername());

        if (userOptional.isPresent()) {
            LocalUser user = userOptional.get();
            if (encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())) {
                if (user.isEmailVerified()) {
                    return jwtService.generateJWT(user);
                } else {
                    List<VerificationToken> verificationTokens = user.getVerificationTokens();
                    boolean resent = verificationTokens.isEmpty() ||
                            verificationTokens.getFirst().getCreatedTimestamp().before(new Timestamp(System.currentTimeMillis() - (60 * 60 * 1000)));

                    if (resent) {
                        VerificationToken verificationToken = createVerificationToken(user);
                        verificationTokenRepository.save(verificationToken);
                        emailService.sendVerificationEmail(verificationToken);
                    }
                    throw new UserNotVerifiedException(resent);
                }
            }
        }
        return  null;
    }

    @Transactional
    public boolean verifyUser(String token) {
        Optional<VerificationToken> tokenOptional = verificationTokenRepository.findByToken(token);

        if (tokenOptional.isPresent()) {
            VerificationToken verificationToken = tokenOptional.get();
            LocalUser user = verificationToken.getUser();

            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                userRepository.save(user);
                verificationTokenRepository.deleteByUser(user);
                return true;
            }
        }
        return false;
    }

    public List<LocalUser> getAllUsers() {
        return userRepository.findAll();
    }
}
