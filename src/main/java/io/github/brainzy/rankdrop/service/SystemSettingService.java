package io.github.brainzy.rankdrop.service;

import io.github.brainzy.rankdrop.entity.SystemSetting;
import io.github.brainzy.rankdrop.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSettingService {
    
    private final SystemSettingRepository systemSettingRepository;
    
    @Transactional
    public String rotateGameKey() {
        String newGameKey = "gk_" + UUID.randomUUID().toString().replace("-", "");
        
        SystemSetting setting = systemSettingRepository.findById("GAME_SECRET")
                .orElse(SystemSetting.builder()
                        .key("GAME_SECRET")
                        .build());
        
        setting.setValue(newGameKey);
        systemSettingRepository.save(setting);
        
        log.info("Rotated game key. New value: {}", newGameKey);
        return newGameKey;
    }
    
    @Transactional
    public void setGameKey(String newGameKey) {
        SystemSetting setting = systemSettingRepository.findById("GAME_SECRET")
                .orElse(SystemSetting.builder()
                        .key("GAME_SECRET")
                        .build());
        
        setting.setValue(newGameKey);
        systemSettingRepository.save(setting);
        
        log.info("Set game key. New value: {}", newGameKey);
    }
    
    @Transactional(readOnly = true)
    public String getGameKey() {
        return systemSettingRepository.findById("GAME_SECRET")
                .map(SystemSetting::getValue)
                .orElse(null);
    }
}
