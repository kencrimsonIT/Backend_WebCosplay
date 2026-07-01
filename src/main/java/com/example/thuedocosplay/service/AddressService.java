package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.AddressRequest;
import com.example.thuedocosplay.dto.response.AddressResponse;
import com.example.thuedocosplay.entity.Address;
import com.example.thuedocosplay.entity.User;
import com.example.thuedocosplay.repository.AddressRepository;
import com.example.thuedocosplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<AddressResponse> getMyAddresses(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return addressRepository.findAllByUserOrderByIsDefaultDescCreatedAtDesc(user)
                .stream()
                .map(AddressResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse addAddress(String email, AddressRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (request.isDefault()) {
            handleDefaultAddress(user);
        }

        Address address = Address.builder()
                .user(user)
                .fullAddress(request.getFullAddress())
                .label(request.getLabel())
                .isDefault(request.isDefault())
                .build();

        return AddressResponse.fromEntity(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(Long id, String email, AddressRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));

        if (request.isDefault() && !address.isDefault()) {
            handleDefaultAddress(user);
        }

        address.setFullAddress(request.getFullAddress());
        address.setLabel(request.getLabel());
        address.setDefault(request.isDefault());

        return AddressResponse.fromEntity(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
        
        addressRepository.delete(address);
    }

    private void handleDefaultAddress(User user) {
        addressRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(oldDefault -> {
                    oldDefault.setDefault(false);
                    addressRepository.save(oldDefault);
                });
    }
}
