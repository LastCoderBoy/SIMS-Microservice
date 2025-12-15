import { useState, useEffect } from 'react';
import incomingStockService from '../services/incomingStockService.js';
import authService from '../services/authService';
import './IncomingStock.css';

const IncomingStock = () => {
    const currentUser = authService.getCurrentUser();
    const isAdminOrManager = currentUser?.role === 'ROLE_ADMIN' || currentUser?.role === 'ROLE_MANAGER';

    const [metrics, setMetrics] = useState(null);
    const [orders, setOrders] = useState(null);
    const [isLoadingMetrics, setIsLoadingMetrics] = useState(true);
    const [isLoadingOrders, setIsLoadingOrders] = useState(true);
    const [error, setError] = useState(null);

    // View mode:  'all' or 'overdue'
    const [viewMode, setViewMode] = useState('all');

    // Pagination
    const [currentPage, setCurrentPage] = useState(0);

    // Search
    const [searchText, setSearchText] = useState('');
    const [searchQuery, setSearchQuery] = useState('');

    // Filter
    const [showFilters, setShowFilters] = useState(false);
    const [filterType, setFilterType] = useState(''); // 'status' or 'category'
    const [filterValue, setFilterValue] = useState('');
    const [activeFilter, setActiveFilter] = useState({ type: '', value: '' });

    // Modals
    const [showReceiveModal, setShowReceiveModal] = useState(false);
    const [showCancelModal, setShowCancelModal] = useState(false);
    const [selectedOrder, setSelectedOrder] = useState(null);

    // Form data for receive stock
    const [formData, setFormData] = useState({
        receivedQuantity: '',
        actualArrivalDate: '',
    });
    const [formErrors, setFormErrors] = useState({});

    // Purchase Order Statuses (for filter)
    const orderStatuses = [
        { value: 'AWAITING_APPROVAL', label:  'Awaiting Approval' },
        { value: 'DELIVERY_IN_PROCESS', label: 'Delivery In Process' },
        { value: 'PARTIALLY_RECEIVED', label: 'Partially Received' },
    ];

    // Product Categories
    const productCategories = [
        { value: 'EDUCATION', label: 'Educational Toy' },
        { value: 'ELECTRONIC', label: 'Electronic Toy' },
        { value: 'ACTION_FIGURES', label: 'Action Figure' },
        { value: 'DOLLS', label: 'Dolls' },
        { value:  'MUSICAL_TOY', label: 'Musical Toy' },
        { value:  'OUTDOOR_TOY', label: 'Outdoor Toy' },
    ];

    // Status colors
    const statusColors = {
        AWAITING_APPROVAL:  '#FF9800',
        DELIVERY_IN_PROCESS: '#2196F3',
        PARTIALLY_RECEIVED: '#9C27B0',
        RECEIVED: '#4CAF50',
        CANCELLED: '#F44336',
        FAILED: '#D32F2F',
    };

    // Category colors
    const categoryColors = {
        EDUCATION: '#4CAF50',
        ELECTRONIC:  '#2196F3',
        ACTION_FIGURES: '#FF5722',
        DOLLS:  '#E91E63',
        MUSICAL_TOY: '#9C27B0',
        OUTDOOR_TOY: '#FF9800',
    };

    // Fetch metrics
    const fetchMetrics = async () => {
        setIsLoadingMetrics(true);
        try {
            const data = await incomingStockService.getMetrics();
            setMetrics(data.data);
        } catch (err) {
            console.error('Error fetching metrics:', err);
        } finally {
            setIsLoadingMetrics(false);
        }
    };

    // Fetch all pending orders
    const fetchAllOrders = async () => {
        setIsLoadingOrders(true);
        setError(null);

        try {
            const params = {};
            if (currentPage > 0) params.page = currentPage;

            const data = await incomingStockService.getAllPendingOrders(params);
            setOrders(data);
        } catch (err) {
            console.error('Error fetching orders:', err);
            setError(err.message || 'Failed to load orders');
        } finally {
            setIsLoadingOrders(false);
        }
    };

    // Fetch overdue orders
    const fetchOverdueOrders = async () => {
        setIsLoadingOrders(true);
        setError(null);

        try {
            const params = {};
            if (currentPage > 0) params.page = currentPage;

            const data = await incomingStockService.getOverdueOrders(params);
            setOrders(data);
        } catch (err) {
            console.error('Error fetching overdue orders:', err);
            setError(err.message || 'Failed to load overdue orders');
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

            const data = await incomingStockService.searchOrders(params);
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
            const params = {};
            if (activeFilter.type === 'status') {
                params.status = activeFilter.value;
            } else if (activeFilter.type === 'category') {
                params.category = activeFilter.value;
            }
            if (currentPage > 0) params.page = currentPage;

            const data = await incomingStockService.filterOrders(params);
            setOrders(data);
        } catch (err) {
            console.error('Error filtering orders:', err);
            setError(err.message || 'Failed to filter orders');
        } finally {
            setIsLoadingOrders(false);
        }
    };

    // Initial load - Fetch metrics
    useEffect(() => {
        fetchMetrics();
    }, []);

    // Fetch orders based on view mode, search, filter, pagination
    useEffect(() => {
        if (searchQuery) {
            searchOrders();
        } else if (activeFilter.value) {
            filterOrders();
        } else if (viewMode === 'overdue') {
            fetchOverdueOrders();
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
            setFilterType('');
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
            alert('Please select a filter value');
            return;
        }

        setCurrentPage(0);
        setSearchQuery('');
        setSearchText('');
        setViewMode('all');
        setShowFilters(false);
        setActiveFilter({ type: filterType, value: filterValue });
    };

    // Clear filter
    const handleClearFilter = () => {
        setFilterType('');
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
        setFilterType('');
        setFilterValue('');
    };

    // Handle page change
    const handlePageChange = (newPage) => {
        setCurrentPage(newPage);
    };

    // Open Receive Modal
    const handleOpenReceiveModal = (order) => {
        setSelectedOrder(order);
        const remaining = order.orderedQuantity - (order.receivedQuantity || 0);
        setFormData({
            receivedQuantity: remaining,
            actualArrivalDate: new Date().toISOString().slice(0, 10),
        });
        setFormErrors({});
        setShowReceiveModal(true);
    };

    // Open Cancel Modal
    const handleOpenCancelModal = (order) => {
        setSelectedOrder(order);
        setShowCancelModal(true);
    };

    // Validate receive form
    const validateReceiveForm = () => {
        const errors = {};
        const remaining = selectedOrder.orderedQuantity - (selectedOrder.receivedQuantity || 0);

        if (formData.receivedQuantity === '' || formData.receivedQuantity < 0) {
            errors.receivedQuantity = 'Received quantity must be 0 or greater';
        } else if (parseInt(formData.receivedQuantity) > remaining) {
            errors.receivedQuantity = `Cannot exceed remaining quantity (${remaining})`;
        }

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // Handle Receive Stock
    const handleReceiveStock = async (e) => {
        e.preventDefault();

        if (!validateReceiveForm()) return;

        try {
            const receiveData = {
                receivedQuantity: parseInt(formData.receivedQuantity),
            };

            if (formData.actualArrivalDate) {
                receiveData.actualArrivalDate = formData.actualArrivalDate;
            }

            await incomingStockService.receiveStock(selectedOrder.id, receiveData);
            setShowReceiveModal(false);
            fetchMetrics();
            if (viewMode === 'overdue') {
                fetchOverdueOrders();
            } else {
                fetchAllOrders();
            }
        } catch (err) {
            console.error('Error receiving stock:', err);
            setFormErrors({ submit: err.message || 'Failed to receive stock' });
        }
    };

    // Handle Cancel Order
    const handleCancelOrder = async () => {
        try {
            await incomingStockService.cancelOrder(selectedOrder.id);
            setShowCancelModal(false);
            fetchMetrics();
            if (viewMode === 'overdue') {
                fetchOverdueOrders();
            } else {
                fetchAllOrders();
            }
        } catch (err) {
            console.error('Error cancelling order:', err);
            setError(err.message || 'Failed to cancel order');
        }
    };

    // Check if order is overdue
    const isOverdue = (expectedDate) => {
        const today = new Date();
        const eta = new Date(expectedDate);
        return eta < today;
    };

    // Get status colour
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

    // Calculate days until ETA
    const getDaysUntilETA = (expectedDate) => {
        const today = new Date();
        const eta = new Date(expectedDate);
        const diffTime = eta - today;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        if (diffDays < 0) return `${Math.abs(diffDays)}d overdue`;
        if (diffDays === 0) return 'Today';
        if (diffDays === 1) return 'Tomorrow';
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

    // ... (previous code)

    if (isLoadingMetrics || (isLoadingOrders && !orders)) {
        return (
            <div className="loading-container">
                <div className="spinner-large"></div>
                <p>Loading purchase orders...</p>
            </div>
        );
    }

    if (error && ! orders) {
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
        <div className="incoming-stock-page">
            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">Incoming Stock</h1>
                    <p className="page-subtitle">Manage pending purchase orders awaiting delivery</p>
                </div>
            </div>

            {/* Metrics Cards */}
            <div className="metrics-grid-is">
                <div className="metric-card-is metric-pending">
                    <div className="metric-icon-wrapper-is">
                        <svg className="metric-icon-is" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 2H15M9 2V6M15 2V6M9 2C7.89543 2 7 2.89543 7 4V6H17V4C17 2.89543 16.1046 2 15 2Z" stroke="currentColor" strokeWidth="2"/>
                            <rect x="3" y="8" width="18" height="13" rx="2" stroke="currentColor" strokeWidth="2"/>
                        </svg>
                    </div>
                    <div className="metric-content-is">
                        <p className="metric-label-is">Pending Orders</p>
                        <h2 className="metric-value-is">{metrics?.totalValid || 0}</h2>
                        <p className="metric-sublabel-is">Awaiting delivery</p>
                    </div>
                </div>

                <div className="metric-card-is metric-transit">
                    <div className="metric-icon-wrapper-is">
                        <svg className="metric-icon-is" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-is">
                        <p className="metric-label-is">In Transit</p>
                        <h2 className="metric-value-is">{metrics?.totalDeliveryInProcess || 0}</h2>
                        <p className="metric-sublabel-is">On the way</p>
                    </div>
                </div>
            </div>

            {/* Orders Table Section */}
            <div className="orders-section">
                {/* Section Header with View Toggle */}
                <div className="section-header-combined">
                    <div className="section-title-wrapper">
                        <h2 className="section-title">Purchase Orders</h2>
                        <p className="section-subtitle">
                            {orders?.totalElements || 0} {viewMode === 'overdue' ?  'overdue' : 'total'} orders
                        </p>
                    </div>

                    <div className="header-actions-wrapper">
                        {/* View Mode Tabs */}
                        <div className="view-mode-tabs">
                            <button
                                className={`tab-btn ${viewMode === 'all' ? 'active' : ''}`}
                                onClick={() => handleToggleViewMode('all')}
                            >
                                All Orders
                            </button>
                            <button
                                className={`tab-btn ${viewMode === 'overdue' ? 'active' :  ''}`}
                                onClick={() => handleToggleViewMode('overdue')}
                            >
                                ⚠️ Overdue
                            </button>
                        </div>

                        <div className="search-filter-controls">
                            {/* Search Bar */}
                            <form className="search-form" onSubmit={handleSearch}>
                                <div className="search-input-wrapper">
                                    <svg className="search-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="2"/>
                                        <path d="M21 21L16.65 16.65" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                    </svg>
                                    <input
                                        type="text"
                                        className="search-input"
                                        placeholder="Search by PO#, supplier, product..."
                                        value={searchText}
                                        onChange={(e) => setSearchText(e.target.value)}
                                    />
                                    {searchText && (
                                        <button type="button" className="clear-search-btn" onClick={handleClearSearch}>
                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                            </svg>
                                        </button>
                                    )}
                                </div>
                                <button type="submit" className="search-btn">
                                    Search
                                </button>
                            </form>

                            {/* Filter Button */}
                            <button
                                className={`filter-btn ${activeFilter.value ? 'active' : ''}`}
                                onClick={() => setShowFilters(!showFilters)}
                            >
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M22 3H2L10 12.46V19L14 21V12.46L22 3Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                </svg>
                                Filter
                                {activeFilter.value && <span className="filter-count">1</span>}
                            </button>
                        </div>
                    </div>
                </div>

                {/* Filter Panel */}
                {showFilters && (
                    <div className="filter-panel">
                        <div className="filter-grid-double">
                            <div className="filter-group">
                                <label className="filter-label">Filter By</label>
                                <select
                                    className="filter-select"
                                    value={filterType}
                                    onChange={(e) => {
                                        setFilterType(e.target.value);
                                        setFilterValue('');
                                    }}
                                >
                                    <option value="">Select filter type</option>
                                    <option value="status">Status</option>
                                    <option value="category">Category</option>
                                </select>
                            </div>

                            {filterType && (
                                <div className="filter-group">
                                    <label className="filter-label">
                                        {filterType === 'status' ?  'Select Status' : 'Select Category'}
                                    </label>
                                    <select
                                        className="filter-select"
                                        value={filterValue}
                                        onChange={(e) => setFilterValue(e.target.value)}
                                    >
                                        <option value="">Select value</option>
                                        {filterType === 'status' && orderStatuses.map(status => (
                                            <option key={status.value} value={status.value}>
                                                {status.label}
                                            </option>
                                        ))}
                                        {filterType === 'category' && productCategories.map(cat => (
                                            <option key={cat.value} value={cat.value}>
                                                {cat.label}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                            )}
                        </div>

                        <div className="filter-actions">
                            <button className="clear-filters-btn" onClick={handleClearFilter}>
                                Clear
                            </button>
                            <button className="apply-filters-btn" onClick={handleApplyFilter}>
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
                {!isLoadingOrders && orders?.content?.length > 0 ?  (
                    <>
                        <div className="table-container">
                            <table className="orders-table">
                                <thead>
                                <tr>
                                    <th>PO Number</th>
                                    <th>Supplier</th>
                                    <th>Product</th>
                                    <th>Category</th>
                                    <th>Ordered</th>
                                    <th>Received</th>
                                    <th>Progress</th>
                                    <th>ETA</th>
                                    <th>Status</th>
                                    {isAdminOrManager && <th>Actions</th>}
                                </tr>
                                </thead>
                                <tbody>
                                {orders.content.map((order) => {
                                    const remaining = order.orderedQuantity - (order.receivedQuantity || 0);
                                    const progress = ((order.receivedQuantity || 0) / order.orderedQuantity) * 100;
                                    const overdue = isOverdue(order.expectedArrivalDate);

                                    return (
                                        <tr key={order.id} className={overdue ? 'row-overdue' : ''}>
                                            <td className="po-number-cell">{order.poNumber}</td>
                                            <td>{order.supplierName}</td>
                                            <td className="product-name-is">{order.productName}</td>
                                            <td>
                                                {order.productCategory && (
                                                    <span
                                                        className="category-badge-is"
                                                        style={{
                                                            backgroundColor: `${getCategoryColor(order.productCategory)}20`,
                                                            color: getCategoryColor(order.productCategory),
                                                            borderLeft: `3px solid ${getCategoryColor(order.productCategory)}`
                                                        }}
                                                    >
                              {order.productCategory.replace(/_/g, ' ')}
                            </span>
                                                )}
                                            </td>
                                            <td className="ordered-cell">{order.orderedQuantity}</td>
                                            <td className="received-cell">
                                                {order.receivedQuantity || 0}
                                                {remaining > 0 && (
                                                    <span className="remaining-hint">({remaining} remaining)</span>
                                                )}
                                            </td>
                                            <td>
                                                <div className="progress-wrapper">
                                                    <div className="progress-bar-is">
                                                        <div
                                                            className="progress-fill-is"
                                                            style={{
                                                                width: `${progress}%`,
                                                                backgroundColor: progress === 100 ? '#4CAF50' : '#2196F3'
                                                            }}
                                                        ></div>
                                                    </div>
                                                    <span className="progress-text">{progress.toFixed(0)}%</span>
                                                </div>
                                            </td>
                                            <td>
                          <span className={`eta-badge ${overdue ? 'eta-overdue' : ''}`}>
                            {overdue && '⚠️ '}
                              {getDaysUntilETA(order.expectedArrivalDate)}
                          </span>
                                            </td>
                                            <td>
                          <span
                              className="status-badge-is"
                              style={{
                                  backgroundColor:  `${getStatusColor(order.status)}20`,
                                  color: getStatusColor(order.status),
                                  border: `1px solid ${getStatusColor(order.status)}50`
                              }}
                          >
                            {order.status.replace(/_/g, ' ')}
                          </span>
                                            </td>
                                            {isAdminOrManager && (
                                                <td>
                                                    <div className="action-buttons-is">
                                                        <button
                                                            className="action-btn-is receive-btn"
                                                            onClick={() => handleOpenReceiveModal(order)}
                                                            title="Receive Stock"
                                                        >
                                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                <path d="M7 10L12 15L17 10" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                <path d="M12 15V3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                            </svg>
                                                        </button>
                                                        <button
                                                            className="action-btn-is cancel-btn"
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
                        <p>There are currently no {viewMode === 'overdue' ? 'overdue' : 'pending'} purchase orders.</p>
                    </div>
                )}
            </div>

            {/* Receive Stock Modal */}
            {showReceiveModal && (
                <div className="modal-overlay" onClick={() => setShowReceiveModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>📥 Receive Stock</h2>
                            <button className="modal-close-btn" onClick={() => setShowReceiveModal(false)}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <form onSubmit={handleReceiveStock}>
                            <div className="modal-body">
                                {formErrors.submit && (
                                    <div className="error-alert">
                                        {formErrors.submit}
                                    </div>
                                )}

                                {/* Order Details */}
                                <div className="order-details-box">
                                    <h3>Order Details</h3>
                                    <div className="detail-row">
                                        <span className="detail-label">PO Number:</span>
                                        <span className="detail-value">{selectedOrder?.poNumber}</span>
                                    </div>
                                    <div className="detail-row">
                                        <span className="detail-label">Product:</span>
                                        <span className="detail-value">{selectedOrder?.productName}</span>
                                    </div>
                                    <div className="detail-row">
                                        <span className="detail-label">Supplier:</span>
                                        <span className="detail-value">{selectedOrder?.supplierName}</span>
                                    </div>
                                    <div className="detail-row">
                                        <span className="detail-label">Ordered:</span>
                                        <span className="detail-value">{selectedOrder?.orderedQuantity} units</span>
                                    </div>
                                    <div className="detail-row">
                                        <span className="detail-label">Already Received:</span>
                                        <span className="detail-value">{selectedOrder?.receivedQuantity || 0} units</span>
                                    </div>
                                    <div className="detail-row highlight">
                                        <span className="detail-label">Remaining:</span>
                                        <span className="detail-value">
                      {selectedOrder?.orderedQuantity - (selectedOrder?.receivedQuantity || 0)} units
                    </span>
                                    </div>
                                </div>

                                <div className="form-group">
                                    <label className="form-label">Received Quantity *</label>
                                    <input
                                        type="number"
                                        min="0"
                                        max={selectedOrder?.orderedQuantity - (selectedOrder?.receivedQuantity || 0)}
                                        className={`form-input ${formErrors.receivedQuantity ? 'input-error' : ''}`}
                                        placeholder="Enter received quantity"
                                        value={formData.receivedQuantity}
                                        onChange={(e) => setFormData({ ...formData, receivedQuantity: e.target.value })}
                                    />
                                    {formErrors.receivedQuantity && <span className="error-text">{formErrors.receivedQuantity}</span>}
                                    <small className="form-help">
                                        Max: {selectedOrder?.orderedQuantity - (selectedOrder?.receivedQuantity || 0)} units
                                    </small>
                                </div>

                                <div className="form-group">
                                    <label className="form-label">Actual Arrival Date (Optional)</label>
                                    <input
                                        type="date"
                                        className="form-input"
                                        value={formData.actualArrivalDate}
                                        onChange={(e) => setFormData({ ...formData, actualArrivalDate: e.target.value })}
                                    />
                                    <small className="form-help">Leave empty to use today's date</small>
                                </div>
                            </div>

                            <div className="modal-footer">
                                <button type="button" className="btn-secondary" onClick={() => setShowReceiveModal(false)}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn-primary">
                                    Receive Stock
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
                            <h2>⚠️ Cancel Purchase Order</h2>
                            <button className="modal-close-btn" onClick={() => setShowCancelModal(false)}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <div className="modal-body">
                            <p>Are you sure you want to cancel this purchase order?</p>

                            <div className="order-details-box">
                                <div className="detail-row">
                                    <span className="detail-label">PO Number:</span>
                                    <span className="detail-value">{selectedOrder?.poNumber}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Product:</span>
                                    <span className="detail-value">{selectedOrder?.productName}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Supplier:</span>
                                    <span className="detail-value">{selectedOrder?.supplierName}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Ordered:</span>
                                    <span className="detail-value">{selectedOrder?.orderedQuantity} units</span>
                                </div>
                            </div>

                            <p className="warning-text">
                                ⚠️ This action cannot be undone. The order will be cancelled and inventory status will be updated.
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
        </div>
    );
};

export default IncomingStock;