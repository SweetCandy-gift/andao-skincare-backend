package com.andao.skincare.common.exception;

import com.andao.skincare.common.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnBusinessCodeAndHttpStatusForBusinessException() {
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(
                new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        assertThat(response.getStatusCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.getHttpStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.getCode());
        assertThat(response.getBody().message()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.getMessage());
        assertThat(response.getBody().data()).isNull();
    }

    @Test
    void shouldHideUnknownExceptionDetails() {
        ResponseEntity<Result<Void>> response = handler.handleUnknownException(
                new IllegalStateException("database password must not be exposed"));

        assertThat(response.getStatusCode()).isEqualTo(ErrorCode.SYSTEM_ERROR.getHttpStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.SYSTEM_ERROR.getCode());
        assertThat(response.getBody().message()).isEqualTo(ErrorCode.SYSTEM_ERROR.getMessage());
    }

    @Test
    void shouldReturnParameterCodeForConstraintViolation() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("订单ID必须为正数");

        ResponseEntity<Result<Void>> response = handler.handleConstraintViolation(
                new ConstraintViolationException(Set.of(violation)));

        assertThat(response.getStatusCode()).isEqualTo(ErrorCode.PARAM_ERROR.getHttpStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.PARAM_ERROR.getCode());
        assertThat(response.getBody().message()).isEqualTo("订单ID必须为正数");
    }

    @Test
    void shouldBuildUnifiedSuccessResult() {
        Result<String> result = Result.success("ok");

        assertThat(result.code()).isZero();
        assertThat(result.message()).isEqualTo("success");
        assertThat(result.data()).isEqualTo("ok");
    }
}
