package org.krylivi.sp.service.dto;

import org.krylivi.sp.model.ServiceStatus;

public record UpdateServiceStatusRequest(Long id, ServiceStatus status) {
}
