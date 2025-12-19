import { useState, useEffect } from 'react';
import outgoingStockService from '../../services/inventoryControl/outgoingStockService.js';
import authService from '../../services/userManagement/authService.js';
import qrCodeService from '../../services/orderManagement/qrCodeService.js';
import './OutgoingStock.css';

const OutgoingStock = () => {
    const currentUser = authService.getCurrentUser();
    const isAdminOrManager = currentUser?.role === 'ROLE_ADMIN' || currentUser?.role === 'ROLE_MANAGER';

    const [metrics, setMetrics] = useState(null);
    const [orders, setOrders] = useState(null);
    const [isLoadingMetrics, setIsLoadingMetrics] = useState(true);
    const [isLoadingOrders, setIsLoadingOrders] = useState(true);
    const [error, setError] = useState(null);

    // View mode:  'all' or 'urgent'
    const [viewMode, setViewMode] = useState('all');

    // Pagination
    const [currentPage, setCurrentPage] = useState(0);

    // Search
    const [searchText, setSearchText] = useState('');
    const [searchQuery, setSearchQuery] = useState('');

    // Filter
    const [showFilters, setShowFilters] = useState(false);
    const [filterValue, setFilterValue] = useState('');
    const [activeFilter, setActiveFilter] = useState({ type: '', value: '' });

    // Modals
    const [showStockOutModal, setShowStockOutModal] = useState(false);
    const [showCancelModal, setShowCancelModal] = useState(false);
    const [showQrModal, setShowQrModal] = useState(false);
    const [selectedOrder, setSelectedOrder] = useState(null);
    const [orderDetails, setOrderDetails] = useState(null);
    const [isLoadingDetails, setIsLoadingDetails] = useState(false);

    // QR Code data
    const [qrCodeData, setQrCodeData] = useState(null);
    const [isLoadingQr, setIsLoadingQr] = useState(false);
    const [qrError, setQrError] = useState(null);

    // Form data for stock out
    const [itemQuantities, setItemQuantities] = useState({});
    const [formErrors, setFormErrors] = useState({});

    // Sales Order Statuses
    const orderStatuses = [
        { value: 'PENDING', label:  'Pending' },
        { value: 'PARTIALLY_APPROVED', label: 'Partially Approved' },
        { value: 'PARTIALLY_DELIVERED', label: 'Partially Delivered' },
        { value: 'APPROVED', label: 'Approved' },
        { value: 'DELIVERY_IN_PROCESS', label: 'Delivery In Process' },
    ];

    // Status colors
    const statusColors = {
        PENDING: '#FF9800',
        PARTIALLY_APPROVED: '#9C27B0',
        PARTIALLY_DELIVERED: '#673AB7',
        APPROVED:  '#4CAF50',
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

    // Fetch metrics
    const fetchMetrics = async () => {
        setIsLoadingMetrics(true);
        try {
            const data = await outgoingStockService.getMetrics();
            setMetrics(data.data);
        } catch (err) {
            console.error('Error fetching metrics:', err);
        } finally {
            setIsLoadingMetrics(false);
        }
    };

    // Fetch all waiting orders
    const fetchAllOrders = async () => {
        setIsLoadingOrders(true);
        setError(null);

        try {
            const params = {};
            if (currentPage > 0) params.page = currentPage;

            const data = await outgoingStockService.getAllWaitingOrders(params);
            setOrders(data);
        } catch (err) {
            console.error('Error fetching orders:', err);
            setError(err.message || 'Failed to load orders');
        } finally {
            setIsLoadingOrders(false);
        }
    };

    // Fetch urgent orders
    const fetchUrgentOrders = async () => {
        setIsLoadingOrders(true);
        setError(null);

        try {
            const params = {};
            if (currentPage > 0) params.page = currentPage;

            const data = await outgoingStockService.getUrgentOrders(params);
            setOrders(data);
        } catch (err) {
            console.error('Error fetching urgent orders:', err);
            setError(err.message || 'Failed to load urgent orders');
        } finally {
            setIsLoadingOrders(false);
        }
    };

    // Search orders
    const searchOrders = async () => {
        setIsLoadingOrders(true);
        setError(null);

        try {
            const params = { text: searchQuery };
            if (currentPage > 0) params.page = currentPage;

            const data = await outgoingStockService.searchOrders(params);
            setOrders(data);
        } catch (err) {
            console.error('Error searching orders:', err);
            setError(err.message || 'Failed to search orders');
        } finally {
            setIsLoadingOrders(false);
        }
    };

    // Filter orders
    const filterOrders = async () => {
        setIsLoadingOrders(true);
        setError(null);

        try {
            const params = { status: activeFilter.value };
            if (currentPage > 0) params.page = currentPage;

            const data = await outgoingStockService.filterOrders(params);
            setOrders(data);
        } catch (err) {
            console.error('Error filtering orders:', err);
            setError(err.message || 'Failed to filter orders');
        } finally {
            setIsLoadingOrders(false);
        }
    };

    // Open QR Code Modal
    const handleOpenQrModal = async (order) => {
        setSelectedOrder(order);
        setShowQrModal(true);
        setIsLoadingQr(true);
        setQrError(null);

        try {
            const response = await qrCodeService.getQrCodeUrl(order.id);
            setQrCodeData(response.data);
        } catch (err) {
            console.error('Error fetching QR code:', err);
            setQrError(err.message || 'Failed to load QR code');
        } finally {
            setIsLoadingQr(false);
        }
    };

    // Close QR Code Modal
    const handleCloseQrModal = () => {
        setShowQrModal(false);
        setQrCodeData(null);
        setQrError(null);
        setSelectedOrder(null);
    };

    // Download QR Code
    const handleDownloadQr = () => {
        if (qrCodeData?.qrCodeUrl) {
            // Create a temporary link and trigger download
            const link = document.createElement('a');
            link.href = qrCodeData.qrCodeUrl;
            link.download = `${qrCodeData.orderReference}-QR.png`;
            link.target = '_blank';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }
    };

    // Print QR Code
    const handlePrintQr = () => {
        if (qrCodeData?.qrCodeUrl) {
            const printWindow = window.open('', '_blank');
            printWindow.document.write(`
                <html>
                    <head>
                        <title>Print QR Code - ${qrCodeData.orderReference}</title>
                        <style>
                            body {
                                display: flex;
                                flex-direction: column;
                                align-items: center;
                                justify-content: center;
                                min-height: 100vh;
                                margin: 0;
                                font-family: Arial, sans-serif;
                            }
                            .qr-print-container {
                                text-align: center;
                                padding: 20px;
                            }
                            h2 {
                                margin-bottom: 10px;
                                color: #333;
                            }
                            p {
                                margin: 5px 0;
                                color: #666;
                            }
                            img {
                                max-width:  400px;
                                margin: 20px 0;
                                border: 2px solid #ddd;
                                border-radius: 8px;
                            }
                            @media print {
                                body {
                                    padding: 0;
                                }
                            }
                        </style>
                    </head>
                    <body>
                        <div class="qr-print-container">
                            <h2>Sales Order QR Code</h2>
                            <p><strong>Order Reference:</strong> ${qrCodeData.orderReference}</p>
                            <p><strong>Customer:</strong> ${selectedOrder?.customerName}</p>
                            <img src="${qrCodeData.qrCodeUrl}" alt="QR Code" />
                            <p><small>Scan to verify order details</small></p>
                        </div>
                    </body>
                </html>
            `);
            printWindow.document.close();
            printWindow.focus();
            setTimeout(() => {
                printWindow.print();
            }, 250);
        }
    };

    // Copy QR Token
    const handleCopyToken = () => {
        if (qrCodeData?.qrToken) {
            navigator.clipboard.writeText(qrCodeData.qrToken)
                .then(() => {
                    alert('QR Token copied to clipboard!');
                })
                .catch((err) => {
                    console.error('Failed to copy:', err);
                });
        }
    };

    // Initial load
    useEffect(() => {
        fetchMetrics();
    }, []);

    // Fetch orders based on view mode, search, filter, pagination
    useEffect(() => {
        if (searchQuery) {
            searchOrders();
        } else if (activeFilter.value) {
            filterOrders();
        } else if (viewMode === 'urgent') {
            fetchUrgentOrders();
        } else {
            fetchAllOrders();
        }
    }, [currentPage, searchQuery, activeFilter, viewMode]);

    // Handle search
    const handleSearch = (e) => {
        e.preventDefault();
        if (searchText.trim()) {
            setCurrentPage(0);
            setActiveFilter({ type: '', value: '' });
            setFilterValue('');
            setViewMode('all');
            setSearchQuery(searchText);
        } else {
            handleClearSearch();
        }
    };

    // Clear search
    const handleClearSearch = () => {
        setSearchText('');
        setCurrentPage(0);
        setSearchQuery('');
    };

    // Apply filter
    const handleApplyFilter = () => {
        if (! filterValue) {
            alert('Please select a status');
            return;
        }

        setCurrentPage(0);
        setSearchQuery('');
        setSearchText('');
        setViewMode('all');
        setShowFilters(false);
        setActiveFilter({ type: 'status', value: filterValue });
    };

    // Clear filter
    const handleClearFilter = () => {
        setFilterValue('');
        setCurrentPage(0);
        setShowFilters(false);
        setActiveFilter({ type: '', value:  '' });
    };

    // Toggle view mode
    const handleToggleViewMode = (mode) => {
        setViewMode(mode);
        setCurrentPage(0);
        setSearchQuery('');
        setSearchText('');
        setActiveFilter({ type: '', value: '' });
        setFilterValue('');
    };

    // Handle page change
    const handlePageChange = (newPage) => {
        setCurrentPage(newPage);
    };

    // Open Stock Out Modal
    const handleOpenStockOutModal = async (order) => {
        setSelectedOrder(order);
        setIsLoadingDetails(true);
        setShowStockOutModal(true);
        setFormErrors({});

        try {
            const details = await outgoingStockService.getOrderDetails(order.id);
            setOrderDetails(details.data);

            // Initialize quantities with remaining quantities
            const initialQuantities = {};
            details.data.items.forEach(item => {
                const remaining = item.quantity - item.approvedQuantity;
                initialQuantities[item.productId] = remaining;
            });
            setItemQuantities(initialQuantities);
        } catch (err) {
            console.error('Error fetching order details:', err);
            setFormErrors({ submit: err.message || 'Failed to load order details' });
        } finally {
            setIsLoadingDetails(false);
        }
    };

    // Close Stock Out Modal
    const handleCloseStockOutModal = () => {
        setShowStockOutModal(false);
        setOrderDetails(null);
        setItemQuantities({});
        setFormErrors({});
        setSelectedOrder(null);
        setIsLoadingDetails(false);
    };

    // Open Cancel Modal
    const handleOpenCancelModal = (order) => {
        setSelectedOrder(order);
        setShowCancelModal(true);
    };

    // Handle quantity change
    const handleQuantityChange = (productId, value) => {
        setItemQuantities(prev => ({
            ...prev,
            [productId]:  value
        }));
    };

    // Validate stock out form
    const validateStockOutForm = () => {
        const errors = {};

        // Check if at least one item has quantity > 0
        const hasQuantity = Object.values(itemQuantities).some(qty => qty > 0);
        if (!hasQuantity) {
            errors.submit = 'At least one item must have quantity greater than 0';
        }

        // Validate each item quantity
        orderDetails?.items.forEach(item => {
            const quantity = parseInt(itemQuantities[item.productId] || 0);
            const remainingQuantity = item.quantity - item.approvedQuantity;

            if (quantity > remainingQuantity) {
                errors[item.productId] = `Cannot exceed remaining quantity (${remainingQuantity})`;
            }
        });

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // Handle Stock Out
    const handleStockOut = async (e) => {
        e.preventDefault();

        if (!validateStockOutForm()) return;

        try {
            // Filter out items with 0 quantity
            const filteredQuantities = {};
            Object.entries(itemQuantities).forEach(([productId, qty]) => {
                if (qty > 0) {
                    filteredQuantities[productId] = parseInt(qty);
                }
            });

            const stockOutData = {
                orderId:  selectedOrder.id,
                itemQuantities: filteredQuantities,
            };

            await outgoingStockService.stockOut(stockOutData);

            // Reset and close
            handleCloseStockOutModal();
            fetchMetrics();
            if (viewMode === 'urgent') {
                fetchUrgentOrders();
            } else {
                fetchAllOrders();
            }
        } catch (err) {
            console.error('Error processing stock out:', err);
            setFormErrors({ submit: err.message || 'Failed to process stock out' });
        }
    };

    // Handle Cancel Order
    const handleCancelOrder = async () => {
        try {
            await outgoingStockService.cancelOrder(selectedOrder.id);
            setShowCancelModal(false);
            fetchMetrics();
            if (viewMode === 'urgent') {
                fetchUrgentOrders();
            } else {
                fetchAllOrders();
            }
        } catch (err) {
            console.error('Error cancelling order:', err);
            setError(err.message || 'Failed to cancel order');
        }
    };

    // Check if order is urgent
    const isUrgent = (estimatedDeliveryDate) => {
        const today = new Date();
        const delivery = new Date(estimatedDeliveryDate);
        const diffTime = delivery - today;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays <= 2;
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
        });
    };

    // Calculate days until delivery
    const getDaysUntilDelivery = (estimatedDate) => {
        const today = new Date();
        const delivery = new Date(estimatedDate);
        const diffTime = delivery - today;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        if (diffDays < 0) return `${Math.abs(diffDays)}d overdue`;
        if (diffDays === 0) return 'Today';
        if (diffDays === 1) return 'Tomorrow';
        if (diffDays <= 2) return `🔥 ${diffDays}d`;
        return `${diffDays}d`;
    };

    // Generate page numbers
    const generatePageNumbers = () => {
        const totalPages = orders?.totalPages || 0;
        const pages = [];
        const maxPagesToShow = 5;

        let startPage = Math.max(0, currentPage - Math.floor(maxPagesToShow / 2));
        let endPage = Math.min(totalPages - 1, startPage + maxPagesToShow - 1);

        if (endPage - startPage < maxPagesToShow - 1) {
            startPage = Math.max(0, endPage - maxPagesToShow + 1);
        }

        for (let i = startPage; i <= endPage; i++) {
            pages.push(i);
        }

        return pages;
    };

    if (isLoadingMetrics || (isLoadingOrders && !orders)) {
        return (
            <div className="loading-container">
                <div className="spinner-large"></div>
                <p>Loading sales orders...</p>
            </div>
        );
    }

    if (error && !orders) {
        return (
            <div className="error-container">
                <svg className="error-icon-large" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                    <path d="M12 8V12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                </svg>
                <h2>Error Loading Data</h2>
                <p>{error}</p>
                <button className="retry-btn" onClick={fetchAllOrders}>
                    Try Again
                </button>
            </div>
        );
    }

    return (
        <div className="outgoing-stock-page">
            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">Outgoing Stock</h1>
                    <p className="page-subtitle">Manage pending sales orders awaiting fulfillment</p>
                </div>
            </div>

            {/* Metrics Cards */}
            <div className="metrics-grid-os">
                <div className="metric-card-os metric-pending">
                    <div className="metric-icon-wrapper-os">
                        <svg className="metric-icon-os" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 2H15M9 2V6M15 2V6M9 2C7.89543 2 7 2.89543 7 4V6H17V4C17 2.89543 16.1046 2 15 2Z" stroke="currentColor" strokeWidth="2"/>
                            <rect x="3" y="8" width="18" height="13" rx="2" stroke="currentColor" strokeWidth="2"/>
                        </svg>
                    </div>
                    <div className="metric-content-os">
                        <p className="metric-label-os">Pending Orders</p>
                        <h2 className="metric-value-os">{metrics?.totalPending || 0}</h2>
                        <p className="metric-sublabel-os">Awaiting confirmation</p>
                    </div>
                </div>

                <div className="metric-card-os metric-approved">
                    <div className="metric-icon-wrapper-os">
                        <svg className="metric-icon-os" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 11L12 14L22 4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-os">
                        <p className="metric-label-os">Partially Approved</p>
                        <h2 className="metric-value-os">{metrics?.totalPartiallyApproved || 0}</h2>
                        <p className="metric-sublabel-os">Ready for fulfillment</p>
                    </div>
                </div>
            </div>

            {/* Orders Table Section */}
            <div className="orders-section">
                {/* Section Header */}
                <div className="section-header-os">

                    <div className="section-title-wrapper">
                        <h2 className="section-title">Sales Orders</h2>
                        <p className="section-subtitle">
                            {orders?.totalElements || 0} {viewMode === 'urgent' ?  'urgent' : 'total'} orders
                        </p>
                    </div>

                    <div className="header-actions-wrapper">
                        {/* Left: View Mode Tabs */}
                        <div className="view-mode-tabs-os">
                            <button
                                className={`tab-btn-os ${viewMode === 'all' ? 'active' :  ''}`}
                                onClick={() => handleToggleViewMode('all')}
                            >
                                <span className="tab-label">Outgoing Orders</span>
                            </button>
                            <button
                                className={`tab-btn-os ${viewMode === 'urgent' ?   'active' : ''}`}
                                onClick={() => handleToggleViewMode('urgent')}
                            >
                                <span className="tab-label">⚠️ Urgent Orders</span>
                            </button>
                        </div>

                        {/* Right: Search and Filter Controls */}
                        <div className="search-filter-controls-os">
                            {/* Search Bar */}
                            <form className="search-form-os" onSubmit={handleSearch}>
                                <div className="search-input-wrapper-os">
                                    <svg className="search-icon-os" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="2"/>
                                        <path d="M21 21L16.65 16.65" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                    </svg>
                                    <input
                                        type="text"
                                        className="search-input-os"
                                        placeholder="Search by order ref, customer..."
                                        value={searchText}
                                        onChange={(e) => setSearchText(e.target.value)}
                                    />
                                    {searchText && (
                                        <button type="button" className="clear-search-btn-os" onClick={handleClearSearch}>
                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                            </svg>
                                        </button>
                                    )}
                                </div>
                                <button type="submit" className="search-btn-os">
                                    Search
                                </button>
                            </form>

                            {/* Filter Button */}
                            <button
                                className={`filter-btn-os ${activeFilter.value ?   'active' : ''}`}
                                onClick={() => setShowFilters(!  showFilters)}
                            >
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M22 3H2L10 12.46V19L14 21V12.46L22 3Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                </svg>
                                Filter
                                {activeFilter.value && <span className="filter-count-os">1</span>}
                            </button>
                        </div>
                    </div>
                </div>

                {/* Filter Panel */}
                {showFilters && (
                    <div className="filter-panel-os">
                        <div className="filter-grid-single-os">
                            <div className="filter-group-os">
                                <label className="filter-label-os">Filter By Status</label>
                                <select
                                    className="filter-select-os"
                                    value={filterValue}
                                    onChange={(e) => setFilterValue(e.target.value)}
                                >
                                    <option value="">Select status</option>
                                    {orderStatuses.map(status => (
                                        <option key={status.value} value={status.value}>
                                            {status.label}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div className="filter-actions-os">
                            <button className="clear-filters-btn-os" onClick={handleClearFilter}>
                                Clear
                            </button>
                            <button className="apply-filters-btn-os" onClick={handleApplyFilter}>
                                Apply Filter
                            </button>
                        </div>
                    </div>
                )}

                {/* Loading indicator */}
                {isLoadingOrders && orders && (
                    <div className="table-loading">
                        <div className="spinner-small"></div>
                        <span>Loading...</span>
                    </div>
                )}

                {/* Orders Table */}
                {!isLoadingOrders && orders?.content?.length > 0 ?   (
                    <>
                        <div className="table-container">
                            <table className="orders-table">
                                <thead>
                                <tr>
                                    <th>Order Ref</th>
                                    <th>Customer</th>
                                    <th>Destination</th>
                                    <th>Total Items</th>
                                    <th>Shipped</th>
                                    <th>Total Amount</th>
                                    <th>Order Date</th>
                                    <th>Est.  Delivery</th>
                                    <th>Status</th>
                                    <th>QR Code</th>
                                    {isAdminOrManager && <th>Actions</th>}
                                </tr>
                                </thead>
                                <tbody>
                                {orders.content.map((order) => {
                                    const urgent = isUrgent(order.estimatedDeliveryDate);

                                    return (
                                        <tr key={order.id} className={urgent ? 'row-urgent' : ''}>
                                            <td className="order-ref-cell">{order.orderReference}</td>
                                            <td className="customer-name-os">{order.customerName}</td>
                                            <td>{order.destination}</td>
                                            <td className="items-cell">{order.totalOrderedQuantity}</td>
                                            <td>
                                                <div className="progress-wrapper">
                                                    <div className="progress-bar-is">
                                                        <div
                                                            className="progress-fill-is"
                                                            style={{
                                                                width: `${Math.min(100, ((order.totalApprovedQuantity || 0) / order.totalOrderedQuantity) * 100)}%`,
                                                                backgroundColor: ((order.totalApprovedQuantity || 0) / order.totalOrderedQuantity) * 100 === 100 ? '#4CAF50' : '#2196F3'
                                                            }}
                                                        ></div>
                                                    </div>
                                                    <div className="progress-details">
                                                        <span className="progress-text">
                                                            {order.totalApprovedQuantity}
                                                            {order.totalApprovedQuantity < order.totalOrderedQuantity && (
                                                                <span className="pending-hint">
                                                                    &nbsp;({order.totalOrderedQuantity - order.totalApprovedQuantity} pending)
                                                                </span>
                                                            )}
                                                        </span>
                                                    </div>
                                                </div>
                                            </td>

                                            <td className="amount-cell">${order.totalAmount?.toFixed(2)}</td>
                                            <td>{formatDate(order.orderDate)}</td>
                                            <td>
                                                <span className={`delivery-badge ${urgent ? 'delivery-urgent' : ''}`}>
                                                    {getDaysUntilDelivery(order.estimatedDeliveryDate)}
                                                </span>
                                            </td>
                                            <td>
                                                <span
                                                    className="status-badge-os"
                                                    style={{
                                                        backgroundColor:   `${getStatusColor(order.status)}20`,
                                                        color: getStatusColor(order.status),
                                                        border: `1px solid ${getStatusColor(order.status)}50`
                                                    }}
                                                >
                                                    {order.status.replace(/_/g, ' ')}
                                                </span>
                                            </td>
                                            <td className="qr-cell">
                                                <button
                                                    className="qr-btn"
                                                    onClick={() => handleOpenQrModal(order)}
                                                    title="View/Print QR Code"
                                                >
                                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                        <rect x="3" y="3" width="8" height="8" rx="1" stroke="currentColor" strokeWidth="2"/>
                                                        <rect x="13" y="3" width="8" height="8" rx="1" stroke="currentColor" strokeWidth="2"/>
                                                        <rect x="3" y="13" width="8" height="8" rx="1" stroke="currentColor" strokeWidth="2"/>
                                                        <rect x="16" y="16" width="2" height="2" fill="currentColor"/>
                                                        <rect x="19" y="16" width="2" height="2" fill="currentColor"/>
                                                        <rect x="16" y="19" width="2" height="2" fill="currentColor"/>
                                                        <rect x="19" y="19" width="2" height="2" fill="currentColor"/>
                                                    </svg>
                                                </button>
                                            </td>
                                            {isAdminOrManager && (
                                                <td>
                                                    <div className="action-buttons-os">
                                                        <button
                                                            className="action-btn-os stockout-btn"
                                                            onClick={() => handleOpenStockOutModal(order)}
                                                            title="Stock Out"
                                                            disabled={order.totalOrderedQuantity === 0}
                                                        >
                                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                <path d="M7 10L12 15L17 10" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                <path d="M12 15V3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                            </svg>
                                                        </button>
                                                        <button
                                                            className="action-btn-os cancel-btn"
                                                            onClick={() => handleOpenCancelModal(order)}
                                                            title="Cancel Order"
                                                        >
                                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                                                                <path d="M15 9L9 15M9 9L15 15" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                                            </svg>
                                                        </button>
                                                    </div>
                                                </td>
                                            )}
                                        </tr>
                                    );
                                })}
                                </tbody>
                            </table>
                        </div>

                        {/* Pagination */}
                        {orders.totalPages > 1 && (
                            <div className="pagination">
                                <button
                                    className="pagination-btn pagination-arrow"
                                    onClick={() => handlePageChange(currentPage - 1)}
                                    disabled={currentPage === 0}
                                >
                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M15 18L9 12L15 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                </button>

                                {currentPage > 2 && (
                                    <>
                                        <button className="pagination-btn pagination-number" onClick={() => handlePageChange(0)}>
                                            1
                                        </button>
                                        {currentPage > 3 && <span className="pagination-ellipsis">...</span>}
                                    </>
                                )}

                                {generatePageNumbers().map((pageNum) => (
                                    <button
                                        key={pageNum}
                                        className={`pagination-btn pagination-number ${pageNum === currentPage ? 'active' : ''}`}
                                        onClick={() => handlePageChange(pageNum)}
                                    >
                                        {pageNum + 1}
                                    </button>
                                ))}

                                {currentPage < orders.totalPages - 3 && (
                                    <>
                                        {currentPage < orders.totalPages - 4 && (
                                            <span className="pagination-ellipsis">...</span>
                                        )}
                                        <button
                                            className="pagination-btn pagination-number"
                                            onClick={() => handlePageChange(orders.totalPages - 1)}
                                        >
                                            {orders.totalPages}
                                        </button>
                                    </>
                                )}

                                <button
                                    className="pagination-btn pagination-arrow"
                                    onClick={() => handlePageChange(currentPage + 1)}
                                    disabled={currentPage === orders.totalPages - 1}
                                >
                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M9 18L15 12L9 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                </button>

                                <div className="pagination-info">
                                    Page {currentPage + 1} of {orders.totalPages}
                                </div>
                            </div>
                        )}
                    </>
                ) : !isLoadingOrders && (
                    <div className="empty-state">
                        <svg className="empty-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 11L12 14L22 4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                        <h3>No Orders Found</h3>
                        <p>There are currently no {viewMode === 'urgent' ? 'urgent' : 'pending'} sales orders. </p>
                    </div>
                )}
            </div>

            {/* Stock Out Modal */}
            {showStockOutModal && (
                <div className="modal-overlay" onClick={handleCloseStockOutModal}>
                    <div className="modal-content modal-large" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>📤 Stock Out - Fulfill Order</h2>
                            <button className="modal-close-btn" onClick={handleCloseStockOutModal}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <form onSubmit={handleStockOut}>
                            <div className="modal-body">
                                {formErrors.submit && (
                                    <div className="error-alert">
                                        {formErrors.submit}
                                    </div>
                                )}

                                {isLoadingDetails ? (
                                    <div className="loading-details">
                                        <div className="spinner-small"></div>
                                        <p>Loading order details...</p>
                                    </div>
                                ) : orderDetails ?   (
                                    <>
                                        {/* Order Summary */}
                                        <div className="order-details-box">
                                            <h3>Order Details</h3>
                                            <div className="detail-row">
                                                <span className="detail-label">Order Reference:  </span>
                                                <span className="detail-value">{orderDetails.orderReference}</span>
                                            </div>
                                            <div className="detail-row">
                                                <span className="detail-label">Customer: </span>
                                                <span className="detail-value">{orderDetails.customerName}</span>
                                            </div>
                                            <div className="detail-row">
                                                <span className="detail-label">Destination:</span>
                                                <span className="detail-value">{orderDetails.destination}</span>
                                            </div>
                                            <div className="detail-row">
                                                <span className="detail-label">Total Items:</span>
                                                <span className="detail-value">{orderDetails.totalItems}</span>
                                            </div>
                                            {orderDetails.totalApprovedQuantity > 0 && (
                                                <div className="detail-row highlight">
                                                    <span className="detail-label">Already Processed:</span>
                                                    <span className="detail-value">{orderDetails.totalApprovedQuantity}</span>
                                                </div>
                                            )}
                                        </div>

                                        {/* Items List */}
                                        <div className="items-list">
                                            <h3>Order Items</h3>
                                            <p className="items-help">Specify the quantity to ship for each item</p>

                                            {orderDetails.items.map((item) => {
                                                const remainingQuantity = item.quantity - item.approvedQuantity;
                                                const isFullyShipped = remainingQuantity === 0;

                                                return (
                                                    <div key={item.id} className="item-card">
                                                        <div className="item-header">
                                                            <div className="item-info">
                                                                <h4>{item.productName}</h4>
                                                                <span className="item-product-id">ID: {item.productId}</span>
                                                                <span
                                                                    className="item-category"
                                                                    style={{
                                                                        backgroundColor:   `${getCategoryColor(item.productCategory)}20`,
                                                                        color: getCategoryColor(item.productCategory),
                                                                    }}
                                                                >
                                                                    {item.productCategory?.replace(/_/g, ' ')}
                                                                </span>
                                                            </div>
                                                        </div>

                                                        <div className="item-details">
                                                            <div className="item-detail">
                                                                <span className="item-label">Ordered:</span>
                                                                <span className="item-value">{item.quantity}</span>
                                                            </div>
                                                            <div className="item-detail">
                                                                <span className="item-label">Shipped:</span>
                                                                <span className="item-value shipped">{item.approvedQuantity}</span>
                                                            </div>
                                                            <div className="item-detail">
                                                                <span className="item-label">Remaining:</span>
                                                                <span className="item-value remaining">{remainingQuantity}</span>
                                                            </div>
                                                            <div className="item-detail">
                                                                <span className="item-label">Unit Price:</span>
                                                                <span className="item-value">${item.unitPrice?.toFixed(2)}</span>
                                                            </div>
                                                        </div>

                                                        <div className="item-quantity-input">
                                                            <label className="form-label">
                                                                Ship Quantity *
                                                                <span className="max-hint">(Max:   {remainingQuantity})</span>
                                                            </label>
                                                            <input
                                                                type="number"
                                                                min="0"
                                                                max={remainingQuantity}
                                                                className={`form-input ${formErrors[item.productId] ? 'input-error' :   ''} ${isFullyShipped ? 'input-disabled' : ''}`}
                                                                value={itemQuantities[item.productId] || 0}
                                                                onChange={(e) => handleQuantityChange(item.productId, e.target.value)}
                                                                disabled={isFullyShipped}
                                                            />
                                                            {isFullyShipped && (
                                                                <span className="info-text">✓ Fully shipped</span>
                                                            )}
                                                            {formErrors[item.productId] && (
                                                                <span className="error-text">{formErrors[item.productId]}</span>
                                                            )}
                                                        </div>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    </>
                                ) : (
                                    <p>Failed to load order details</p>
                                )}
                            </div>

                            <div className="modal-footer">
                                <button type="button" className="btn-secondary" onClick={handleCloseStockOutModal}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn-primary" disabled={isLoadingDetails}>
                                    Process Stock Out
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Cancel Confirmation Modal */}
            {showCancelModal && (
                <div className="modal-overlay" onClick={() => setShowCancelModal(false)}>
                    <div className="modal-content modal-small" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>⚠️ Cancel Sales Order</h2>
                            <button className="modal-close-btn" onClick={() => setShowCancelModal(false)}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <div className="modal-body">
                            <p>Are you sure you want to cancel this sales order?</p>

                            <div className="order-details-box">
                                <div className="detail-row">
                                    <span className="detail-label">Order Reference:</span>
                                    <span className="detail-value">{selectedOrder?.orderReference}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Customer:</span>
                                    <span className="detail-value">{selectedOrder?.customerName}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Destination:  </span>
                                    <span className="detail-value">{selectedOrder?.destination}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Total Amount:</span>
                                    <span className="detail-value">${selectedOrder?.totalAmount?.toFixed(2)}</span>
                                </div>
                            </div>

                            <p className="warning-text">
                                ⚠️ This action cannot be undone. Reserved stock will be released back to inventory.
                            </p>
                        </div>

                        <div className="modal-footer">
                            <button type="button" className="btn-secondary" onClick={() => setShowCancelModal(false)}>
                                Go Back
                            </button>
                            <button type="button" className="btn-danger" onClick={handleCancelOrder}>
                                Cancel Order
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* QR Code Modal */}
            {showQrModal && (
                <div className="modal-overlay" onClick={handleCloseQrModal}>
                    <div className="modal-content modal-qr" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>📱 Order QR Code</h2>
                            <button className="modal-close-btn" onClick={handleCloseQrModal}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <div className="modal-body">
                            {isLoadingQr ? (
                                <div className="loading-qr">
                                    <div className="spinner-small"></div>
                                    <p>Generating QR code...</p>
                                </div>
                            ) : qrError ? (
                                <div className="error-alert">
                                    {qrError}
                                </div>
                            ) : qrCodeData ? (
                                <>
                                    {/* Order Info */}
                                    <div className="qr-order-info">
                                        <h3>Order Details</h3>
                                        <div className="qr-info-row">
                                            <span className="qr-label">Order Reference:</span>
                                            <span className="qr-value">{qrCodeData.orderReference}</span>
                                        </div>
                                        <div className="qr-info-row">
                                            <span className="qr-label">Customer:</span>
                                            <span className="qr-value">{selectedOrder?.customerName}</span>
                                        </div>
                                        <div className="qr-info-row">
                                            <span className="qr-label">Destination:</span>
                                            <span className="qr-value">{selectedOrder?.destination}</span>
                                        </div>
                                    </div>

                                    {/* QR Code Image */}
                                    <div className="qr-image-container">
                                        <img
                                            src={qrCodeData.qrCodeUrl}
                                            alt="Order QR Code"
                                            className="qr-image"
                                        />
                                        <p className="qr-scan-hint">Scan this QR code to verify order details</p>
                                    </div>

                                    {/* QR Token */}
                                    <div className="qr-token-section">
                                        <label className="qr-token-label">QR Token:</label>
                                        <div className="qr-token-container">
                                            <input
                                                type="text"
                                                className="qr-token-input"
                                                value={qrCodeData.qrToken}
                                                readOnly
                                            />
                                            <button
                                                className="copy-token-btn"
                                                onClick={handleCopyToken}
                                                title="Copy Token"
                                            >
                                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <rect x="9" y="9" width="13" height="13" rx="2" stroke="currentColor" strokeWidth="2"/>
                                                    <path d="M5 15H4C2.89543 15 2 14.1046 2 13V4C2 2.89543 2.89543 2 4 2H13C14.1046 2 15 2.89543 15 4V5" stroke="currentColor" strokeWidth="2"/>
                                                </svg>
                                            </button>
                                        </div>
                                        <small className="qr-expires-hint">
                                            QR code URL expires in {qrCodeData.expiresIn} minutes
                                        </small>
                                    </div>

                                    {/* Action Buttons */}
                                    <div className="qr-actions">
                                        <button className="qr-action-btn download-btn" onClick={handleDownloadQr}>
                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                <path d="M7 10L12 15L17 10" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                <path d="M12 15V3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                            </svg>
                                            Download QR
                                        </button>
                                        <button className="qr-action-btn print-btn" onClick={handlePrintQr}>
                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M6 9V2H18V9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                <path d="M6 18H4C3.46957 18 2.96086 17.7893 2.58579 17.4142C2.21071 17.0391 2 16.5304 2 16V11C2 10.4696 2.21071 9.96086 2.58579 9.58579C2.96086 9.21071 3.46957 9 4 9H20C20.5304 9 21.0391 9.21071 21.4142 9.58579C21.7893 9.96086 22 10.4696 22 11V16C22 16.5304 21.7893 17.0391 21.4142 17.4142C21.0391 17.7893 20.5304 18 20 18H18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                <rect x="6" y="14" width="12" height="8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                            </svg>
                                            Print QR
                                        </button>
                                    </div>
                                </>
                            ) : null}
                        </div>

                        <div className="modal-footer">
                            <button type="button" className="btn-secondary" onClick={handleCloseQrModal}>
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default OutgoingStock;