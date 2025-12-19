import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../../services/userManagement/authService';
import Toast from '../../components/common/Toast';
import './UserProfile.css';

const UserProfile = () => {
    const navigate = useNavigate();
    const currentUser = authService.getCurrentUser();

    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        password: '',
        confirmPassword: '',
    });

    const [errors, setErrors] = useState({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [toast, setToast] = useState(null);

    // Handle input change
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value,
        }));

        // Clear error for this field
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]:  '',
            }));
        }
    };

    // Validate form
    const validateForm = () => {
        const newErrors = {};

        // Check if at least one field is filled
        const hasData = formData.firstName || formData.lastName || formData.password;
        if (!hasData) {
            newErrors.general = 'Please fill at least one field to update';
            setErrors(newErrors);
            return false;
        }

        // Validate firstName
        if (formData.firstName && (formData.firstName.length < 2 || formData.firstName.length > 50)) {
            newErrors.firstName = 'First name must be between 2 and 50 characters';
        }

        // Validate lastName
        if (formData.lastName && (formData.lastName.length < 2 || formData.lastName.length > 50)) {
            newErrors.lastName = 'Last name must be between 2 and 50 characters';
        }

        // Validate password
        if (formData.password) {
            const passwordRegex = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=_])(?=\S+$).{8,}$/;
            if (!passwordRegex.test(formData.password)) {
                newErrors.password = 'Password must contain at least 8 characters, including 1 uppercase, 1 lowercase, 1 number and 1 special character (@#$%^&+=_)';
            }

            // Check password confirmation
            if (formData.password !== formData.confirmPassword) {
                newErrors.confirmPassword = 'Passwords do not match';
            }
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    // Handle form submit
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        setIsSubmitting(true);

        try {
            // Prepare update data (only send non-empty fields)
            const updateData = {};
            if (formData.firstName) updateData.firstName = formData.firstName;
            if (formData.lastName) updateData.lastName = formData.lastName;
            if (formData.password) updateData.password = formData.password;

            const result = await authService.updateUser(updateData);

            if (result.success) {
                showToast(result.message, 'success');

                // If password was changed, logout and redirect to login
                if (result.requireRelogin) {
                    setTimeout(() => {
                        authService.clearAuthData();
                        navigate('/login');
                    }, 2000);
                } else {
                    // Clear form after success
                    setFormData({
                        firstName: '',
                        lastName: '',
                        password:  '',
                        confirmPassword: '',
                    });
                }
            }
        } catch (error) {
            console.error('Update error:', error);
            showToast(error.message || 'Failed to update profile', 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    // Show toast notification
    const showToast = (message, type = 'success') => {
        setToast({ message, type });
    };

    // Close toast
    const closeToast = () => {
        setToast(null);
    };

    return (
        <div className="user-profile-page">
            {/* Page Header */}
            <div className="profile-header">
                <div className="profile-header-content">
                    <div className="profile-avatar-large">
                        {currentUser?.username?.charAt(0).toUpperCase() || 'U'}
                    </div>
                    <div>
                        <h1 className="profile-title">👤 User Profile</h1>
                        <p className="profile-subtitle">
                            Manage your account information and security settings
                        </p>
                    </div>
                </div>
            </div>

            {/* Profile Content */}
            <div className="profile-content">
                {/* Account Information Card */}
                <div className="profile-card account-info-card">
                    <div className="card-header-profile">
                        <h2 className="card-title-profile">📋 Account Information</h2>
                        <p className="card-subtitle-profile">View your account details</p>
                    </div>
                    <div className="account-info-grid">
                        <div className="info-item">
                            <span className="info-label">Username</span>
                            <span className="info-value">{currentUser?.username || 'N/A'}</span>
                        </div>
                        <div className="info-item">
                            <span className="info-label">Role</span>
                            <span className="info-badge">{currentUser?.role?.replace('ROLE_', '') || 'N/A'}</span>
                        </div>
                    </div>
                </div>

                {/* Update Profile Form Card */}
                <div className="profile-card">
                    <div className="card-header-profile">
                        <h2 className="card-title-profile">✏️ Update Profile</h2>
                        <p className="card-subtitle-profile">Update your personal information and password</p>
                    </div>

                    <form onSubmit={handleSubmit} className="profile-form">
                        {/* General Error */}
                        {errors.general && (
                            <div className="error-alert-profile">
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                                    <path d="M12 8V12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                                </svg>
                                <span>{errors.general}</span>
                            </div>
                        )}

                        <div className="form-grid">
                            {/* Username (Read-only) */}
                            <div className="form-group-profile">
                                <label className="form-label-profile">
                                    Username
                                    <span className="label-badge">Read-only</span>
                                </label>
                                <input
                                    type="text"
                                    className="form-input-profile"
                                    value={currentUser?.username || ''}
                                    disabled
                                    readOnly
                                />
                                <small className="form-help">Username cannot be changed</small>
                            </div>

                            {/* Role (Read-only) */}
                            <div className="form-group-profile">
                                <label className="form-label-profile">
                                    Role
                                    <span className="label-badge">Read-only</span>
                                </label>
                                <input
                                    type="text"
                                    className="form-input-profile"
                                    value={currentUser?.role?.replace('ROLE_', '') || ''}
                                    disabled
                                    readOnly
                                />
                                <small className="form-help">Role is assigned by administrators</small>
                            </div>

                            {/* First Name */}
                            <div className="form-group-profile">
                                <label className="form-label-profile">First Name</label>
                                <input
                                    type="text"
                                    name="firstName"
                                    className={`form-input-profile ${errors.firstName ? 'error' :  ''}`}
                                    placeholder="Enter your first name"
                                    value={formData.firstName}
                                    onChange={handleChange}
                                    disabled={isSubmitting}
                                />
                                {errors.firstName && <span className="error-text">{errors.firstName}</span>}
                            </div>

                            {/* Last Name */}
                            <div className="form-group-profile">
                                <label className="form-label-profile">Last Name</label>
                                <input
                                    type="text"
                                    name="lastName"
                                    className={`form-input-profile ${errors.lastName ? 'error' :  ''}`}
                                    placeholder="Enter your last name"
                                    value={formData.lastName}
                                    onChange={handleChange}
                                    disabled={isSubmitting}
                                />
                                {errors.lastName && <span className="error-text">{errors.lastName}</span>}
                            </div>
                        </div>

                        {/* Password Section */}
                        <div className="password-section">
                            <h3 className="section-title">🔒 Change Password</h3>
                            <p className="section-subtitle">Leave blank if you don't want to change your password</p>

                            <div className="form-grid">
                                {/* New Password */}
                                <div className="form-group-profile">
                                    <label className="form-label-profile">New Password</label>
                                    <input
                                        type="password"
                                        name="password"
                                        className={`form-input-profile ${errors.password ? 'error' : ''}`}
                                        placeholder="Enter new password"
                                        value={formData.password}
                                        onChange={handleChange}
                                        disabled={isSubmitting}
                                    />
                                    {errors.password && <span className="error-text">{errors.password}</span>}
                                    <small className="form-help">
                                        Must contain at least 8 characters, including uppercase, lowercase, number and special character
                                    </small>
                                </div>

                                {/* Confirm Password */}
                                <div className="form-group-profile">
                                    <label className="form-label-profile">Confirm Password</label>
                                    <input
                                        type="password"
                                        name="confirmPassword"
                                        className={`form-input-profile ${errors.confirmPassword ?  'error' : ''}`}
                                        placeholder="Re-enter your password"
                                        value={formData.confirmPassword}
                                        onChange={handleChange}
                                        disabled={isSubmitting || !formData.password}
                                    />
                                    {errors.confirmPassword && <span className="error-text">{errors.confirmPassword}</span>}
                                </div>
                            </div>

                            {/* Warning for password change */}
                            {formData.password && (
                                <div className="warning-alert-profile">
                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M10.29 3.86L1.82 18C1.64537 18.3024 1.55296 18.6453 1.55199 18.9945C1.55101 19.3437 1.64151 19.6871 1.81445 19.9905C1.98738 20.2939 2.23675 20.5467 2.53773 20.7239C2.83871 20.901 3.18082 20.9962 3.53 21H20.47C20.8192 20.9962 21.1613 20.901 21.4623 20.7239C21.7633 20.5467 22.0126 20.2939 22.1856 19.9905C22.3585 19.6871 22.449 19.3437 22.448 18.9945C22.447 18.6453 22.3546 18.3024 22.18 18L13.71 3.86C13.5317 3.56611 13.2807 3.32312 12.9812 3.15448C12.6817 2.98585 12.3437 2.89725 12 2.89725C11.6563 2.89725 11.3183 2.98585 11.0188 3.15448C10.7193 3.32312 10.4683 3.56611 10.29 3.86Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                        <path d="M12 9V13" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                        <circle cx="12" cy="17" r="1" fill="currentColor"/>
                                    </svg>
                                    <span>⚠️ Changing your password will log you out. You'll need to login again with your new password.</span>
                                </div>
                            )}
                        </div>

                        {/* Form Actions */}
                        <div className="form-actions">
                            <button
                                type="button"
                                className="btn-cancel-profile"
                                onClick={() => {
                                    setFormData({
                                        firstName: '',
                                        lastName:  '',
                                        password: '',
                                        confirmPassword: '',
                                    });
                                    setErrors({});
                                }}
                                disabled={isSubmitting}
                            >
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                                Reset Form
                            </button>
                            <button
                                type="submit"
                                className="btn-submit-profile"
                                disabled={isSubmitting}
                            >
                                {isSubmitting ? (
                                    <>
                                        <div className="spinner-small-profile"></div>
                                        Updating...
                                    </>
                                ) : (
                                    <>
                                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16L21 8V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                            <path d="M17 21V13H7V21" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                            <path d="M7 3V8H15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                        </svg>
                                        Save Changes
                                    </>
                                )}
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            {/* Toast Notification */}
            {toast && (
                <Toast
                    message={toast.message}
                    type={toast.type}
                    onClose={closeToast}
                    duration={3000}
                />
            )}
        </div>
    );
};

export default UserProfile;