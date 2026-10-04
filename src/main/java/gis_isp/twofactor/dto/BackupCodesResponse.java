package gis_isp.twofactor.dto;

import java.util.List;

public record BackupCodesResponse(
        List<String> backupCodes
) {
}