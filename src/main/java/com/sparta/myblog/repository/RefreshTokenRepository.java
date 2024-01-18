package com.sparta.myblog.repository;

import com.sparta.myblog.redis.redishash.RefreshToken;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByMemberId(Long id);
    void deleteByMemberId(Long id);
}