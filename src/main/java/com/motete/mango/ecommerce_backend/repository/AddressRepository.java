package com.motete.mango.ecommerce_backend.repository;

import com.motete.mango.ecommerce_backend.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser_id(Long id);

}
