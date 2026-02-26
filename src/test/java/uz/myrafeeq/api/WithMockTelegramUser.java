package uz.myrafeeq.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockTelegramUserFactory.class)
public @interface WithMockTelegramUser {

  long telegramId() default 123456789L;
}
