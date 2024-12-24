package com.motete.mango.ecommerce_backend.service;

import com.motete.mango.ecommerce_backend.api.model.LoginBody;
import com.motete.mango.ecommerce_backend.api.model.RegistrationBody;
import com.motete.mango.ecommerce_backend.exception.*;
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

    public LocalUser registerUser(RegistrationBody registrationBody) throws EmailFailureException {
        if (userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(
                registrationBody.getUsername(), registrationBody.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(
                    "User with the Email '"+registrationBody.getEmail()+"' or Username '"+registrationBody.getUsername()+"' already exists.");
        }
        LocalUser user = new LocalUser();
        user.setEmail(registrationBody.getEmail());
        user.setUsername(registrationBody.getUsername());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));

        VerificationToken verificationToken = createVerificationToken(user);
        try {
            emailService.sendVerificationEmail(verificationToken);
        } catch (EmailFailureException e) {
            throw new EmailFailureException("Failed to send verification email to '"+ verificationToken.getUser().getEmail()+"'", e);
        }
        return userRepository.save(user);
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
        } else {
            throw new UserNotFoundException("User with the Username '"+loginBody.getUsername()+"' is not registered");
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
            } else {
                throw new EmailAlreadyVerifiedException("User with the Email '"+user.getEmail()+"' is already verified");
            }
        } else {
            throw new TokenNotFoundException("Token '"+token+"' does not exists");
        }
    }

    public List<LocalUser> getAllUsers() {
        return userRepository.findAll();
    }
}
