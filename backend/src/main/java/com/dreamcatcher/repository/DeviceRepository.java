package com.dreamcatcher.repository;

import com.dreamcatcher.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Device entity operations.
 * Key query: checking if a device fingerprint has already used the free trial.
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceFingerprint(String deviceFingerprint);

    List<Device> findByUserId(Long userId);

    boolean existsByDeviceFingerprintAndFreeTrialUsedTrue(String deviceFingerprint);

}
