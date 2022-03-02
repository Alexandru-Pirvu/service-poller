package org.krylivi.sp.message;

import org.krylivi.sp.model.ServiceStatus;

public record ServiceStatusUpdated(Long id, ServiceStatus status) {
}
