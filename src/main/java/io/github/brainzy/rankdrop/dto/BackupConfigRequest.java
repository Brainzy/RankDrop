package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for configuring backup settings")
public class BackupConfigRequest {
    @Schema(description = "Number of days to retain backups", example = "7")
    private Integer retentionDays;
    
    @Schema(description = "Directory path for backup files", example = "./backups")
    private String backupPath;
}
