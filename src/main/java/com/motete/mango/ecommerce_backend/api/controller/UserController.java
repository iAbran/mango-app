package com.motete.mango.ecommerce_backend.api.controller;

import com.motete.mango.ecommerce_backend.model.Address;
import com.motete.mango.ecommerce_backend.model.DataChange;
import com.motete.mango.ecommerce_backend.model.LocalUser;
import com.motete.mango.ecommerce_backend.repository.AddressRepository;
import com.motete.mango.ecommerce_backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AddressRepository addressRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;

    public UserController(AddressRepository addressRepository, SimpMessagingTemplate simpMessagingTemplate,
                          UserService userService) {

        this.addressRepository = addressRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userService = userService;
    }

    @GetMapping("/{userId}/address")
    public ResponseEntity<List<Address>> getAddress(@AuthenticationPrincipal LocalUser user,
                                                    @PathVariable Long userId) {

        if (!userService.userHasPermissionToUser(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(addressRepository.findByUser_id(userId));
    }

    @PutMapping("/{userId}/address")
    public ResponseEntity<Address> putAddress(@AuthenticationPrincipal LocalUser user,
                                              @PathVariable Long userId, @RequestBody Address address) {

        if (!userService.userHasPermissionToUser(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        address.setId(null);
        LocalUser refUser = new LocalUser();
        refUser.setId(userId);
        address.setUser(refUser);
        Address savedAddress = addressRepository.save(address);
        simpMessagingTemplate.convertAndSend("/topic/user/"+userId+"/address",
                new DataChange<>(DataChange.ChangeType.INSERT, address));

        return  ResponseEntity.ok(savedAddress);
    }

    @PatchMapping("/{userId}/address/{addressId}")
    public ResponseEntity<Address> patchAddress(@AuthenticationPrincipal LocalUser user,
                                                @PathVariable Long userId,
                                                @PathVariable Long addressId,
                                                @RequestBody Address address) {

        if (!userService.userHasPermissionToUser(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (Objects.equals(address.getId(), addressId)) {
            Optional<Address> originalAddressOpt = addressRepository.findById(addressId);

            if (originalAddressOpt.isPresent()) {
                LocalUser originalUser = originalAddressOpt.get().getUser();

                if (Objects.equals(originalUser.getId(), userId)) {
                    address.setUser(originalUser);
                    Address savedAddress = addressRepository.save(address);
                    simpMessagingTemplate.convertAndSend("/topic/user/"+userId+"/address",
                            new DataChange<>(DataChange.ChangeType.UPDATE, address));

                    return  ResponseEntity.ok(savedAddress);
                }
            }
        }
        return ResponseEntity.badRequest().build();
    }

}
