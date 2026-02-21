package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
    
    Optional<SystemSetting> findByKey(String key);
}
