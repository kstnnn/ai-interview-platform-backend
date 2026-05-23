package io.github.kstnnn.organization.service.service;

import io.github.kstnnn.organization.service.dto.UserLookupResponse;
import io.github.kstnnn.organization.service.exception.BusinessUserRequiredException;
import io.github.kstnnn.organization.service.exception.CandidateUserRequiredException;
import io.github.kstnnn.organization.service.model.UserStatus;
import io.github.kstnnn.organization.service.model.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

  private final UserLookupService userLookupService;

  public UserLookupResponse resolve(Jwt jwt) {
    return userLookupService.resolveByProviderUserId(jwt.getSubject());
  }

  public UserLookupResponse requireActiveUser(Jwt jwt) {
    var user = resolve(jwt);
    if (user.userStatus() != UserStatus.ACTIVE) {
      throw new BusinessUserRequiredException();
    }
    return user;
  }

  public UserLookupResponse requireActiveCandidateUser(Jwt jwt) {
    var user = resolve(jwt);
    if (user.userType() != UserType.PERSONAL || user.userStatus() != UserStatus.ACTIVE) {
      throw new CandidateUserRequiredException();
    }
    return user;
  }

  public UserLookupResponse requireActiveBusinessUser(Jwt jwt) {
    var user = resolve(jwt);
    if (user.userType() != UserType.BUSINESS || user.userStatus() != UserStatus.ACTIVE) {
      throw new BusinessUserRequiredException();
    }
    return user;
  }
}
