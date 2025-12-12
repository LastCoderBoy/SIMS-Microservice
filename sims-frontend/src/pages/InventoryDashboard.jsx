import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import inventoryService from '../services/inventoryService';
import './InventoryDashboard.css';

const InventoryDashboard = () => {
    const navigate = useNavigate();
    const [dashboardData, setDashboardData] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // Pagination state
    const [currentPage, setCurrentPage] = useState(0);

    // Search state
    const [searchText, setSearchText] = useState('');
    const [searchQuery, setSearchQuery] = useState('');

    // Filter state - ONLY TYPE
    const [showFilters, setShowFilters] = useState(false);
    const [filterType, setFilterType] = useState('');
    const [activeFilterType, setActiveFilterType] = useState('');

    // Fetch dashboard data
    const fetchDashboardData = async () => {
        setIsLoading(true);
        setError(null);

        try {
            const data = await inventoryService.getDashboard(currentPage > 0 ? currentPage : undefined);
            setDashboardData(data);
        } catch (err) {
            console.error('Error fetching dashboard:', err);
            setError(err.message || 'Failed to load dashboard data');
        } finally {
            setIsLoading(false);
        }
    };

    // Search pending orders
    const searchPendingOrders = async () => {
        setIsLoading(true);
        setError(null);

        try {
            const data = await inventoryService.searchPendingOrders(
                searchQuery,
                currentPage > 0 ? currentPage : undefined
            );
            setDashboardData(prev => ({
                ...prev,
                allPendingOrders: data,
            }));
        } catch (err) {
            console.error('Error searching orders:', err);
            setError(err.message || 'Failed to search orders');
        } finally {
            setIsLoading(false);
        }
    };

    // Filter pending orders
    const filterPendingOrders = async () => {
        setIsLoading(true);
        setError(null);

        try {
            const filterParams = {};

            // Only add type if it's set
            if (activeFilterType) {
                filterParams.type = activeFilterType;
            }

            // Only add page if greater than 0
            if (currentPage > 0) {
                filterParams.page = currentPage;
            }

            console.log('[DASHBOARD] Calling filterPendingOrders with params:', filterParams);

            const data = await inventoryService.filterPendingOrders(filterParams);
            setDashboardData(prev => ({
                ...prev,
                allPendingOrders: data,
            }));
        } catch (err) {
            console.error('Error filtering orders:', err);
            setError(err.message || 'Failed to filter orders');
        } finally {
            setIsLoading(false);
        }
    };

    // Single useEffect to handle all data fetching
    useEffect(() => {
        if (searchQuery) {
            searchPendingOrders();
        } else if (activeFilterType) {
            filterPendingOrders();
        } else {
            fetchDashboardData();
        }
    }, [currentPage, searchQuery, activeFilterType]); // Dependencies:  page, search, filter

    // Handle search - ONLY update state, let useEffect handle the call
    const handleSearch = (e) => {
        e.preventDefault();
        if (searchText.trim()) {
            // Reset page and filter, then set search query (triggers useEffect)
            setCurrentPage(0);
            setActiveFilterType('');
            setFilterType('');
            setSearchQuery(searchText);
        } else {
            handleClearSearch();
        }
    };

    // Clear search - ONLY update state
    const handleClearSearch = () => {
        setSearchText('');
        setCurrentPage(0);
        setSearchQuery(''); // This triggers useEffect → fetchDashboardData
    };

    // Apply filter - ONLY update state, let useEffect handle the call
    const handleApplyFilter = () => {
        // Reset page and search, then set filter (triggers useEffect)
        setCurrentPage(0);
        setSearchQuery('');
        setSearchText('');
        setShowFilters(false);
        setActiveFilterType(filterType); // This triggers useEffect → filterPendingOrders
    };

    // Clear filter - ONLY update state
    const handleClearFilter = () => {
        setFilterType('');
        setCurrentPage(0);
        setShowFilters(false);
        setActiveFilterType(''); // This triggers useEffect → fetchDashboardData
    };

    // Handle page change
    const handlePageChange = (newPage) => {
        setCurrentPage(newPage); // This triggers useEffect with current search/filter
    };

    // Navigate to specific page
    const handleCardClick = (route) => {
        navigate(route);
    };

    // Format date
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
        });
    };

    // Get status badge class
    const getStatusClass = (status) => {
        const statusLower = status?.toLowerCase() || '';
        if (statusLower.includes('pending') || statusLower.includes('awaiting')) return 'status-pending';
        if (statusLower.includes('approved')) return 'status-approved';
        if (statusLower.includes('delivered') || statusLower.includes('received')) return 'status-delivered';
        if (statusLower.includes('process')) return 'status-process';
        if (statusLower.includes('cancelled') || statusLower.includes('failed')) return 'status-cancelled';
        return 'status-default';
    };

    // Generate page numbers for pagination
    const generatePageNumbers = () => {
        const totalPages = dashboardData?.allPendingOrders?.totalPages || 0;
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

    if (isLoading && !dashboardData) {
        return (
            <div className="loading-container">
                <div className="spinner-large"></div>
                <p>Loading dashboard...</p>
            </div>
        );
    }

    if (error && !dashboardData) {
        return (
            <div className="error-container">
                <svg className="error-icon-large" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                    <path d="M12 8V12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                </svg>
                <h2>Error Loading Dashboard</h2>
                <p>{error}</p>
                <button className="retry-btn" onClick={fetchDashboardData}>
                    Try Again
                </button>
            </div>
        );
    }

    return (
        <div className="inventory-dashboard">
            {/* Page Header */}
            <div className="dashboard-header-section">
                <div>
                    <h1 className="dashboard-title">Inventory Dashboard</h1>
                    <p className="dashboard-subtitle">Overview of your inventory and pending orders</p>
                </div>
                <button className="refresh-btn" onClick={() => {
                    setCurrentPage(0);
                    setSearchQuery('');
                    setSearchText('');
                    setActiveFilterType('');
                    setFilterType('');
                }}>
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21.5 2V8M21.5 8H15.5M21.5 8L18 4.5C16.7429 3.24286 15.125 2.5 13.5 2.5C9.35786 2.5 6 5.85786 6 10C6 10.3438 6.02222 10.6827 6.06558 11.0156" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        <path d="M2.5 22V16M2.5 16H8.5M2.5 16L6 19.5C7.25714 20.7571 8.875 21.5 10.5 21.5C14.6421 21.5 18 18.1421 18 14C18 13.6562 17.9778 13.3173 17.9344 12.9844" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                    Refresh
                </button>
            </div>

            {/* Metrics Cards */}
            <div className="metrics-grid">
                {/* Total Inventory */}
                <div
                    className="metric-card metric-primary clickable"
                    onClick={() => handleCardClick('/inventory/total-items')}
                >
                    <div className="metric-icon-wrapper">
                        <svg className="metric-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M20 7H4C2.9 7 2 7.9 2 9V19C2 20.1 2.9 21 4 21H20C21.1 21 22 20.1 22 19V9C22 7.9 21.1 7 20 7Z" stroke="currentColor" strokeWidth="2"/>
                            <path d="M16 7V5C16 3.9 15.1 3 14 3H10C8.9 3 8 3.9 8 5V7" stroke="currentColor" strokeWidth="2"/>
                        </svg>
                    </div>
                    <div className="metric-content">
                        <p className="metric-label">Total Inventory</p>
                        <h2 className="metric-value">{dashboardData?.totalInventorySize || 0}</h2>
                        <p className="metric-description">Click to view all items</p>
                    </div>
                    <svg className="card-arrow" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                </div>

                {/* Low Stock */}
                <div
                    className="metric-card metric-warning clickable"
                    onClick={() => handleCardClick('/inventory/low-stock')}
                >
                    <div className="metric-icon-wrapper">
                        <svg className="metric-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M10.29 3.86L1.82 18C1.64537 18.3024 1.55299 18.6453 1.55201 18.9945C1.55103 19.3437 1.64149 19.6871 1.81442 19.9905C1.98735 20.2939 2.23672 20.5467 2.53771 20.7239C2.8387 20.901 3.18089 20.9962 3.53 21H20.47C20.8191 20.9962 21.1613 20.901 21.4623 20.7239C21.7633 20.5467 22.0127 20.2939 22.1856 19.9905C22.3585 19.6871 22.449 19.3437 22.448 18.9945C22.447 18.6453 22.3546 18.3024 22.18 18L13.71 3.86C13.5317 3.56611 13.2807 3.32312 12.9812 3.15448C12.6817 2.98585 12.3437 2.89725 12 2.89725C11.6563 2.89725 11.3183 2.98585 11.0188 3.15448C10.7193 3.32312 10.4683 3.56611 10.29 3.86Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M12 9V13" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            <circle cx="12" cy="17" r="1" fill="currentColor"/>
                        </svg>
                    </div>
                    <div className="metric-content">
                        <p className="metric-label">Low Stock</p>
                        <h2 className="metric-value">{dashboardData?.lowStockSize || 0}</h2>
                        <p className="metric-description">Click to view low stock items</p>
                    </div>
                    <svg className="card-arrow" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                </div>

                {/* Incoming Stock */}
                <div
                    className="metric-card metric-success clickable"
                    onClick={() => handleCardClick('/inventory/incoming-stock')}
                >
                    <div className="metric-icon-wrapper">
                        <svg className="metric-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 5V19M12 5L6 11M12 5L18 11" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content">
                        <p className="metric-label">Incoming Stock</p>
                        <h2 className="metric-value">{dashboardData?.incomingStockSize || 0}</h2>
                        <p className="metric-description">Click to view purchase orders</p>
                    </div>
                    <svg className="card-arrow" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                </div>

                {/* Outgoing Stock */}
                <div
                    className="metric-card metric-info clickable"
                    onClick={() => handleCardClick('/inventory/outgoing-stock')}
                >
                    <div className="metric-icon-wrapper">
                        <svg className="metric-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 19V5M12 19L6 13M12 19L18 13" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content">
                        <p className="metric-label">Outgoing Stock</p>
                        <h2 className="metric-value">{dashboardData?.outgoingStockSize || 0}</h2>
                        <p className="metric-description">Click to view sales orders</p>
                    </div>
                    <svg className="card-arrow" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                </div>

                {/* Damage & Loss */}
                <div
                    className="metric-card metric-danger clickable"
                    onClick={() => handleCardClick('/inventory/damage-loss')}
                >
                    <div className="metric-icon-wrapper">
                        <svg className="metric-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                            <path d="M15 9L9 15M9 9L15 15" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        </svg>
                    </div>
                    <div className="metric-content">
                        <p className="metric-label">Damage & Loss</p>
                        <h2 className="metric-value">{dashboardData?.damageLossSize || 0}</h2>
                        <p className="metric-description">Click to view damaged items</p>
                    </div>
                    <svg className="card-arrow" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                </div>
            </div>

            {/* Pending Orders Section */}
            <div className="pending-orders-section">
                {/* Header with Search and Filter */}
                <div className="section-header-combined">
                    <div className="section-title-wrapper">
                        <h2 className="section-title">Pending Orders</h2>
                        <p className="section-subtitle">
                            {dashboardData?.allPendingOrders?.totalElements || 0} total orders
                        </p>
                    </div>

                    {/* Search and Filter Controls */}
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
                                    placeholder="Search by order reference, customer, supplier..."
                                    value={searchText}
                                    onChange={(e) => setSearchText(e.target.value)}
                                />
                                {searchText && (
                                    <button
                                        type="button"
                                        className="clear-search-btn"
                                        onClick={handleClearSearch}
                                    >
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
                            className={`filter-btn ${activeFilterType ?  'active' : ''}`}
                            onClick={() => setShowFilters(!showFilters)}
                        >
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M22 3H2L10 12.46V19L14 21V12.46L22 3Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                            Filter
                            {activeFilterType && (
                                <span className="filter-count">1</span>
                            )}
                        </button>
                    </div>
                </div>

                {/* Filter Panel - ONLY TYPE */}
                {showFilters && (
                    <div className="filter-panel">
                        <div className="filter-grid-single">
                            {/* Type Filter */}
                            <div className="filter-group">
                                <label className="filter-label">Order Type</label>
                                <select
                                    className="filter-select"
                                    value={filterType}
                                    onChange={(e) => setFilterType(e.target.value)}
                                >
                                    <option value="">All Types</option>
                                    <option value="SALES_ORDER">Sales Order</option>
                                    <option value="PURCHASE_ORDER">Purchase Order</option>
                                </select>
                            </div>
                        </div>

                        {/* Filter Actions */}
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

                {/* Loading indicator for table */}
                {isLoading && dashboardData && (
                    <div className="table-loading">
                        <div className="spinner-small"></div>
                        <span>Loading...</span>
                    </div>
                )}

                {/* Orders Table */}
                {! isLoading && dashboardData?.allPendingOrders?.content?.length > 0 ?  (
                    <>
                        <div className="table-container">
                            <table className="pending-orders-table">
                                <thead>
                                <tr>
                                    <th>Order Reference</th>
                                    <th>Type</th>
                                    <th>Status</th>
                                    <th>Customer/Supplier</th>
                                    <th>Order Date</th>
                                    <th>Estimated Date</th>
                                    <th>Quantity</th>
                                </tr>
                                </thead>
                                <tbody>
                                {dashboardData.allPendingOrders.content.map((order) => (
                                    <tr key={order.id}>
                                        <td className="order-ref">{order.orderReference}</td>
                                        <td>
                        <span className={`type-badge ${order.type === 'PURCHASE_ORDER' ? 'type-purchase' : 'type-sales'}`}>
                          {order.type === 'PURCHASE_ORDER' ?  'Purchase' : 'Sales'}
                        </span>
                                        </td>
                                        <td>
                        <span className={`status-badge ${getStatusClass(order.status)}`}>
                          {order.status.replace(/_/g, ' ')}
                        </span>
                                        </td>
                                        <td>{order.customerOrSupplierName}</td>
                                        <td>{formatDate(order.orderDate)}</td>
                                        <td>{formatDate(order.estimatedDate)}</td>
                                        <td className="quantity-cell">{order.totalOrderedQuantity}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        {/* Enhanced Pagination */}
                        {dashboardData.allPendingOrders.totalPages > 1 && (
                            <div className="pagination">
                                <button
                                    className="pagination-btn pagination-arrow"
                                    onClick={() => handlePageChange(currentPage - 1)}
                                    disabled={currentPage === 0}
                                    title="Previous page"
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

                                {currentPage < dashboardData.allPendingOrders.totalPages - 3 && (
                                    <>
                                        {currentPage < dashboardData.allPendingOrders.totalPages - 4 && (
                                            <span className="pagination-ellipsis">...</span>
                                        )}
                                        <button
                                            className="pagination-btn pagination-number"
                                            onClick={() => handlePageChange(dashboardData.allPendingOrders.totalPages - 1)}
                                        >
                                            {dashboardData.allPendingOrders.totalPages}
                                        </button>
                                    </>
                                )}

                                <button
                                    className="pagination-btn pagination-arrow"
                                    onClick={() => handlePageChange(currentPage + 1)}
                                    disabled={currentPage === dashboardData.allPendingOrders.totalPages - 1}
                                    title="Next page"
                                >
                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M9 18L15 12L9 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                </button>

                                <div className="pagination-info">
                                    Page {currentPage + 1} of {dashboardData.allPendingOrders.totalPages}
                                </div>
                            </div>
                        )}
                    </>
                ) : !isLoading && (
                    <div className="empty-state">
                        <svg className="empty-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 11L12 14L22 4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                        <h3>No Pending Orders</h3>
                        <p>There are currently no pending orders to display.</p>
                        {(searchQuery || activeFilterType) && (
                            <button className="clear-search-filter-btn" onClick={() => {
                                handleClearSearch();
                                handleClearFilter();
                            }}>
                                Clear Search & Filter
                            </button>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default InventoryDashboard;