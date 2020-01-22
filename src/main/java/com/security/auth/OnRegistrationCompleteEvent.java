package com.security.auth;

import com.entities.Account;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Getter
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private Account user;

    public OnRegistrationCompleteEvent(
            Account user) {
        super(user);

        this.user = user;
    }

    // standard getters and setters
}
