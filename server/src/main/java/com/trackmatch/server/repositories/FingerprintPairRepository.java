package com.trackmatch.server.repositories;

import com.trackmatch.server.entities.FingerprintPair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FingerprintPairRepository extends JpaRepository<FingerprintPair, Long> {
    List<FingerprintPair> findByHashIn(List<Long> hashes);
}
