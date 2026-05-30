package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.Address;
import com.example.thuedocosplay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByUserOrderByIsDefaultDescCreatedAtDesc(User user);
    Optional<Address> findByIdAndUser(Long id, User user);
    Optional<Address> findByUserAndIsDefaultTrue(User user);
}
