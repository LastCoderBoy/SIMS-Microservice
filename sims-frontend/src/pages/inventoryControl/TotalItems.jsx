import { useState, useEffect } from 'react';
import totalItemsService from '../../services/inventoryControl/totalItemsService.js';
import authService from '../../services/userManagement/authService.js';
import './TotalItems.css';

const TotalItems = () => {
    const currentUser = authService.getCurrentUser();
    const isAdminOrManager = currentUser?.role === 'ROLE_ADMIN' || currentUser?.role === 'ROLE_MANAGER';
    const isAdmin = currentUser?.role === 'ROLE_ADMIN';

    const [products, setProducts] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // Pagination
    const [currentPage, setCurrentPage] = useState(0);

    // Search
    const [searchText, setSearchText] = useState('');
    const [searchQuery, setSearchQuery] = useState('');

    // Filter
    const [showFilters, setShowFilters] = useState(false);
    const [filterType, setFilterType] = useState(''); // 'category' or 'status'
    const [filterValue, setFilterValue] = useState('');
    const [activeFilter, setActiveFilter] = useState({ type: '', value: '' });

    // Modals
    const [showEditModal, setShowEditModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [selectedProduct, setSelectedProduct] = useState(null);

    // Form data
    const [formData, setFormData] = useState({
        currentStock: '',
        minLevel:  '',
    });
    const [formErrors, setFormErrors] = useState({});

    // Product Categories
    const productCategories = [
        { value: 'EDUCATION', label:  'Educational Toy' },
        { value: 'ELECTRONIC', label: 'Electronic Toy' },
        { value: 'ACTION_FIGURES', label:  'Action Figure' },
        { value: 'DOLLS', label: 'Dolls' },
        { value: 'MUSICAL_TOY', label: 'Musical Toy' },
        { value: 'OUTDOOR_TOY', label: 'Outdoor Toy' },
    ];

    // Inventory Statuses
    const inventoryStatuses = [
        { value:  'INCOMING', label: 'Incoming' },
        { value: 'IN_STOCK', label: 'In Stock' },
        { value: 'LOW_STOCK', label: 'Low Stock' },
        { value: 'INVALID', label: 'Invalid' },
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

    // Status colors
    const statusColors = {
        INCOMING: '#2196F3',
        IN_STOCK: '#4CAF50',
        LOW_STOCK:  '#FF9800',
        INVALID:  '#F44336',
    };

    // Fetch all products
    const fetchAllProducts = async () => {
        setIsLoading(true);
        setError(null);

        try {
            const params = {};
            if (currentPage > 0) params.page = currentPage;

            const data = await totalItemsService.getAllProducts(params);
            setProducts(data);
        } catch (err) {
            console.error('Error fetching products:', err);
            setError(err.message || 'Failed to load products');
        } finally {
            setIsLoading(false);
        }
    };

    // Search products
    const searchProducts = async () => {
        setIsLoading(true);
        setError(null);

        try {
            const params = { text: searchQuery };
            if (currentPage > 0) params.page = currentPage;

            const data = await totalItemsService.searchProducts(params);
            setProducts(data);
        } catch (err) {
            console.error('Error searching products:', err);
            setError(err.message || 'Failed to search products');
        } finally {
            setIsLoading(false);
        }
    };

    // Filter products
    const filterProducts = async () => {
        setIsLoading(true);
        setError(null);

        try {
            const params = { filterBy: activeFilter.value };
            if (currentPage > 0) params.page = currentPage;

            const data = await totalItemsService.filterProducts(params);
            setProducts(data);
        } catch (err) {
            console.error('Error filtering products:', err);
            setError(err.message || 'Failed to filter products');
        } finally {
            setIsLoading(false);
        }
    };

    // useEffect for data fetching
    useEffect(() => {
        if (searchQuery) {
            searchProducts();
        } else if (activeFilter.value) {
            filterProducts();
        } else {
            fetchAllProducts();
        }
    }, [currentPage, searchQuery, activeFilter]);

    // Handle search
    const handleSearch = (e) => {
        e.preventDefault();
        if (searchText.trim()) {
            setCurrentPage(0);
            setActiveFilter({ type: '', value: '' });
            setFilterType('');
            setFilterValue('');
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

    // Handle page change
    const handlePageChange = (newPage) => {
        setCurrentPage(newPage);
    };

    // Open Edit Modal
    const handleOpenEditModal = (product) => {
        setSelectedProduct(product);
        setFormData({
            currentStock: product.currentStock,
            minLevel: product.minLevel,
        });
        setFormErrors({});
        setShowEditModal(true);
    };

    // Open Delete Modal
    const handleOpenDeleteModal = (product) => {
        setSelectedProduct(product);
        setShowDeleteModal(true);
    };

    // Validate form
    const validateForm = () => {
        const errors = {};

        if (formData.currentStock === '' || formData.currentStock < 0) {
            errors.currentStock = 'Current stock must be 0 or greater';
        }

        if (formData.minLevel === '' || formData.minLevel < 0) {
            errors.minLevel = 'Min level must be 0 or greater';
        }

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // Handle Update Product
    const handleUpdateProduct = async (e) => {
        e.preventDefault();

        if (!validateForm()) return;

        try {
            const updateData = {
                currentStock:  parseInt(formData.currentStock),
                minLevel: parseInt(formData.minLevel),
            };

            await totalItemsService.updateProduct(selectedProduct.sku, updateData);
            setShowEditModal(false);
            fetchAllProducts();
        } catch (err) {
            console.error('Error updating product:', err);
            setFormErrors({ submit: err.message || 'Failed to update product' });
        }
    };

    // Handle Delete Product
    const handleDeleteProduct = async () => {
        try {
            await totalItemsService.deleteProduct(selectedProduct.sku);
            setShowDeleteModal(false);
            fetchAllProducts();
        } catch (err) {
            console.error('Error deleting product:', err);
            setError(err.message || 'Failed to delete product');
        }
    };

    // Download report
    const handleDownloadReport = async () => {
        try {
            await totalItemsService.downloadReport();
        } catch (err) {
            console.error('Error downloading report:', err);
            setError(err.message || 'Failed to download report');
        }
    };

    // Get category color
    const getCategoryColor = (category) => {
        return categoryColors[category] || '#757575';
    };

    // Get status color
    const getStatusColor = (status) => {
        return statusColors[status] || '#757575';
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

    if (isLoading && !products) {
        return (
            <div className="loading-container">
                <div className="spinner-large"></div>
                <p>Loading inventory...</p>
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
        <div className="total-items-page">
            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">Total Inventory Items</h1>
                    <p className="page-subtitle">Manage all products in your inventory</p>
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

            {/* Products Table Section */}
            <div className="products-section">
                {/* Search and Filter */}
                <div className="section-header-combined">
                    <div className="section-title-wrapper">
                        <h2 className="section-title">All Products</h2>
                        <p className="section-subtitle">
                            {products?.totalElements || 0} total items
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
                            className={`filter-btn ${activeFilter.value ?  'active' : ''}`}
                            onClick={() => setShowFilters(! showFilters)}
                        >
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M22 3H2L10 12.46V19L14 21V12.46L22 3Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                            Filter
                            {activeFilter.value && <span className="filter-count">1</span>}
                        </button>
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
                                    <option value="category">Category</option>
                                    <option value="status">Status</option>
                                </select>
                            </div>

                            {filterType && (
                                <div className="filter-group">
                                    <label className="filter-label">
                                        {filterType === 'category' ?  'Select Category' : 'Select Status'}
                                    </label>
                                    <select
                                        className="filter-select"
                                        value={filterValue}
                                        onChange={(e) => setFilterValue(e.target.value)}
                                    >
                                        <option value="">Select value</option>
                                        {filterType === 'category' && productCategories.map(cat => (
                                            <option key={cat.value} value={cat.value}>
                                                {cat.label}
                                            </option>
                                        ))}
                                        {filterType === 'status' && inventoryStatuses.map(status => (
                                            <option key={status.value} value={status.value}>
                                                {status.label}
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
                {isLoading && products && (
                    <div className="table-loading">
                        <div className="spinner-small"></div>
                        <span>Loading...</span>
                    </div>
                )}

                {/* Products Table */}
                {!isLoading && products?.content?.length > 0 ?  (
                    <>
                        <div className="table-container">
                            <table className="products-table">
                                <thead>
                                <tr>
                                    <th>Product</th>
                                    <th>SKU</th>
                                    <th>Category</th>
                                    <th>Location</th>
                                    <th>Current Stock</th>
                                    <th>Min Level</th>
                                    <th>Available</th>
                                    <th>Status</th>
                                    <th>Price</th>
                                    {(isAdminOrManager || isAdmin) && <th>Actions</th>}
                                </tr>
                                </thead>
                                <tbody>
                                {products.content.map((product) => (
                                    <tr key={product.sku}>
                                        <td>
                                            <div className="product-info">
                                                <span className="product-name-ti">{product.productName}</span>
                                                <span className="product-id-ti">ID: {product.productId}</span>
                                            </div>
                                        </td>
                                        <td className="sku-cell-ti">{product.sku}</td>
                                        <td>
                        <span
                            className="category-badge-ti"
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
                                        <td className="stock-cell">{product.currentStock}</td>
                                        <td className="min-level-cell">{product.minLevel}</td>
                                        <td className="available-cell">
                                            {product.availableStock}
                                            {product.reservedStock > 0 && (
                                                <span className="reserved-hint">({product.reservedStock} reserved)</span>
                                            )}
                                        </td>
                                        <td>
                        <span
                            className="status-badge-ti"
                            style={{
                                backgroundColor:  `${getStatusColor(product.inventoryStatus)}20`,
                                color: getStatusColor(product.inventoryStatus),
                                border: `1px solid ${getStatusColor(product.inventoryStatus)}50`
                            }}
                        >
                          {product.inventoryStatus.replace(/_/g, ' ')}
                        </span>
                                        </td>
                                        <td className="price-cell">${product.price.toFixed(2)}</td>
                                        {(isAdminOrManager || isAdmin) && (
                                            <td>
                                                <div className="action-buttons">
                                                    {isAdminOrManager && (
                                                        <button
                                                            className="action-btn edit-btn"
                                                            onClick={() => handleOpenEditModal(product)}
                                                            title="Update Stock Levels"
                                                        >
                                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                <path d="M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                <path d="M18.5 2.50023C18.8978 2.1024 19.4374 1.87891 20 1.87891C20.5626 1.87891 21.1022 2.1024 21.5 2.50023C21.8978 2.89805 22.1213 3.43762 22.1213 4.00023C22.1213 4.56284 21.8978 5.1024 21.5 5.50023L12 15.0002L8 16.0002L9 12.0002L18.5 2.50023Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                            </svg>
                                                        </button>
                                                    )}
                                                    {isAdmin && (
                                                        <button
                                                            className="action-btn delete-btn"
                                                            onClick={() => handleOpenDeleteModal(product)}
                                                            title="Delete Product"
                                                        >
                                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                <path d="M3 6H5H21" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                <path d="M8 6V4C8 3.46957 8.21071 2.96086 8.58579 2.58579C8.96086 2.21071 9.46957 2 10 2H14C14.5304 2 15.0391 2.21071 15.4142 2.58579C15.7893 2.96086 16 3.46957 16 4V6M19 6V20C19 20.5304 18.7893 21.0391 18.4142 21.4142C18.0391 21.7893 17.5304 22 17 22H7C6.46957 22 5.96086 21.7893 5.58579 21.4142C5.21071 21.0391 5 20.5304 5 20V6H19Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                            </svg>
                                                        </button>
                                                    )}
                                                </div>
                                            </td>
                                        )}
                                    </tr>
                                ))}
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
                ) : !isLoading && (
                    <div className="empty-state">
                        <svg className="empty-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M20 7H4C2.9 7 2 7.9 2 9V19C2 20.1 2.9 21 4 21H20C21.1 21 22 20.1 22 19V9C22 7.9 21.1 7 20 7Z" stroke="currentColor" strokeWidth="2"/>
                            <path d="M16 7V5C16 3.9 15.1 3 14 3H10C8.9 3 8 3.9 8 5V7" stroke="currentColor" strokeWidth="2"/>
                        </svg>
                        <h3>No Products Found</h3>
                        <p>There are currently no products in the inventory.</p>
                    </div>
                )}
            </div>

            {/* Edit Modal */}
            {showEditModal && (
                <div className="modal-overlay" onClick={() => setShowEditModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Update Stock Levels</h2>
                            <button className="modal-close-btn" onClick={() => setShowEditModal(false)}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <form onSubmit={handleUpdateProduct}>
                            <div className="modal-body">
                                {formErrors.submit && (
                                    <div className="error-alert">
                                        {formErrors.submit}
                                    </div>
                                )}

                                {/* Product Info Display */}
                                <div className="product-display">
                                    <h3>{selectedProduct?.productName}</h3>
                                    <p className="sku-display">SKU: {selectedProduct?.sku}</p>
                                </div>

                                <div className="form-group">
                                    <label className="form-label">Current Stock *</label>
                                    <input
                                        type="number"
                                        min="0"
                                        className={`form-input ${formErrors.currentStock ? 'input-error' : ''}`}
                                        placeholder="Enter current stock"
                                        value={formData.currentStock}
                                        onChange={(e) => setFormData({ ...formData, currentStock: e.target.value })}
                                    />
                                    {formErrors.currentStock && <span className="error-text">{formErrors.currentStock}</span>}
                                </div>

                                <div className="form-group">
                                    <label className="form-label">Minimum Level *</label>
                                    <input
                                        type="number"
                                        min="0"
                                        className={`form-input ${formErrors.minLevel ? 'input-error' :  ''}`}
                                        placeholder="Enter minimum level"
                                        value={formData.minLevel}
                                        onChange={(e) => setFormData({ ...formData, minLevel: e.target.value })}
                                    />
                                    {formErrors.minLevel && <span className="error-text">{formErrors.minLevel}</span>}
                                </div>
                            </div>

                            <div className="modal-footer">
                                <button type="button" className="btn-secondary" onClick={() => setShowEditModal(false)}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn-primary">
                                    Update Stock
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Delete Confirmation Modal */}
            {showDeleteModal && (
                <div className="modal-overlay" onClick={() => setShowDeleteModal(false)}>
                    <div className="modal-content modal-small" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Delete Product</h2>
                            <button className="modal-close-btn" onClick={() => setShowDeleteModal(false)}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <div className="modal-body">
                            <p>Are you sure you want to delete this product?</p>
                            <div className="product-display">
                                <h3>{selectedProduct?.productName}</h3>
                                <p className="sku-display">SKU: {selectedProduct?.sku}</p>
                            </div>
                            <p className="warning-text">This action will remove the product from inventory and archive it.</p>
                        </div>

                        <div className="modal-footer">
                            <button type="button" className="btn-secondary" onClick={() => setShowDeleteModal(false)}>
                                Cancel
                            </button>
                            <button type="button" className="btn-danger" onClick={handleDeleteProduct}>
                                Delete Product
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default TotalItems;