package com.example.cap1.global.exception;

import com.example.cap1.global.response.Code;

public class EntityNotFoundException extends GeneralException{

    public EntityNotFoundException(String message) {
        super(Code.ENTITY_NOT_FOUND, message);
    }
}
