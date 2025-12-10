package com.sims.simscoreservice.shared.util;

import jakarta.servlet.http.HttpServletRequest;

import static com.sims.common.constants.AppConstants.IP_ADDRESS_HEADER;
import static com.sims.common.constants.AppConstants.USER_AGENT_HEADER;

public class HttpRequestUtil {

    /**
     * Extracts the IP address from the HttpServletRequest.
     * @param request The HttpServletRequest object.
     * @return The extracted IP address.
     */
    public static String extractIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader(IP_ADDRESS_HEADER);

        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        } else {
            // The first one is the client's real IP
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    /**
     * Extracts the User-Agent from the HttpServletRequest.
     * @param request The HttpServletRequest object.
     * @return The extracted User-Agent, truncated to 255 characters if necessary.
     */
    public static String extractUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader(USER_AGENT_HEADER);

        if (userAgent != null && userAgent.length() > 255) {
            userAgent = userAgent.substring(0, 255);
        }
        return userAgent;
    }
}
