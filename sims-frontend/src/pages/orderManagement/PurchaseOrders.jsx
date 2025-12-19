import { useState, useEffect } from 'react';
import purchaseOrderService from '../../services/orderManagement/purchaseOrderService.js';
import productService from '../../services/productService.js';
import supplierService from '../../services/shared/supplierService.js';
import authService from '../../services/userManagement/authService.js';
import Toast from '../../components/common/Toast.jsx';
import './PurchaseOrders.css';

const PurchaseOrders = () => {
    const currentUser = authService.getCurrentUser();
    const isAdminOrManager = currentUser?.role === 'ROLE_ADMIN' || currentUser?.role === 'ROLE_MANAGER';

    const [metrics, setMetrics] = useState(null);
    const [orders, setOrders] = useState(null);
    const [isLoadingMetrics, setIsLoadingMetrics] = useState(true);
    const [isLoadingOrders, setIsLoadingOrders] = useState(true);
    const [error, setError] = useState(null);

    // Toast notification
    const [toast, setToast] = useState(null);

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
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showDetailsModal, setShowDetailsModal] = useState(false);
    const [showCancelModal, setShowCancelModal] = useState(false);
    const [selectedOrder, setSelectedOrder] = useState(null);
    const [orderDetails, setOrderDetails] = useState(null);
    const [isLoadingDetails, setIsLoadingDetails] = useState(false);

    // Create PO form data
    const [products, setProducts] = useState([]);
    const [suppliers, setSuppliers] = useState([]);
    const [isLoadingProducts, setIsLoadingProducts] = useState(false);
    const [isLoadingSuppliers, setIsLoadingSuppliers] = useState(false);
    const [formData, setFormData] = useState({
        productId: '',
        orderQuantity: '',
        supplierId: '',
        notes: '',
    });
    const [formErrors, setFormErrors] = useState({});

    // Purchase Order Statuses
    const orderStatuses = [
        { value: 'AWAITING_APPROVAL', label:  'Awaiting Approval' },
        { value: 'DELIVERY_IN_PROCESS', label: 'Delivery In Process' },
        { value:  'PARTIALLY_RECEIVED', label: 'Partially Received' },
        { value:  'RECEIVED', label: 'Received' },
        { value: 'CANCELLED', label: 'Cancelled' },
        { value: 'FAILED', label: 'Failed' },
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

    // Show toast notification
    const showToast = (message, type = 'success') => {
        setToast({ message, type });
    };

    // Close toast
    const closeToast = () => {
        setToast(null);
    };

    // Fetch metrics
    const fetchMetrics = async () => {
        setIsLoadingMetrics(true);
        try {
            const data = await purchaseOrderService.getMetrics();
            setMetrics(data.data);
        } catch (err) {
            console.error('Error fetching metrics:', err);
        } finally {
            setIsLoadingMetrics(false);
        }
    };

    // Fetch all orders
    const fetchAllOrders = async () => {
        setIsLoadingOrders(true);
        setError(null);

        try {
            const params = {};
            if (currentPage > 0) params.page = currentPage;

            const data = await purchaseOrderService.getAllPurchaseOrders(params);
            setOrders(data);
        } catch (err) {
            console.error('Error fetching orders:', err);
            setError(err.message || 'Failed to load orders');
        } finally {
            setIsLoadingOrders(false);
        }
    };

    // Fetch overdue orders (filter by status and check dates)
    const fetchOverdueOrders = async () => {
        setIsLoadingOrders(true);
        setError(null);

        try {
            const params = {};
            if (currentPage > 0) params.page = currentPage;

            // Get all orders and filter overdue on frontend
            const data = await purchaseOrderService.getAllPurchaseOrders(params);

            // Filter overdue orders
            const today = new Date();
            today.setHours(0, 0, 0, 0);

            const overdueContent = data.content.filter(order => {
                if (! order.expectedArrivalDate) return false;
                const eta = new Date(order.expectedArrivalDate);
                eta.setHours(0, 0, 0, 0);
                return eta < today &&
                    order.status !== 'RECEIVED' &&
                    order.status !== 'CANCELLED' &&
                    order.status !== 'FAILED';
            });

            setOrders({
                ...data,
                content: overdueContent,
                totalElements: overdueContent.length,
            });
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

            const data = await purchaseOrderService.searchOrders(params);
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

            const data = await purchaseOrderService.filterOrders(params);
            setOrders(data);
        } catch (err) {
            console.error('Error filtering orders:', err);
            setError(err.message || 'Failed to filter orders');
        } finally {
            setIsLoadingOrders(false);
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
        setActiveFilter({ type: '', value: '' });
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

    // Open Create Modal
    const handleOpenCreateModal = async () => {
        setShowCreateModal(true);
        setFormData({
            productId: '',
            orderQuantity: '',
            supplierId: '',
            notes: '',
        });
        setFormErrors({});

        // Fetch products and suppliers
        setIsLoadingProducts(true);
        setIsLoadingSuppliers(true);

        try {
            const [productsData, suppliersData] = await Promise.all([
                productService.getAllProductsList(),
                supplierService.getAllSuppliers(),
            ]);
            setProducts(productsData || []);
            setSuppliers(suppliersData || []);
        } catch (err) {
            console.error('Error fetching data:', err);
            setFormErrors({ submit: 'Failed to load products and suppliers' });
        } finally {
            setIsLoadingProducts(false);
            setIsLoadingSuppliers(false);
        }
    };

    // Close Create Modal
    const handleCloseCreateModal = () => {
        setShowCreateModal(false);
        setFormData({
            productId: '',
            orderQuantity: '',
            supplierId:  '',
            notes: '',
        });
        setFormErrors({});
        setProducts([]);
        setSuppliers([]);
    };

    // Open Details Modal
    const handleOpenDetailsModal = async (order) => {
        setSelectedOrder(order);
        setShowDetailsModal(true);
        setIsLoadingDetails(true);

        try {
            const details = await purchaseOrderService.getOrderDetails(order.id);
            setOrderDetails(details);
        } catch (err) {
            console.error('Error fetching order details:', err);
            setError(err.message || 'Failed to load order details');
        } finally {
            setIsLoadingDetails(false);
        }
    };

    // Close Details Modal
    const handleCloseDetailsModal = () => {
        setShowDetailsModal(false);
        setOrderDetails(null);
        setSelectedOrder(null);
    };

    // Open Cancel Modal
    const handleOpenCancelModal = (order) => {
        setSelectedOrder(order);
        setShowCancelModal(true);
    };

    // Handle form input change
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        // Clear error for this field
        if (formErrors[name]) {
            setFormErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }
    };

    // Validate create form
    const validateCreateForm = () => {
        const errors = {};

        if (!formData.productId) {
            errors.productId = 'Product is required';
        }

        if (!formData.supplierId) {
            errors.supplierId = 'Supplier is required';
        }

        if (!formData.orderQuantity || formData.orderQuantity < 1) {
            errors.orderQuantity = 'Order quantity must be at least 1';
        }

        if (formData.notes && formData.notes.length > 500) {
            errors.notes = 'Notes cannot exceed 500 characters';
        }

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // Handle Create Purchase Order
    const handleCreatePurchaseOrder = async (e) => {
        e.preventDefault();

        if (!validateCreateForm()) return;

        try {
            const createData = {
                productId: formData.productId,
                orderQuantity: parseInt(formData.orderQuantity),
                supplierId: parseInt(formData.supplierId),
                notes: formData.notes || undefined,
            };

            await purchaseOrderService.createPurchaseOrder(createData);

            // Show success toast
            showToast('Purchase order created successfully!  🎉', 'success');

            handleCloseCreateModal();
            fetchMetrics();
            if (viewMode === 'overdue') {
                fetchOverdueOrders();
            } else {
                fetchAllOrders();
            }
        } catch (err) {
            console.error('Error creating purchase order:', err);
            setFormErrors({ submit: err.message || 'Failed to create purchase order' });
        }
    };

    // Handle Cancel Order
    const handleCancelOrder = async () => {
        try {
            await purchaseOrderService.cancelOrder(selectedOrder.id);
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
        if (!expectedDate) return false;
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const eta = new Date(expectedDate);
        eta.setHours(0, 0, 0, 0);
        return eta < today;
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

    const getETADate = (order) => {
        if (order.status === 'RECEIVED' && order.actualArrivalDate) {
            return formatDate(order.actualArrivalDate);
        }
        return formatDate(order.expectedArrivalDate);
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
                <p>Loading purchase orders...</p>
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
        <div className="purchase-orders-page">

            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">Purchase Orders</h1>
                    <p className="page-subtitle">Manage and track all purchase orders</p>
                </div>
            </div>

            {/* Metrics Cards */}
            <div className="metrics-grid-po">
                <div className="metric-card-po metric-total">
                    <div className="metric-icon-wrapper-po">
                        <svg className="metric-icon-po" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 2H15M9 2V6M15 2V6M9 2C7.89543 2 7 2.89543 7 4V6H17V4C17 2.89543 16.1046 2 15 2Z" stroke="currentColor" strokeWidth="2"/>
                            <rect x="3" y="8" width="18" height="13" rx="2" stroke="currentColor" strokeWidth="2"/>
                        </svg>
                    </div>
                    <div className="metric-content-po">
                        <p className="metric-label-po">Total Orders</p>
                        <h2 className="metric-value-po">{metrics?.totalOrders || 0}</h2>
                        <p className="metric-sublabel-po">All purchase orders</p>
                    </div>
                </div>

                <div className="metric-card-po metric-pending">
                    <div className="metric-icon-wrapper-po">
                        <svg className="metric-icon-po" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                            <path d="M12 6V12L16 14" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-po">
                        <p className="metric-label-po">Pending</p>
                        <h2 className="metric-value-po">{metrics?.totalAwaitingApproval || 0}</h2>
                        <p className="metric-sublabel-po">Awaiting approval</p>
                    </div>
                </div>

                <div className="metric-card-po metric-delivery">
                    <div className="metric-icon-wrapper-po">
                        <svg className="metric-icon-po" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="1" y="3" width="15" height="13" rx="2" stroke="currentColor" strokeWidth="2"/>
                            <path d="M16 8H20L23 11V16H16V8Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
                            <circle cx="5.5" cy="18.5" r="2.5" stroke="currentColor" strokeWidth="2"/>
                            <circle cx="18.5" cy="18.5" r="2.5" stroke="currentColor" strokeWidth="2"/>
                        </svg>
                    </div>
                    <div className="metric-content-po">
                        <p className="metric-label-po">In Delivery</p>
                        <h2 className="metric-value-po">
                            {(metrics?.totalDeliveryInProcess || 0) + (metrics?.totalPartiallyReceived || 0)}
                        </h2>
                        <p className="metric-sublabel-po">On the way</p>
                    </div>
                </div>

                <div className="metric-card-po metric-received">
                    <div className="metric-icon-wrapper-po">
                        <svg className="metric-icon-po" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 11L12 14L22 4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-po">
                        <p className="metric-label-po">Received</p>
                        <h2 className="metric-value-po">{metrics?.totalReceived || 0}</h2>
                        <p className="metric-sublabel-po">Completed</p>
                    </div>
                </div>
            </div>

            {/* Orders Table Section */}
            <div className="orders-section">
                {/* Section Header */}
                <div className="section-header-po">
                    {/* Left:  View Mode Tabs */}
                    <div className="view-mode-tabs-po">
                        <button
                            className={`tab-btn-po ${viewMode === 'all' ? 'active' : ''}`}
                            onClick={() => handleToggleViewMode('all')}
                        >
                            <span className="tab-label">All Purchase Orders</span>
                            <span className="tab-count">{viewMode === 'all' ? orders?.totalElements || 0 : metrics?.totalOrders || 0}</span>
                        </button>
                        <button
                            className={`tab-btn-po ${viewMode === 'overdue' ?   'active' : ''}`}
                            onClick={() => handleToggleViewMode('overdue')}
                        >
                            <span className="tab-label">⚠️ Overdue Orders</span>
                            <span className="tab-count">{viewMode === 'overdue' ?  orders?.totalElements || 0 : 0}</span>
                        </button>
                    </div>

                    {/* Right: Actions */}
                    <div className="header-actions-po">
                        {isAdminOrManager && (
                            <button className="create-po-btn" onClick={handleOpenCreateModal}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M12 5V19M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                                Create New PO
                            </button>
                        )}

                        {/* Search Bar */}
                        <form className="search-form-po" onSubmit={handleSearch}>
                            <div className="search-input-wrapper-po">
                                <svg className="search-icon-po" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="2"/>
                                    <path d="M21 21L16.65 16.65" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                                <input
                                    type="text"
                                    className="search-input-po"
                                    placeholder="Search by PO#, product, supplier..."
                                    value={searchText}
                                    onChange={(e) => setSearchText(e.target.value)}
                                />
                                {searchText && (
                                    <button type="button" className="clear-search-btn-po" onClick={handleClearSearch}>
                                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                        </svg>
                                    </button>
                                )}
                            </div>
                            <button type="submit" className="search-btn-po">
                                Search
                            </button>
                        </form>

                        {/* Filter Button */}
                        <button
                            className={`filter-btn-po ${activeFilter.value ?   'active' : ''}`}
                            onClick={() => setShowFilters(!showFilters)}
                        >
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M22 3H2L10 12.46V19L14 21V12.46L22 3Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                            Filter
                            {activeFilter.value && <span className="filter-count-po">1</span>}
                        </button>
                    </div>
                </div>

                {/* Filter Panel */}
                {showFilters && (
                    <div className="filter-panel-po">
                        <div className="filter-grid-double-po">
                            <div className="filter-group-po">
                                <label className="filter-label-po">Filter By</label>
                                <select
                                    className="filter-select-po"
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
                                <div className="filter-group-po">
                                    <label className="filter-label-po">
                                        {filterType === 'status' ?   'Select Status' : 'Select Category'}
                                    </label>
                                    <select
                                        className="filter-select-po"
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

                        <div className="filter-actions-po">
                            <button className="clear-filters-btn-po" onClick={handleClearFilter}>
                                Clear
                            </button>
                            <button className="apply-filters-btn-po" onClick={handleApplyFilter}>
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
                {! isLoadingOrders && orders?.content?.length > 0 ?    (
                    <>
                        <div className="table-container">
                            <table className="orders-table">
                                <thead>
                                <tr>
                                    <th>PO Number</th>
                                    <th>Product</th>
                                    <th>Category</th>
                                    <th>Supplier</th>
                                    <th>Ordered</th>
                                    <th>Received</th>
                                    <th>Status</th>
                                    <th>Order Date</th>
                                    <th>ETA</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {orders.content.map((order) => {
                                    const overdue = isOverdue(order.expectedArrivalDate) &&
                                        order.status !== 'RECEIVED' &&
                                        order.status !== 'CANCELLED' &&
                                        order.status !== 'FAILED';

                                    return (
                                        <tr key={order.id} className={overdue ? 'row-overdue' : ''}>
                                            <td className="po-number-cell">{order.poNumber}</td>
                                            <td className="product-name-po">{order.productName}</td>
                                            <td>
                                                {order.productCategory && (
                                                    <span
                                                        className="category-badge-po"
                                                        style={{
                                                            backgroundColor:   `${getCategoryColor(order.productCategory)}20`,
                                                            color: getCategoryColor(order.productCategory),
                                                            borderLeft: `3px solid ${getCategoryColor(order.productCategory)}`
                                                        }}
                                                    >
                                                        {order.productCategory.replace(/_/g, ' ')}
                                                    </span>
                                                )}
                                            </td>
                                            <td>{order.supplierName}</td>
                                            <td className="ordered-cell">{order.orderedQuantity}</td>
                                            <td className="received-cell">
                                                {order.receivedQuantity || 0}
                                                {(order.receivedQuantity || 0) < order.orderedQuantity && (
                                                    <span className="remaining-hint">
                                                        ({order.orderedQuantity - (order.receivedQuantity || 0)} remaining)
                                                    </span>
                                                )}
                                            </td>
                                            <td>
                                                <span
                                                    className="status-badge-po"
                                                    style={{
                                                        backgroundColor:  `${getStatusColor(order.status)}20`,
                                                        color: getStatusColor(order.status),
                                                        border: `1px solid ${getStatusColor(order.status)}50`
                                                    }}
                                                >
                                                    {order.status.replace(/_/g, ' ')}
                                                </span>
                                            </td>
                                            <td>{formatDate(order.orderDate)}</td>
                                            <td>
                                                  <span className={`eta-badge ${overdue ? 'eta-overdue' : ''}`}>
                                                    {overdue && '⚠️ '}
                                                      {getETADate(order)}
                                                </span>
                                            </td>
                                            <td>
                                                <div className="action-buttons-po">
                                                    <button
                                                        className="action-btn-po view-btn"
                                                        onClick={() => handleOpenDetailsModal(order)}
                                                        title="View Details"
                                                    >
                                                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                            <path d="M1 12C1 12 5 4 12 4C19 4 23 12 23 12C23 12 19 20 12 20C5 20 1 12 1 12Z" stroke="currentColor" strokeWidth="2"/>
                                                            <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2"/>
                                                        </svg>
                                                    </button>
                                                    {isAdminOrManager && order.status !== 'RECEIVED' && order.status !== 'CANCELLED' && (
                                                        <button
                                                            className="action-btn-po delete-btn"
                                                            onClick={() => handleOpenCancelModal(order)}
                                                            title="Cancel Order"
                                                        >
                                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                <path d="M3 6H5H21" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                <path d="M8 6V4C8 3.46957 8.21071 2.96086 8.58579 2.58579C8.96086 2.21071 9.46957 2 10 2H14C14.5304 2 15.0391 2.21071 15.4142 2.58579C15.7893 2.96086 16 3.46957 16 4V6M19 6V20C19 20.5304 18.7893 21.0391 18.4142 21.4142C18.0391 21.7893 17.5304 22 17 22H7C6.46957 22 5.96086 21.7893 5.58579 21.4142C5.21071 21.0391 5 20.5304 5 20V6H19Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                            </svg>
                                                        </button>
                                                    )}
                                                </div>
                                            </td>
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
                ) : ! isLoadingOrders && (
                    <div className="empty-state">
                        <svg className="empty-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" strokeWidth="2"/>
                            <path d="M9 9H15M9 13H15M9 17H12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        </svg>
                        <h3>No Orders Found</h3>
                        <p>There are currently no {viewMode === 'overdue' ? 'overdue' : ''} purchase orders.</p>
                    </div>
                )}
            </div>

            {/* Create Purchase Order Modal */}
            {showCreateModal && (
                <div className="modal-overlay" onClick={handleCloseCreateModal}>
                    <div className="modal-content modal-large" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>📦 Create New Purchase Order</h2>
                            <button className="modal-close-btn" onClick={handleCloseCreateModal}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <form onSubmit={handleCreatePurchaseOrder}>
                            <div className="modal-body">
                                {formErrors.submit && (
                                    <div className="error-alert">
                                        {formErrors.submit}
                                    </div>
                                )}

                                {/* Product Selection */}
                                <div className="form-group">
                                    <label className="form-label">Product *</label>
                                    {isLoadingProducts ?  (
                                        <div className="loading-dropdown">
                                            <div className="spinner-small"></div>
                                            <span>Loading products...</span>
                                        </div>
                                    ) : (
                                        <select
                                            name="productId"
                                            className={`form-input ${formErrors.productId ? 'input-error' : ''}`}
                                            value={formData.productId}
                                            onChange={handleInputChange}
                                        >
                                            <option value="">Select a product</option>
                                            {products.map(product => (
                                                <option key={product.productId} value={product.productId}>
                                                    {product.name} ({product.productId}) - ${product.price?.toFixed(2)}
                                                </option>
                                            ))}
                                        </select>
                                    )}
                                    {formErrors.productId && <span className="error-text">{formErrors.productId}</span>}
                                </div>

                                {/* Supplier Selection */}
                                <div className="form-group">
                                    <label className="form-label">Supplier *</label>
                                    {isLoadingSuppliers ? (
                                        <div className="loading-dropdown">
                                            <div className="spinner-small"></div>
                                            <span>Loading suppliers...</span>
                                        </div>
                                    ) : (
                                        <select
                                            name="supplierId"
                                            className={`form-input ${formErrors.supplierId ? 'input-error' : ''}`}
                                            value={formData.supplierId}
                                            onChange={handleInputChange}
                                        >
                                            <option value="">Select a supplier</option>
                                            {suppliers.map(supplier => (
                                                <option key={supplier.id} value={supplier.id}>
                                                    {supplier.name} - {supplier.contactPerson}
                                                </option>
                                            ))}
                                        </select>
                                    )}
                                    {formErrors.supplierId && <span className="error-text">{formErrors.supplierId}</span>}
                                </div>

                                {/* Order Quantity */}
                                <div className="form-group">
                                    <label className="form-label">Order Quantity *</label>
                                    <input
                                        type="number"
                                        name="orderQuantity"
                                        min="1"
                                        className={`form-input ${formErrors.orderQuantity ? 'input-error' : ''}`}
                                        placeholder="Enter quantity"
                                        value={formData.orderQuantity}
                                        onChange={handleInputChange}
                                    />
                                    {formErrors.orderQuantity && <span className="error-text">{formErrors.orderQuantity}</span>}
                                    <small className="form-help">Minimum quantity: 1 unit</small>
                                </div>

                                {/* Notes */}
                                <div className="form-group">
                                    <label className="form-label">Notes (Optional)</label>
                                    <textarea
                                        name="notes"
                                        className={`form-textarea ${formErrors.notes ? 'input-error' : ''}`}
                                        placeholder="Add any additional notes..."
                                        rows="4"
                                        maxLength="500"
                                        value={formData.notes}
                                        onChange={handleInputChange}
                                    ></textarea>
                                    {formErrors.notes && <span className="error-text">{formErrors.notes}</span>}
                                    <small className="form-help">
                                        {formData.notes.length}/500 characters
                                    </small>
                                </div>
                            </div>

                            <div className="modal-footer">
                                <button type="button" className="btn-secondary" onClick={handleCloseCreateModal}>
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="btn-primary"
                                    disabled={isLoadingProducts || isLoadingSuppliers}
                                >
                                    Create Purchase Order
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* View Details Modal */}
            {showDetailsModal && (
                <div className="modal-overlay" onClick={handleCloseDetailsModal}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>📋 Purchase Order Details</h2>
                            <button className="modal-close-btn" onClick={handleCloseDetailsModal}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <div className="modal-body">
                            {isLoadingDetails ? (
                                <div className="loading-details">
                                    <div className="spinner-small"></div>
                                    <p>Loading order details...</p>
                                </div>
                            ) : orderDetails ?    (
                                <>
                                    {/* Order Information */}
                                    <div className="details-section">
                                        <h3 className="details-title">Order Information</h3>
                                        <div className="details-grid">
                                            <div className="detail-item">
                                                <span className="detail-label">PO Number:</span>
                                                <span className="detail-value po-number">{orderDetails.poNumber}</span>
                                            </div>
                                            <div className="detail-item">
                                                <span className="detail-label">Status:</span>
                                                <span
                                                    className="status-badge-po"
                                                    style={{
                                                        backgroundColor:    `${getStatusColor(orderDetails.status)}20`,
                                                        color: getStatusColor(orderDetails.status),
                                                        border: `1px solid ${getStatusColor(orderDetails.status)}50`
                                                    }}
                                                >
                                                    {orderDetails.status.replace(/_/g, ' ')}
                                                </span>
                                            </div>
                                            <div className="detail-item">
                                                <span className="detail-label">Order Date: </span>
                                                <span className="detail-value">{formatDate(orderDetails.orderDate)}</span>
                                            </div>
                                            <div className="detail-item">
                                                <span className="detail-label">Expected Arrival:</span>
                                                <span className="detail-value">{formatDate(orderDetails.expectedArrivalDate)}</span>
                                            </div>
                                            {orderDetails.actualArrivalDate && (
                                                <div className="detail-item">
                                                    <span className="detail-label">Actual Arrival:</span>
                                                    <span className="detail-value">{formatDate(orderDetails.actualArrivalDate)}</span>
                                                </div>
                                            )}
                                        </div>
                                    </div>

                                    {/* Product Information */}
                                    <div className="details-section">
                                        <h3 className="details-title">Product Information</h3>
                                        <div className="details-grid">
                                            <div className="detail-item">
                                                <span className="detail-label">Product Name:</span>
                                                <span className="detail-value product-name">{orderDetails.productName}</span>
                                            </div>
                                            <div className="detail-item">
                                                <span className="detail-label">Category:</span>
                                                {orderDetails.productCategory && (
                                                    <span
                                                        className="category-badge-po"
                                                        style={{
                                                            backgroundColor:    `${getCategoryColor(orderDetails.productCategory)}20`,
                                                            color: getCategoryColor(orderDetails.productCategory),
                                                            borderLeft: `3px solid ${getCategoryColor(orderDetails.productCategory)}`
                                                        }}
                                                    >
                                                        {orderDetails.productCategory.replace(/_/g, ' ')}
                                                    </span>
                                                )}
                                            </div>
                                        </div>
                                    </div>

                                    {/* Supplier Information */}
                                    <div className="details-section">
                                        <h3 className="details-title">Supplier Information</h3>
                                        <div className="details-grid">
                                            <div className="detail-item">
                                                <span className="detail-label">Supplier:</span>
                                                <span className="detail-value">{orderDetails.supplierName}</span>
                                            </div>
                                        </div>
                                    </div>

                                    {/* Quantity & Pricing */}
                                    <div className="details-section">
                                        <h3 className="details-title">Quantity & Pricing</h3>
                                        <div className="details-grid">
                                            <div className="detail-item">
                                                <span className="detail-label">Ordered Quantity:</span>
                                                <span className="detail-value quantity">{orderDetails.orderedQuantity} units</span>
                                            </div>
                                            <div className="detail-item">
                                                <span className="detail-label">Received Quantity:</span>
                                                <span className="detail-value quantity">{orderDetails.receivedQuantity || 0} units</span>
                                            </div>
                                            <div className="detail-item">
                                                <span className="detail-label">Remaining: </span>
                                                <span className="detail-value quantity">
                                                    {orderDetails.orderedQuantity - (orderDetails.receivedQuantity || 0)} units
                                                </span>
                                            </div>
                                            <div className="detail-item">
                                                <span className="detail-label">Total Price:</span>
                                                <span className="detail-value price">${orderDetails.totalPrice?.toFixed(2)}</span>
                                            </div>
                                        </div>

                                        {/* Progress Bar */}
                                        <div className="progress-section">
                                            <div className="progress-header">
                                                <span>Fulfillment Progress</span>
                                                <span>{Math.round(((orderDetails.receivedQuantity || 0) / orderDetails.orderedQuantity) * 100)}%</span>
                                            </div>
                                            <div className="progress-bar-po">
                                                <div
                                                    className="progress-fill-po"
                                                    style={{
                                                        width:  `${((orderDetails.receivedQuantity || 0) / orderDetails.orderedQuantity) * 100}%`,
                                                        backgroundColor: orderDetails.receivedQuantity === orderDetails.orderedQuantity ? '#4CAF50' : '#2196F3'
                                                    }}
                                                ></div>
                                            </div>
                                        </div>
                                    </div>

                                    {/* User Information */}
                                    <div className="details-section">
                                        <h3 className="details-title">User Information</h3>
                                        <div className="details-grid">
                                            <div className="detail-item">
                                                <span className="detail-label">Ordered By:</span>
                                                <span className="detail-value">{orderDetails.orderedBy || 'N/A'}</span>
                                            </div>
                                            {orderDetails.updatedBy && (
                                                <div className="detail-item">
                                                    <span className="detail-label">Updated By: </span>
                                                    <span className="detail-value">{orderDetails.updatedBy}</span>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </>
                            ) : (
                                <p>Failed to load order details</p>
                            )}
                        </div>

                        <div className="modal-footer">
                            <button type="button" className="btn-secondary" onClick={handleCloseDetailsModal}>
                                Close
                            </button>
                        </div>
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
                                    <span className="detail-label">Product: </span>
                                    <span className="detail-value">{selectedOrder?.productName}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Supplier:</span>
                                    <span className="detail-value">{selectedOrder?.supplierName}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Ordered Quantity:</span>
                                    <span className="detail-value">{selectedOrder?.orderedQuantity} units</span>
                                </div>
                            </div>

                            <p className="warning-text">
                                ⚠️ This action cannot be undone. The order will be cancelled and product status will be updated.
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

export default PurchaseOrders;