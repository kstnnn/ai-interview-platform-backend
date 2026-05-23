package io.github.kstnnn.organization.service.service;

import io.github.kstnnn.organization.service.dto.UserLookupResponse;

public interface UserLookupService {

  UserLookupResponse resolveByProviderUserId(String providerUserId);
}
