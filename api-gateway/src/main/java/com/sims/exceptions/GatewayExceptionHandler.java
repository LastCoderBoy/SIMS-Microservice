
/**
 * Exception handler for API Gateway
 * Handles gateway-level errors (routing failures, circuit breaker, etc.)
 *
 * @author LastCoderBoy
 * @since 2025-01-17
 */
@Component
@Order(-1)  // High priority
@Slf4j
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("[API-GATEWAY] Error: {}", ex.getMessage(), ex);

        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<Void> errorResponse = ApiResponse.error(
                "Gateway error: " + ex.getMessage()
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse()
                            .bufferFactory()
                            .wrap(bytes)));
        } catch (Exception e) {
            log.error("[API-GATEWAY] Failed to write error response", e);
            return exchange.getResponse().setComplete();
        }
    }
}