package software.amazon.vpclattice.common;

import software.amazon.awssdk.services.vpclattice.model.AccessDeniedException;
import software.amazon.awssdk.services.vpclattice.model.ConflictException;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ResourceNotFoundException;
import software.amazon.awssdk.services.vpclattice.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.awssdk.services.vpclattice.model.ValidationException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.ProgressEvent;

public class ExceptionHandler {
    private ExceptionHandler() {
        //prevent instantiation
    }

    public static <ModelT, CallbackT> ProgressEvent<ModelT, CallbackT> handleExceptionAndReturnProgressEvent(Exception exception) {
        if (exception instanceof LatticeNotStabilizedException) {
            final var e = (LatticeNotStabilizedException) exception;

            return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.NotStabilized);
        }

        if (exception instanceof ValidationException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);
        }
        if (exception instanceof ConflictException && exception.getMessage() != null
                && exception.getMessage().toLowerCase().contains("already exist")) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.AlreadyExists);
        }
        if (exception instanceof ConflictException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);
        }
        if (exception instanceof AccessDeniedException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.AccessDenied);
        }
        if (exception instanceof InternalServerException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ServiceInternalError);
        }
        if (exception instanceof ServiceQuotaExceededException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ServiceLimitExceeded);
        }
        if (exception instanceof ThrottlingException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.Throttling);
        }
        if (exception instanceof ResourceNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);
        }

        if (exception instanceof BaseHandlerException) {
            final var e = (BaseHandlerException) exception;

            return ProgressEvent.defaultFailureHandler(e, e.getErrorCode());
        }

        return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InternalFailure);
    }

    public static <RequestT, ResponseT, ClientT, ModelT, CallbackT> ProgressEvent<ModelT, CallbackT> handleError(
            RequestT _request,
            Exception exception,
            ClientT _client,
            ModelT model,
            CallbackT _context) {
        final var event = ExceptionHandler.<ModelT, CallbackT>handleExceptionAndReturnProgressEvent(exception);

        // Set model for CFN to know what to rollback
        if (model != null) {
            event.setResourceModel(model);
        }

        return event;
    }
}
