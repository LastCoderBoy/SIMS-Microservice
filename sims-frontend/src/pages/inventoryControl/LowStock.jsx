import { useState, useEffect } from 'react';
import lowStockService from '../../services/inventoryControl/lowStockService.js';
import './LowStock.css';

const LowStock = () => {
    const [metrics, setMetrics] = useState(null);
    const [products, setProducts] = useState(null);
    const [isLoadingMetrics, setIsLoadingMetrics] = useState(true);
    const [isLoadingProducts, setIsLoadingProducts] = useState(true);
    const [error, setError] = useState(null);

    // Pagination
    const [currentPage, setCurrentPage] = useState(0);

    // Search
    const [searchText, setSearchText] = useState('');
    const [searchQuery, setSearchQuery] = useState('');

    // Filter
    const [showFilters, setShowFilters] = useState(false);
    const [filterCategory, setFilterCategory] = useState('');
    const [activeFilterCategory, setActiveFilterCategory] = useState('');

    // Product Categories
    const productCategories = [
        { value: 'EDUCATION', label:  'Educational Toy' },
        { value: 'ELECTRONIC', label: 'Electronic Toy' },
        { value: 'ACTION_FIGURES', label: 'Action Figure' },
        { value: 'DOLLS', label: 'Dolls' },
        { value:  'MUSICAL_TOY', label: 'Musical Toy' },
        { value:  'OUTDOOR_TOY', label: 'Outdoor Toy' },
    ];

    // Category colors
    const categoryColors = {
        EDUCATION: '#4CAF50',
        ELECTRONIC: '#2196F3',
        ACTION_FIGURES: '#FF5722',
        DOLLS:  '#E91E63',
        MUSICAL_TOY: '#9C27B0',
        OUTDOOR_TOY: '#FF9800',
    };

    // Fetch metrics
    const fetchMetrics = async () => {
        setIsLoadingMetrics(true);
        try {
            const data = await lowStockService.getMetrics();
            setMetrics(data.data);
        } catch (err) {
            console.error('Error fetching metrics:', err);
        } finally {
            setIsLoadingMetrics(false);
        }
    };

    // Fetch all low stock products
    const fetchAllProducts = async () => {
        setIsLoadingProducts(true);
        setError(null);

        try {
            const params = {};
            if (currentPage > 0) params.page = currentPage;

            const data = await lowStockService.getAllLowStockProducts(params);
            setProducts(data);
        } catch (err) {
            console.error('Error fetching products:', err);
            setError(err.message || 'Failed to load products');
        } finally {
            setIsLoadingProducts(false);
        }
    };

    // Search products
    const searchProducts = async () => {
        setIsLoadingProducts(true);
        setError(null);

        try {
            const params = { text: searchQuery };
            if (currentPage > 0) params.page = currentPage;

            const data = await lowStockService.searchLowStockProducts(params);
            setProducts(data);
        } catch (err) {
            console.error('Error searching products:', err);
            setError(err.message || 'Failed to search products');
        } finally {
            setIsLoadingProducts(false);
        }
    };

    // Filter products
    const filterProducts = async () => {
        setIsLoadingProducts(true);
        setError(null);

        try {
            const params = { category: activeFilterCategory };
            if (currentPage > 0) params.page = currentPage;

            const data = await lowStockService.filterLowStockProducts(params);
            setProducts(data);
        } catch (err) {
            console.error('Error filtering products:', err);
            setError(err.message || 'Failed to filter products');
        } finally {
            setIsLoadingProducts(false);
        }
    };

    // Initial load - Fetch metrics
    useEffect(() => {
        fetchMetrics();
    }, []);

    // Fetch products based on search/filter/pagination
    useEffect(() => {
        if (searchQuery) {
            searchProducts();
        } else if (activeFilterCategory) {
            filterProducts();
        } else {
            fetchAllProducts();
        }
    }, [currentPage, searchQuery, activeFilterCategory]);

    // Handle search
    const handleSearch = (e) => {
        e.preventDefault();
        if (searchText.trim()) {
            setCurrentPage(0);
            setActiveFilterCategory('');
            setFilterCategory('');
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
        setCurrentPage(0);
        setSearchQuery('');
        setSearchText('');
        setShowFilters(false);
        setActiveFilterCategory(filterCategory);
    };

    // Clear filter
    const handleClearFilter = () => {
        setFilterCategory('');
        setCurrentPage(0);
        setShowFilters(false);
        setActiveFilterCategory('');
    };

    // Handle page change
    const handlePageChange = (newPage) => {
        setCurrentPage(newPage);
    };

    // Download report
    const handleDownloadReport = async () => {
        try {
            await lowStockService.downloadReport();
        } catch (err) {
            console.error('Error downloading report:', err);
            setError(err.message || 'Failed to download report');
        }
    };

    // Get stock status
    const getStockStatus = (currentStock, minLevel) => {
        const percentage = (currentStock / minLevel) * 100;
        if (percentage <= 25) return { label: 'Critical', class: 'status-critical', color: '#f44336' };
        if (percentage <= 50) return { label: 'Warning', class: 'status-warning', color: '#ff9800' };
        return { label: 'Low', class: 'status-low', color: '#ffc107' };
    };

    // Get stock percentage
    const getStockPercentage = (currentStock, minLevel) => {
        return Math.min(((currentStock / minLevel) * 100).toFixed(1), 100);
    };

    // Get category color
    const getCategoryColor = (category) => {
        return categoryColors[category] || '#757575';
    };

    // Format date
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    // Generate page numbers
    const generatePageNumbers = () => {
        const totalPages = products?.totalPages || 0;
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

    if (isLoadingMetrics || (isLoadingProducts && !products)) {
        return (
            <div className="loading-container">
                <div className="spinner-large"></div>
                <p>Loading low stock data...</p>
            </div>
        );
    }

    if (error && !products) {
        return (
            <div className="error-container">
                <svg className="error-icon-large" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                    <path d="M12 8V12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                </svg>
                <h2>Error Loading Data</h2>
                <p>{error}</p>
                <button className="retry-btn" onClick={fetchAllProducts}>
                    Try Again
                </button>
            </div>
        );
    }

    return (
        <div className="low-stock-page">
            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">Low Stock Alert</h1>
                    <p className="page-subtitle">Critical items requiring immediate attention</p>
                </div>
                <button className="export-btn" onClick={handleDownloadReport}>
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        <path d="M7 10L12 15L17 10" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        <path d="M12 15V3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                    Export Excel
                </button>
            </div>

            {/* Metrics Cards */}
            <div className="metrics-grid-ls">
                <div className="metric-card-ls metric-critical">
                    <div className="metric-icon-wrapper-ls">
                        <svg className="metric-icon-ls" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M10.29 3.86L1.82 18C1.64537 18.3024 1.55299 18.6453 1.55201 18.9945C1.55103 19.3437 1.64149 19.6871 1.81442 19.9905C1.98735 20.2939 2.23672 20.5467 2.53771 20.7239C2.8387 20.901 3.18089 20.9962 3.53 21H20.47C20.8191 20.9962 21.1613 20.901 21.4623 20.7239C21.7633 20.5467 22.0127 20.2939 22.1856 19.9905C22.3585 19.6871 22.449 19.3437 22.448 18.9945C22.447 18.6453 22.3546 18.3024 22.18 18L13.71 3.86C13.5317 3.56611 13.2807 3.32312 12.9812 3.15448C12.6817 2.98585 12.3437 2.89725 12 2.89725C11.6563 2.89725 11.3183 2.98585 11.0188 3.15448C10.7193 3.32312 10.4683 3.56611 10.29 3.86Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M12 9V13" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            <circle cx="12" cy="17" r="1" fill="currentColor"/>
                        </svg>
                    </div>
                    <div className="metric-content-ls">
                        <p className="metric-label-ls">Critical Items</p>
                        <h2 className="metric-value-ls">{metrics?.criticalLowStockItems || 0}</h2>
                        <p className="metric-sublabel-ls">≤ 25% of min level</p>
                    </div>
                </div>

                <div className="metric-card-ls metric-total">
                    <div className="metric-icon-wrapper-ls">
                        <svg className="metric-icon-ls" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M20 7H4C2.9 7 2 7.9 2 9V19C2 20.1 2.9 21 4 21H20C21.1 21 22 20.1 22 19V9C22 7.9 21.1 7 20 7Z" stroke="currentColor" strokeWidth="2"/>
                            <path d="M16 7V5C16 3.9 15.1 3 14 3H10C8.9 3 8 3.9 8 5V7" stroke="currentColor" strokeWidth="2"/>
                        </svg>
                    </div>
                    <div className="metric-content-ls">
                        <p className="metric-label-ls">Total Low Stock</p>
                        <h2 className="metric-value-ls">{metrics?.totalLowStockItems || 0}</h2>
                        <p className="metric-sublabel-ls">Items below min level</p>
                    </div>
                </div>

                <div className="metric-card-ls metric-average">
                    <div className="metric-icon-wrapper-ls">
                        <svg className="metric-icon-ls" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M18 20V10" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M12 20V4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M6 20V14" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-ls">
                        <p className="metric-label-ls">Average Stock Level</p>
                        <h2 className="metric-value-ls">{metrics?.averageLowStockLevel?.toFixed(1) || 0}%</h2>
                        <p className="metric-sublabel-ls">Of minimum level</p>
                    </div>
                </div>
            </div>

            {/* Products Table Section */}
            <div className="products-section">
                {/* Search and Filter */}
                <div className="section-header-combined">
                    <div className="section-title-wrapper">
                        <h2 className="section-title">Low Stock Items</h2>
                        <p className="section-subtitle">
                            {products?.totalElements || 0} items need attention
                        </p>
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
                                    placeholder="Search by SKU, product name, location..."
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
                            className={`filter-btn ${activeFilterCategory ? 'active' :  ''}`}
                            onClick={() => setShowFilters(!showFilters)}
                        >
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M22 3H2L10 12.46V19L14 21V12.46L22 3Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                            Filter
                            {activeFilterCategory && <span className="filter-count">1</span>}
                        </button>
                    </div>
                </div>

                {/* Filter Panel */}
                {showFilters && (
                    <div className="filter-panel">
                        <div className="filter-grid-single">
                            <div className="filter-group">
                                <label className="filter-label">Product Category</label>
                                <select
                                    className="filter-select"
                                    value={filterCategory}
                                    onChange={(e) => setFilterCategory(e.target.value)}
                                >
                                    <option value="">All Categories</option>
                                    {productCategories.map(cat => (
                                        <option key={cat.value} value={cat.value}>
                                            {cat.label}
                                        </option>
                                    ))}
                                </select>
                            </div>
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
                {isLoadingProducts && products && (
                    <div className="table-loading">
                        <div className="spinner-small"></div>
                        <span>Loading...</span>
                    </div>
                )}

                {/* Products Table */}
                {!isLoadingProducts && products?.content?.length > 0 ?  (
                    <>
                        <div className="table-container">
                            <table className="products-table">
                                <thead>
                                <tr>
                                    <th>Product</th>
                                    <th>SKU</th>
                                    <th>Category</th>
                                    <th>Location</th>
                                    <th>Stock Level</th>
                                    <th>Available</th>
                                    <th>Status</th>
                                    <th>Last Update</th>
                                </tr>
                                </thead>
                                <tbody>
                                {products.content.map((product) => {
                                    const status = getStockStatus(product.currentStock, product.minLevel);
                                    const percentage = getStockPercentage(product.currentStock, product.minLevel);

                                    return (
                                        <tr key={product.sku}>
                                            <td>
                                                <div className="product-info">
                                                    <span className="product-name-ls">{product.productName}</span>
                                                    <span className="product-id-ls">ID: {product.productId}</span>
                                                </div>
                                            </td>
                                            <td className="sku-cell-ls">{product.sku}</td>
                                            <td>
                          <span
                              className="category-badge-ls"
                              style={{
                                  backgroundColor: `${getCategoryColor(product.category)}20`,
                                  color: getCategoryColor(product.category),
                                  borderLeft: `3px solid ${getCategoryColor(product.category)}`
                              }}
                          >
                            {product.category.replace(/_/g, ' ')}
                          </span>
                                            </td>
                                            <td className="location-cell">{product.location}</td>
                                            <td>
                                                <div className="stock-level">
                                                    <div className="stock-text">
                                                        <span className="current-stock">{product.currentStock}</span>
                                                        <span className="stock-separator">/</span>
                                                        <span className="min-level">{product.minLevel}</span>
                                                    </div>
                                                    <div className="progress-bar">
                                                        <div
                                                            className="progress-fill"
                                                            style={{
                                                                width: `${percentage}%`,
                                                                backgroundColor: status.color
                                                            }}
                                                        ></div>
                                                    </div>
                                                    <span className="percentage-text">{percentage}%</span>
                                                </div>
                                            </td>
                                            <td className="available-stock">
                                                {product.availableStock}
                                                {product.reservedStock > 0 && (
                                                    <span className="reserved-hint">({product.reservedStock} reserved)</span>
                                                )}
                                            </td>
                                            <td>
                          <span className={`status-badge-ls ${status.class}`}>
                            {status.label}
                          </span>
                                            </td>
                                            <td className="date-cell">{formatDate(product.lastUpdate)}</td>
                                        </tr>
                                    );
                                })}
                                </tbody>
                            </table>
                        </div>

                        {/* Pagination */}
                        {products.totalPages > 1 && (
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

                                {currentPage < products.totalPages - 3 && (
                                    <>
                                        {currentPage < products.totalPages - 4 && (
                                            <span className="pagination-ellipsis">...</span>
                                        )}
                                        <button
                                            className="pagination-btn pagination-number"
                                            onClick={() => handlePageChange(products.totalPages - 1)}
                                        >
                                            {products.totalPages}
                                        </button>
                                    </>
                                )}

                                <button
                                    className="pagination-btn pagination-arrow"
                                    onClick={() => handlePageChange(currentPage + 1)}
                                    disabled={currentPage === products.totalPages - 1}
                                >
                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M9 18L15 12L9 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                </button>

                                <div className="pagination-info">
                                    Page {currentPage + 1} of {products.totalPages}
                                </div>
                            </div>
                        )}
                    </>
                ) : !isLoadingProducts && (
                    <div className="empty-state">
                        <svg className="empty-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 11L12 14L22 4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                        <h3>No Low Stock Items</h3>
                        <p>All products are sufficiently stocked! </p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default LowStock;