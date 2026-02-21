package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for configuring backup settings")
public record BackupConfigRequest(
        @Schema(description = "Number of days to retain backups", example = "7")
        Integer retentionDays,
        
        @Schema(description = "Directory path for backup files", example = "./backups")
        String backupPath
) {}
