import { useState, useEffect } from 'react';
import qrCodeService from '../../services/orderManagement/qrCodeService.js';
import authService from '../../services/userManagement/authService.js';
import Toast from '../../components/common/Toast.jsx';
import './QrOrderTracker.css';

const QrOrderTracker = () => {
    const currentUser = authService.getCurrentUser();
    const isAuthorizedToUpdate =
        currentUser?.role === 'ROLE_ADMIN' ||
        currentUser?.role === 'ROLE_MANAGER' ||
        currentUser?.role === 'ROLE_COURIER';

    // State
    const [qrToken, setQrToken] = useState('');
    const [orderDetails, setOrderDetails] = useState(null);
    const [isVerifying, setIsVerifying] = useState(false);
    const [isUpdating, setIsUpdating] = useState(false);
    const [error, setError] = useState(null);
    const [selectedStatus, setSelectedStatus] = useState('');
    const [toast, setToast] = useState(null);

    // Sales Order Statuses
    const orderStatuses = [
        { value: 'APPROVED', label: 'Approved' },
        { value: 'DELIVERY_IN_PROCESS', label: 'Delivery In Process' },
        { value: 'DELIVERED', label: 'Delivered' },
        { value: 'COMPLETED', label:  'Completed' },
    ];

    // Status colors
    const statusColors = {
        PENDING: '#FF9800',
        PARTIALLY_APPROVED: '#9C27B0',
        PARTIALLY_DELIVERED: '#673AB7',
        APPROVED: '#4CAF50',
        DELIVERY_IN_PROCESS: '#2196F3',
        DELIVERED: '#00BCD4',
        CANCELLED: '#F44336',
        COMPLETED: '#4CAF50',
    };

    // Category colors
    const categoryColors = {
        EDUCATION: '#4CAF50',
        ELECTRONIC: '#2196F3',
        ACTION_FIGURES: '#FF5722',
        DOLLS: '#E91E63',
        MUSICAL_TOY: '#9C27B0',
        OUTDOOR_TOY: '#FF9800',
    };

    // Load saved token from localStorage on mount
    useEffect(() => {
        const savedToken = localStorage.getItem('lastQrToken');
        if (savedToken) {
            setQrToken(savedToken);
        }
    }, []);

    // Show toast notification
    const showToast = (message, type = 'success') => {
        setToast({ message, type });
    };

    // Close toast
    const closeToast = () => {
        setToast(null);
    };

    // Handle verify order
    const handleVerifyOrder = async (e) => {
        e.preventDefault();

        if (!qrToken.trim()) {
            showToast('Please enter a QR token', 'warning');
            return;
        }

        setIsVerifying(true);
        setError(null);
        setOrderDetails(null);

        try {
            const response = await qrCodeService.verifyQrCode(qrToken.trim(), currentUser?.username || 'GUEST');
            setOrderDetails(response.data);

            // Save token to localStorage
            localStorage.setItem('lastQrToken', qrToken.trim());

            showToast('Order verified successfully!  ✅', 'success');
        } catch (err) {
            console.error('Error verifying QR code:', err);
            setError(err.message || 'Failed to verify QR code. Please check the token and try again.');
            showToast(err.message || 'Failed to verify QR code', 'error');
        } finally {
            setIsVerifying(false);
        }
    };

    // Handle update status
    const handleUpdateStatus = async (e) => {
        e.preventDefault();

        if (!selectedStatus) {
            showToast('Please select a status', 'warning');
            return;
        }

        if (!confirm(`Are you sure you want to update the order status to ${selectedStatus.replace(/_/g, ' ')}?`)) {
            return;
        }

        setIsUpdating(true);

        try {
            await qrCodeService.updateOrderStatus(qrToken.trim(), selectedStatus, currentUser?.username);

            showToast('Order status updated successfully!  🎉', 'success');

            // Refresh order details
            const response = await qrCodeService.verifyQrCode(qrToken.trim(), currentUser?.username || 'GUEST');
            setOrderDetails(response.data);
            setSelectedStatus('');
        } catch (err) {
            console.error('Error updating status:', err);
            showToast(err.message || 'Failed to update order status', 'error');
        } finally {
            setIsUpdating(false);
        }
    };

    // Handle clear token
    const handleClearToken = () => {
        setQrToken('');
        setOrderDetails(null);
        setError(null);
        setSelectedStatus('');
        localStorage.removeItem('lastQrToken');
        showToast('Token cleared', 'info');
    };

    // Get status color
    const getStatusColor = (status) => {
        return statusColors[status] || '#757575';
    };

    // Get category color
    const getCategoryColor = (category) => {
        return categoryColors[category] || '#757575';
    };

    // Format date
    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    return (
        <div className="qr-tracker-page">
            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">📱 QR Order Tracker</h1>
                    <p className="page-subtitle">Verify and track sales orders using QR token</p>
                </div>
            </div>

            {/* Main Content */}
            <div className="tracker-container">
                {/* Step 1: Enter QR Token */}
                <div className="tracker-card">
                    <div className="card-header">
                        <h2>🔍 Enter QR Token</h2>
                        <p className="card-subtitle">Enter the QR token to verify order details</p>
                    </div>

                    <form className="token-form" onSubmit={handleVerifyOrder}>
                        <div className="token-input-group">
                            <label className="token-label">QR Token *</label>
                            <div className="token-input-wrapper">
                                <input
                                    type="text"
                                    className="token-input"
                                    placeholder="Enter QR token (e.g., abc123xyz...)"
                                    value={qrToken}
                                    onChange={(e) => setQrToken(e.target.value)}
                                    disabled={isVerifying}
                                />
                                {qrToken && (
                                    <button
                                        type="button"
                                        className="clear-token-btn"
                                        onClick={handleClearToken}
                                        title="Clear Token"
                                    >
                                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                        </svg>
                                    </button>
                                )}
                            </div>
                            <small className="token-help">
                                Paste the QR token from your order confirmation or scan result
                            </small>
                        </div>

                        <button
                            type="submit"
                            className="verify-btn"
                            disabled={isVerifying || ! qrToken.trim()}
                        >
                            {isVerifying ? (
                                <>
                                    <div className="spinner-small"></div>
                                    Verifying...
                                </>
                            ) : (
                                <>
                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M9 11L12 14L22 4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                        <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                    Verify Order
                                </>
                            )}
                        </button>
                    </form>

                    {/* Error Display */}
                    {error && (
                        <div className="error-alert">
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                                <path d="M12 8V12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                <circle cx="12" cy="16" r="1" fill="currentColor"/>
                            </svg>
                            <span>{error}</span>
                        </div>
                    )}
                </div>

                {/* Step 2: Order Details */}
                {orderDetails && (
                    <div className="tracker-card">
                        <div className="card-header">
                            <h2>📋 Order Details</h2>
                            <span
                                className="status-badge-tracker"
                                style={{
                                    backgroundColor: `${getStatusColor(orderDetails.status)}20`,
                                    color: getStatusColor(orderDetails.status),
                                    border: `2px solid ${getStatusColor(orderDetails.status)}`,
                                }}
                            >
                                {orderDetails.status.replace(/_/g, ' ')}
                            </span>
                        </div>

                        {/* Order Information */}
                        <div className="details-section">
                            <h3 className="section-title">Order Information</h3>
                            <div className="details-grid">
                                <div className="detail-item">
                                    <span className="detail-label">Order Reference</span>
                                    <span className="detail-value order-ref">{orderDetails.orderReference}</span>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Order Date</span>
                                    <span className="detail-value">{formatDate(orderDetails.orderDate)}</span>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Estimated Delivery</span>
                                    <span className="detail-value">{formatDate(orderDetails.estimatedDeliveryDate)}</span>
                                </div>
                                {orderDetails.deliveryDate && (
                                    <div className="detail-item">
                                        <span className="detail-label">Actual Delivery</span>
                                        <span className="detail-value">{formatDate(orderDetails.deliveryDate)}</span>
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Customer Information */}
                        <div className="details-section">
                            <h3 className="section-title">Customer Information</h3>
                            <div className="details-grid">
                                <div className="detail-item">
                                    <span className="detail-label">Customer Name</span>
                                    <span className="detail-value">{orderDetails.customerName}</span>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Destination</span>
                                    <span className="detail-value">{orderDetails.destination}</span>
                                </div>
                            </div>
                        </div>

                        {/* Order Items */}
                        <div className="details-section">
                            <h3 className="section-title">Order Items</h3>
                            <div className="items-table-wrapper">
                                <table className="items-table">
                                    <thead>
                                    <tr>
                                        <th>Product</th>
                                        <th>Category</th>
                                        <th>Ordered</th>
                                        <th>Shipped</th>
                                        <th>Unit Price</th>
                                        <th>Total</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {orderDetails.items?.map((item) => (
                                        <tr key={item.id}>
                                            <td>
                                                <div className="item-product-info">
                                                    <span className="item-product-name">{item.productName}</span>
                                                    <span className="item-product-id">{item.productId}</span>
                                                </div>
                                            </td>
                                            <td>
                                                <span
                                                    className="category-badge-tracker"
                                                    style={{
                                                        backgroundColor:  `${getCategoryColor(item.productCategory)}20`,
                                                        color: getCategoryColor(item.productCategory),
                                                    }}
                                                >
                                                    {item.productCategory?.replace(/_/g, ' ')}
                                                </span>
                                            </td>
                                            <td className="qty-cell">{item.quantity}</td>
                                            <td className="qty-cell shipped">{item.approvedQuantity}</td>
                                            <td className="price-cell">${item.unitPrice?.toFixed(2)}</td>
                                            <td className="price-cell total">${item.totalPrice?.toFixed(2)}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        {/* Order Summary */}
                        <div className="details-section">
                            <h3 className="section-title">Order Summary</h3>
                            <div className="summary-grid">
                                <div className="summary-item">
                                    <span className="summary-label">Total Items</span>
                                    <span className="summary-value">{orderDetails.totalItems} units</span>
                                </div>
                                <div className="summary-item">
                                    <span className="summary-label">Total Shipped</span>
                                    <span className="summary-value shipped">{orderDetails.totalApprovedQuantity || 0} units</span>
                                </div>
                                <div className="summary-item">
                                    <span className="summary-label">Total Amount</span>
                                    <span className="summary-value amount">${orderDetails.totalAmount?.toFixed(2)}</span>
                                </div>
                            </div>

                            {/* Progress Bar */}
                            <div className="progress-section">
                                <div className="progress-header">
                                    <span>Fulfillment Progress</span>
                                    <span>{Math.round(((orderDetails.totalApprovedQuantity || 0) / orderDetails.totalItems) * 100)}%</span>
                                </div>
                                <div className="progress-bar-tracker">
                                    <div
                                        className="progress-fill-tracker"
                                        style={{
                                            width: `${((orderDetails.totalApprovedQuantity || 0) / orderDetails.totalItems) * 100}%`,
                                            backgroundColor: orderDetails.totalApprovedQuantity === orderDetails.totalItems ? '#4CAF50' : '#2196F3'
                                        }}
                                    ></div>
                                </div>
                            </div>
                        </div>

                        {/* Additional Info */}
                        {(orderDetails.confirmedBy || orderDetails.lastUpdate) && (
                            <div className="details-section">
                                <h3 className="section-title">Additional Information</h3>
                                <div className="details-grid">
                                    {orderDetails.confirmedBy && (
                                        <div className="detail-item">
                                            <span className="detail-label">Confirmed By</span>
                                            <span className="detail-value">{orderDetails.confirmedBy}</span>
                                        </div>
                                    )}
                                    {orderDetails.lastUpdate && (
                                        <div className="detail-item">
                                            <span className="detail-label">Last Update</span>
                                            <span className="detail-value">{formatDate(orderDetails.lastUpdate)}</span>
                                        </div>
                                    )}
                                </div>
                            </div>
                        )}
                    </div>
                )}

                {/* Step 3: Update Status (Admin/Manager/Courier only) */}
                {orderDetails && isAuthorizedToUpdate && (
                    <div className="tracker-card">
                        <div className="card-header">
                            <h2>🔄 Update Order Status</h2>
                            <p className="card-subtitle">Update the order status to track delivery progress</p>
                        </div>

                        <form className="status-form" onSubmit={handleUpdateStatus}>
                            <div className="form-group">
                                <label className="form-label">New Status *</label>
                                <select
                                    className="form-select"
                                    value={selectedStatus}
                                    onChange={(e) => setSelectedStatus(e.target.value)}
                                    disabled={isUpdating}
                                >
                                    <option value="">Select new status</option>
                                    {orderStatuses.map(status => (
                                        <option key={status.value} value={status.value}>
                                            {status.label}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <button
                                type="submit"
                                className="update-btn"
                                disabled={isUpdating || !selectedStatus}
                            >
                                {isUpdating ? (
                                    <>
                                        <div className="spinner-small"></div>
                                        Updating...
                                    </>
                                ) : (
                                    <>
                                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M21.5 2V8M21.5 8H15.5M21.5 8L18 4.5C16.8 3.3 15.3 2.4 13.6 2C9.9 1 6.1 2.7 4.1 6C2.1 9.3 2.3 13.5 4.8 16.5C7.3 19.5 11.3 20.6 14.9 19.3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                            <path d="M2.5 22V16M2.5 16H8.5M2.5 16L6 19.5C7.2 20.7 8.7 21.6 10.4 22C14.1 23 17.9 21.3 19.9 18C21.9 14.7 21.7 10.5 19.2 7.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                        </svg>
                                        Update Status
                                    </>
                                )}
                            </button>
                        </form>
                    </div>
                )}
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

export default QrOrderTracker;