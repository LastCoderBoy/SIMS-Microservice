import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../../services/userManagement/authService.js';
import './Login.css';
import logo from "../../../public/logo.png";

const Login = () => {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        username: '',
        password: '',
    });

    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    // Handle input changes
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value,
        });

        // Clear error for this field
        if (errors[name]) {
            setErrors({
                ...errors,
                [name]: '',
            });
        }

        // Clear general error message
        if (errorMessage) {
            setErrorMessage('');
        }
    };

    // Validate form
    const validateForm = () => {
        const newErrors = {};

        if (!formData.username.trim()) {
            newErrors.username = 'Username or email is required';
        }

        if (!formData.password) {
            newErrors.password = 'Password is required';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    // Handle form submission
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        setIsLoading(true);
        setErrorMessage('');

        try {
            const response = await authService.login({
                username: formData.username,
                password: formData.password,
            });

            if (response.success) {
                // Successful login - redirect to dashboard
                console.log('Login successful:', response.message);
                navigate('/dashboard');
            }
        } catch (error) {
            console.error('Login error:', error);
            setErrorMessage(
                error.message || 'Login failed. Please check your credentials and try again.'
            );
        } finally {
            setIsLoading(false);
        }
    };

    // Toggle password visibility
    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    return (
        <div className="login-container">
            <div className="login-background">
                <div className="login-overlay"></div>
            </div>

            <div className="login-content">
                <div className="login-card">
                    {/* Logo and Title */}
                    <div className="login-header">
                        <div className="login-logo">
                            <img src={logo} alt="SIMS Logo" className="logo-icon" />
                        </div>
                        <h1 className="login-title">SIMS</h1>
                        <p className="login-subtitle">Smart Inventory Management System</p>
                    </div>

                    {/* Login Form */}
                    <form className="login-form" onSubmit={handleSubmit} noValidate>
                        {/* Error Message */}
                        {errorMessage && (
                            <div className="error-alert">
                                <svg
                                    className="error-icon"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                                    <path d="M12 8V12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                                </svg>
                                <span>{errorMessage}</span>
                            </div>
                        )}

                        {/* Username/Email Field */}
                        <div className="form-group">
                            <label htmlFor="username" className="form-label">
                                Username or Email
                            </label>
                            <div className="input-wrapper">
                                <svg
                                    className="input-icon"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <path
                                        d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                    />
                                    <path
                                        d="M12 11C14.2091 11 16 9.20914 16 7C16 4.79086 14.2091 3 12 3C9.79086 3 8 4.79086 8 7C8 9.20914 9.79086 11 12 11Z"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                    />
                                </svg>
                                <input
                                    type="text"
                                    id="username"
                                    name="username"
                                    className={`form-input ${errors.username ? 'input-error' : ''}`}
                                    placeholder="Enter your username or email"
                                    value={formData.username}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                    autoComplete="username"
                                />
                            </div>
                            {errors.username && (
                                <span className="error-text">{errors.username}</span>
                            )}
                        </div>

                        {/* Password Field */}
                        <div className="form-group">
                            <label htmlFor="password" className="form-label">
                                Password
                            </label>
                            <div className="input-wrapper">
                                <svg
                                    className="input-icon"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <rect
                                        x="3"
                                        y="11"
                                        width="18"
                                        height="11"
                                        rx="2"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                    />
                                    <path
                                        d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                    />
                                </svg>
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    id="password"
                                    name="password"
                                    className={`form-input ${errors.password ? 'input-error' : ''}`}
                                    placeholder="Enter your password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                    autoComplete="current-password"
                                />
                                <button
                                    type="button"
                                    className="password-toggle"
                                    onClick={togglePasswordVisibility}
                                    disabled={isLoading}
                                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                                >
                                    {showPassword ? (
                                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M3 3L21 21" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                            <path d="M10.5 10.677C10.0353 11.1648 9.75 11.8228 9.75 12.5485C9.75 14.0651 10.9812 15.3 12.4931 15.3C13.2161 15.3 13.8715 15.0143 14.3575 14.5477" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                            <path d="M7.36364 7.5C5.25 8.94444 3.27273 11.3889 2 13.5C4.09091 16.6111 8.18182 20.5 12 20.5C13.5545 20.5 15.0182 19.9722 16.3636 19.1667M19.2273 16.6111C20.7818 15.1667 21.8182 13.6111 22 13.5C19.9091 10.3889 15.8182 6.5 12 6.5C11.4545 6.5 10.9182 6.55556 10.3909 6.65278" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                        </svg>
                                    ) : (
                                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M2 12C2 12 5 5 12 5C19 5 22 12 22 12C22 12 19 19 12 19C5 19 2 12 2 12Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                            <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2"/>
                                        </svg>
                                    )}
                                </button>
                            </div>
                            {errors.password && (
                                <span className="error-text">{errors.password}</span>
                            )}
                        </div>

                        {/* Submit Button */}
                        <button
                            type="submit"
                            className="login-button"
                            disabled={isLoading}
                        >
                            {isLoading ? (
                                <>
                                    <svg className="spinner" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                        <circle className="spinner-circle" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"/>
                                    </svg>
                                    <span>Signing in...</span>
                                </>
                            ) : (
                                <>
                                    <span>Sign In</span>
                                    <svg className="button-arrow" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                        <path d="M12 5L19 12L12 19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                </>
                            )}
                        </button>
                    </form>

                    {/* Footer */}
                    <div className="login-footer">
                        <p className="footer-text">
                            Contact your administrator for login credentials
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;