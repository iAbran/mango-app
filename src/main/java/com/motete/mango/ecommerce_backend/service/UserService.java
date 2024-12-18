package com.motete.mango.ecommerce_backend.service;

import com.motete.mango.ecommerce_backend.api.model.LoginBody;
import com.motete.mango.ecommerce_backend.api.model.RegistrationBody;
import com.motete.mango.ecommerce_backend.exception.UserAlreadyExistsException;
import com.motete.mango.ecommerce_backend.model.LocalUser;
import com.motete.mango.ecommerce_backend.repository.LocalUserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final LocalUserRepository userRepository;
    private final EncryptionService encryptionService;
    private final JWTService jwtService;

    public UserService(LocalUserRepository userRepository,
                       EncryptionService encryptionService,
                       JWTService jwtService) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
    }

    public LocalUser registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException{
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

        return userRepository.save(user);
    }

    public String loginUser(LoginBody loginBody) {
        Optional<LocalUser> optionalUser = userRepository.findByUsernameIgnoreCase(loginBody.getUsername());

        if (optionalUser.isPresent()) {
            LocalUser user = optionalUser.get();
            if (encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())) {
                return jwtService.generateJWT(user);
            }
        }
        return  null;
    }
}