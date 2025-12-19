import api from '../../api/axios.js';
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL;

const AUTH_ENDPOINTS = {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    LOGOUT_ALL: '/auth/logout-all',
    REFRESH_TOKEN: '/auth/refresh',
    UPDATE_USER: '/auth/update',
};

class AuthService {
    /**
     * Login function
     * @param {Object} credentials - { username, password }
     * @returns {Promise<Object>} User data and tokens
     */
    async login(credentials) {
        try {
            // Transform username to login field as per backend
            const loginRequest = {
                login: credentials.username,
                password: credentials.password,
            };

            const response = await axios.post(
                `${API_BASE_URL}${AUTH_ENDPOINTS.LOGIN}`,
                loginRequest,
                {
                    withCredentials: true, // Important:  Receive refresh token cookie
                }
            );

            // Backend returns:  { success, message, data:  TokenResponse }
            if (response.data.success && response.data.data) {
                const tokenData = response.data.data;

                // Store access token
                if (tokenData.accessToken) {
                    localStorage.setItem('accessToken', tokenData.accessToken);
                }

                // Store user info
                const userData = {
                    username: tokenData.username,
                    role: tokenData.role,
                };
                localStorage.setItem('user', JSON.stringify(userData));

                return {
                    success: true,
                    message: response.data.message,
                    user: userData,
                    tokenType: tokenData.tokenType,
                    expiresIn: tokenData.expiresIn,
                };
            } else {
                throw new Error(response.data.message || 'Login failed');
            }
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Logout function
     * Requires access token in Authorization header
     */
    async logout() {
        try {
            const token = this.getAccessToken();

            if (token) {
                await api.post(AUTH_ENDPOINTS.LOGOUT);
            }
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            // Clear local storage regardless of API call success
            this.clearAuthData();
        }
    }

    /**
     * Logout from all devices
     */
    async logoutAllDevices() {
        try {
            await api.post(AUTH_ENDPOINTS.LOGOUT_ALL);
        } catch (error) {
            console.error('Logout all devices error:', error);
        } finally {
            this.clearAuthData();
        }
    }

    /**
     * Update user information
     * @param {Object} userData - { firstName?, lastName?, password? }
     */
    async updateUser(userData) {
        try {
            const response = await api.put(AUTH_ENDPOINTS.UPDATE_USER, userData);

            if (response.data.success) {
                // If password was updated, user needs to re-login
                if (userData.password) {
                    return {
                        success: true,
                        message: response.data.message,
                        requireRelogin: true,
                    };
                }

                return {
                    success: true,
                    message: response.data.message,
                    requireRelogin: false,
                };
            } else {
                throw new Error(response.data.message || 'Update failed');
            }
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get current user from localStorage
     */
    getCurrentUser() {
        const userStr = localStorage.getItem('user');
        if (userStr) {
            try {
                return JSON.parse(userStr);
            } catch (e) {
                console.error('Error parsing user data:', e);
                return null;
            }
        }
        return null;
    }

    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        const token = localStorage.getItem('accessToken');
        return !!token;
    }

    /**
     * Get access token
     */
    getAccessToken() {
        return localStorage.getItem('accessToken');
    }

    /**
     * Clear all authentication data
     */
    clearAuthData() {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('user');
    }

    /**
     * Refresh access token
     * Note:  Refresh token is stored in HttpOnly cookie (secure)
     */
    async refreshToken() {
        try {
            const response = await axios.post(
                `${API_BASE_URL}${AUTH_ENDPOINTS.REFRESH_TOKEN}`,
                {},
                {
                    withCredentials: true, // Send refresh token cookie
                }
            );

            if (response.data.success && response.data.data.accessToken) {
                const tokenData = response.data.data;

                // Store new access token
                localStorage.setItem('accessToken', tokenData.accessToken);

                // Update user info
                const userData = {
                    username: tokenData.username,
                    role: tokenData.role,
                };
                localStorage.setItem('user', JSON.stringify(userData));

                return tokenData;
            } else {
                throw new Error('Token refresh failed');
            }
        } catch (error) {
            this.clearAuthData();
            throw error;
        }
    }

    /**
     * Error handler
     */
    handleError(error) {
        if (error.response) {
            // Server responded with error
            const errorData = error.response.data;

            return {
                message: errorData.message || 'An error occurred',
                status:  error.response.status,
                success: false,
            };
        } else if (error.request) {
            // Request made but no response
            return {
                message: 'No response from server. Please check your connection.',
                status: null,
                success: false,
            };
        } else {
            // Something else happened
            return {
                message:  error.message || 'An unexpected error occurred',
                status: null,
                success: false,
            };
        }
    }
}

export default new AuthService();