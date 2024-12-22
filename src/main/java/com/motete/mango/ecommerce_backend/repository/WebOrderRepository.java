package com.motete.mango.ecommerce_backend.repository;

import com.motete.mango.ecommerce_backend.model.LocalUser;
import com.motete.mango.ecommerce_backend.model.WebOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebOrderRepository extends JpaRepository<WebOrder, Long> {

    List<WebOrder> findByUser(LocalUser user);
}
