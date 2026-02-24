package uz.myrafeeq.api.security;

import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthenticatedUser {

  private AuthenticatedUser() {}

  public static Long getTelegramId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    return (Long) auth.getPrincipal();
  }
}
