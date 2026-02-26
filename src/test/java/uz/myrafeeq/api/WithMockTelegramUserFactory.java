package uz.myrafeeq.api;

import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockTelegramUserFactory
    implements WithSecurityContextFactory<WithMockTelegramUser> {

  @Override
  public SecurityContext createSecurityContext(WithMockTelegramUser annotation) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(
        new UsernamePasswordAuthenticationToken(annotation.telegramId(), null, List.of()));
    return context;
  }
}
