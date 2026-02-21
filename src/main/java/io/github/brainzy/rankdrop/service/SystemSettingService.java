package io.github.brainzy.rankdrop.service;

import io.github.brainzy.rankdrop.entity.SystemSetting;
import io.github.brainzy.rankdrop.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;

    @Transactional(readOnly = true)
    public String getSetting(String key, String defaultValue) {
        return systemSettingRepository.findById(key)
                .map(SystemSetting::getValue)
                .orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public String getSetting(String key) {
        return systemSettingRepository.findById(key)
                .map(SystemSetting::getValue)
                .orElse(null);
    }

    @Transactional
    public void setSetting(String key, String value) {
        SystemSetting setting = systemSettingRepository.findById(key)
                .orElse(SystemSetting.builder()
                        .key(key)
                        .build());

        setting.setValue(value);
        systemSettingRepository.save(setting);
    }
}
