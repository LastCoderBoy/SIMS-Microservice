import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL;

// Create axios instance
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000, // 10 seconds
    withCredentials: true, // Important: Send cookies with requests (for refresh token)
});

// Request Interceptor - Add JWT token to every request
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response Interceptor - Handle token refresh and errors
api.interceptors.response.use(
    (response) => {
        return response;
    },
    async (error) => {
        const originalRequest = error.config;

        // Handle 401 Unauthorized - Try to refresh token
        if (error.response && error.response.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                // Try to refresh the token
                const refreshResponse = await axios.post(
                    `${API_BASE_URL}/auth/refresh`,
                    {},
                    {
                        withCredentials: true, // Send refresh token cookie
                    }
                );

                if (refreshResponse.data.success && refreshResponse.data.data.accessToken) {
                    const newAccessToken = refreshResponse.data.data.accessToken;

                    // Store new access token
                    localStorage.setItem('accessToken', newAccessToken);

                    // Update user info if provided
                    if (refreshResponse.data.data.username) {
                        const userData = {
                            username: refreshResponse.data.data.username,
                            role: refreshResponse.data.data.role,
                        };
                        localStorage.setItem('user', JSON.stringify(userData));
                    }

                    // Retry original request with new token
                    originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                    return api(originalRequest);
                }
            } catch (refreshError) {
                // Refresh token failed - logout user
                console.error('Token refresh failed:', refreshError);
                localStorage.removeItem('accessToken');
                localStorage.removeItem('user');
                window.location.href = '/login';
                return Promise.reject(refreshError);
            }
        }

        // Handle 403 Forbidden - No permission
        if (error.response && error.response.status === 403) {
            console.error('Access forbidden - insufficient permissions');
        }

        return Promise.reject(error);
    }
);

export default api;