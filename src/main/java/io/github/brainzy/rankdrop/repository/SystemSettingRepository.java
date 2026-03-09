package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
    @Query("SELECT s FROM SystemSetting s WHERE s.key = :key")
    Optional<SystemSetting> findByKey(@Param("key") String key);
}
