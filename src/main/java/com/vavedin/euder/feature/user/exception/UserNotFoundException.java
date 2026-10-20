package com.vavedin.euder.feature.user.exception;

import com.vavedin.euder.common.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String memberId) {
        super("No user was found for id " + memberId);
    }
}
