package io.github.kstnnn.ai.interview.service.service;

import java.util.UUID;

public interface UserLookupService {

  UUID resolveInternalUserId(String providerUserId);
}
