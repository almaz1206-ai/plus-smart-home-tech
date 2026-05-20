package ru.practicum.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.exception.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleProductNotFoundException(ProductNotFoundException ex) {

        ApiError response = createResponse(
                ex,
                "Товар не найден. Пожалуйста, проверьте запрос.",
                HttpStatus.NOT_FOUND
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotAuthorizedUserException.class)
    public ResponseEntity<ApiError> handleNotAuthorizedUserException(NotAuthorizedUserException ex) {

        ApiError response = createResponse(
                ex,
                "Пользователь не указан. Пожалуйста, проверьте запрос.",
                HttpStatus.UNAUTHORIZED
        );

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException ex) {

        ApiError response = createResponse(
                ex,
                "Ресурс не найден.",
                HttpStatus.NOT_FOUND
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    public ResponseEntity<ApiError> handleSpecifiedProductAlreadyInWarehouseException(
            SpecifiedProductAlreadyInWarehouseException ex) {

        ApiError response = createResponse(
                ex,
                "Ошибка, товар с таким описанием уже зарегистрирован на складе",
                HttpStatus.BAD_REQUEST
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    public ResponseEntity<ApiError> handleNoSpecifiedProductInWarehouseException(
            NoSpecifiedProductInWarehouseException ex) {

        ApiError response = createResponse(
                ex,
                "Нет информации о товаре на складе",
                HttpStatus.BAD_REQUEST
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(BadRequestException ex) {

        ApiError response = createResponse(
                ex,
                "Некорректный запрос.",
                HttpStatus.BAD_REQUEST
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        return ((FieldError) error).getField() + ": " + error.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.joining(", "));

        ApiError response = createResponse(
                ex,
                "Ошибка валидации: " + errorMessage,
                HttpStatus.BAD_REQUEST
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {

        ApiError response = createResponse(
                ex,
                "Внутренняя ошибка сервера",
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ApiError createResponse(Exception ex, String userMessage, HttpStatus httpStatus) {
        ApiError response = ApiError.builder()
                .message(ex.getMessage())
                .localizedMessage(ex.getLocalizedMessage())
                .userMessage(userMessage)
                .httpStatus(httpStatus.toString())
                .stackTrace(convertStackTrace(ex.getStackTrace()))
                .build();

        if (ex.getCause() != null) {
            Throwable cause = ex.getCause();
            ApiError.ReasonError reasonError = ApiError.ReasonError.builder()
                    .stackTrace(convertStackTrace(cause.getStackTrace()))
                    .message(cause.getMessage())
                    .localizedMessage(cause.getLocalizedMessage())
                    .build();
            response.setCause(reasonError);
        }

        if (ex.getSuppressed() != null && ex.getSuppressed().length > 0) {
            List<ApiError.ReasonError> suppressedList = Arrays.stream(ex.getSuppressed())
                    .map(sup -> ApiError.ReasonError.builder()
                            .stackTrace(convertStackTrace(sup.getStackTrace()))
                            .message(sup.getMessage())
                            .localizedMessage(sup.getLocalizedMessage())
                            .build())
                    .collect(Collectors.toList());
            response.setSuppressed(suppressedList);
        }

        return response;
    }

    private List<ApiError.StackTraceItem> convertStackTrace(StackTraceElement[] elements) {
        if (elements == null || elements.length == 0) {
            return List.of();
        }

        return Arrays.stream(elements)
                .map(el -> ApiError.StackTraceItem.builder()
                        .classLoaderName(el.getClassLoaderName())
                        .moduleName(el.getModuleName())
                        .moduleVersion(el.getModuleVersion())
                        .methodName(el.getMethodName())
                        .fileName(el.getFileName())
                        .lineNumber(el.getLineNumber())
                        .className(el.getClassName())
                        .nativeMethod(el.isNativeMethod())
                        .build())
                .collect(Collectors.toList());
    }
}
