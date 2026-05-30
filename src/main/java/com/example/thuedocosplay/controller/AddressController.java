package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.AddressRequest;
import com.example.thuedocosplay.dto.response.AddressResponse;
import com.example.thuedocosplay.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getMyAddresses(Principal principal) {
        return ResponseEntity.ok(addressService.getMyAddresses(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            Principal principal,
            @Valid @RequestBody AddressRequest request
    ) {
        return ResponseEntity.ok(addressService.addAddress(principal.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody AddressRequest request
    ) {
        return ResponseEntity.ok(addressService.updateAddress(id, principal.getName(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id, Principal principal) {
        addressService.deleteAddress(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
