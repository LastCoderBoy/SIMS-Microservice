import { useState, useEffect } from 'react';
import productService from '../../services/productService';
import authService from '../../services/userManagement/authService';
import { PRODUCT_CATEGORIES, PRODUCT_STATUS, getCategoryLabel, getStatusLabel, getStatusColor, getCategoryColor } from '../../constants/productConstants';
import Toast from '../../components/common/Toast';
import './ProductManagement.css';

const ProductManagement = () => {
    const currentUser = authService.getCurrentUser();
    const isAdminOrManager = currentUser?.role === 'ROLE_ADMIN' || currentUser?.role === 'ROLE_MANAGER';
    const isAdmin = currentUser?.role === 'ROLE_ADMIN';

    const [products, setProducts] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [toast, setToast] = useState(null);

    // Pagination (10 per page default)
    const [currentPage, setCurrentPage] = useState(0);

    // Search
    const [searchText, setSearchText] = useState('');
    const [searchQuery, setSearchQuery] = useState('');

    // Filter
    const [showFilters, setShowFilters] = useState(false);
    const [filterType, setFilterType] = useState('');
    const [filterValue, setFilterValue] = useState('');
    const [activeFilter, setActiveFilter] = useState({ type: '', value: '' });

    // Modals
    const [showAddModal, setShowAddModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [selectedProduct, setSelectedProduct] = useState(null);

    // Form data
    const [formData, setFormData] = useState({
        name: '',
        location: '',
        category: '',
        price: '',
        status: 'ACTIVE',
    });
    const [formErrors, setFormErrors] = useState({});
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Fetch all products
    const fetchAllProducts = async () => {
        setIsLoading(true);
        setError(null);

        try {
            const params = {};
            if (currentPage > 0) params.page = currentPage;

            const data = await productService.getAllProducts(params);
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
            const params = {
                text: searchQuery,
                page: currentPage,
                size: 10,
                sortBy: 'productId',
                sortDirection: 'asc',
            };

            const data = await productService.searchProducts(params);
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
            const filter = `${activeFilter.type}:${activeFilter.value}`;
            const params = {
                filter,
                page: currentPage,
                size: 10,
                sortBy: 'productId',
                direction: 'asc',
            };

            const data = await productService.filterProducts(params);
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
        if (!filterValue) {
            showToast('Please select a filter value', 'error');
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

    // Validate form
    const validateForm = () => {
        const errors = {};

        if (!formData.name || formData.name.trim().length < 2) {
            errors.name = 'Product name must be at least 2 characters';
        }

        if (!formData.location) {
            errors.location = 'Location is required';
        } else if (!/^[A-Za-z]\d{1,2}-\d{3}$/.test(formData.location)) {
            errors.location = 'Location must follow format:  A1-123';
        }

        if (!formData.category) {
            errors.category = 'Category is required';
        }

        if (!formData.price) {
            errors.price = 'Price is required';
        } else if (isNaN(formData.price) || parseFloat(formData.price) <= 0) {
            errors.price = 'Price must be greater than 0';
        } else if (parseFloat(formData.price) > 99999.99) {
            errors.price = 'Price cannot exceed 99999.99';
        }

        if (!formData.status) {
            errors.status = 'Status is required';
        }

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // Handle add product
    const handleAddProduct = async (e) => {
        e.preventDefault();

        if (!validateForm()) return;

        setIsSubmitting(true);

        try {
            const productData = {
                name: formData.name.trim(),
                location: formData.location.trim().toUpperCase(),
                category:  formData.category,
                price: parseFloat(formData.price),
                status: formData.status,
            };

            const response = await productService.addProduct(productData);

            if (response.success) {
                showToast('Product added successfully! ', 'success');
                setShowAddModal(false);
                resetForm();
                fetchAllProducts();
            }
        } catch (err) {
            console.error('Error adding product:', err);
            showToast(err.message || 'Failed to add product', 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    // Handle edit product
    const handleEditProduct = async (e) => {
        e.preventDefault();

        if (!validateForm()) return;

        setIsSubmitting(true);

        try {
            const productData = {
                name: formData.name.trim(),
                location: formData.location.trim().toUpperCase(),
                category: formData.category,
                price: parseFloat(formData.price),
                status: formData.status,
            };

            const response = await productService.updateProduct(selectedProduct.productId, productData);

            if (response.success) {
                showToast('Product updated successfully! ', 'success');
                setShowEditModal(false);
                resetForm();
                fetchAllProducts();
            }
        } catch (err) {
            console.error('Error updating product:', err);
            showToast(err.message || 'Failed to update product', 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    // Handle delete product
    const handleDeleteProduct = async () => {
        setIsSubmitting(true);

        try {
            const response = await productService.deleteProduct(selectedProduct.productId);

            if (response.success) {
                showToast('Product and associated inventory deleted successfully!', 'success');
                setShowDeleteModal(false);
                setSelectedProduct(null);
                fetchAllProducts();
            }
        } catch (err) {
            console.error('Error deleting product:', err);
            showToast(err.message || 'Failed to delete product', 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    // Open add modal
    const openAddModal = () => {
        resetForm();
        setShowAddModal(true);
    };

    // Open edit modal
    const openEditModal = (product) => {
        setSelectedProduct(product);
        setFormData({
            name: product.name,
            location: product.location,
            category: product.category,
            price: product.price.toString(),
            status: product.status,
        });
        setShowEditModal(true);
    };

    // Open delete modal
    const openDeleteModal = (product) => {
        setSelectedProduct(product);
        setShowDeleteModal(true);
    };

    // Reset form
    const resetForm = () => {
        setFormData({
            name: '',
            location: '',
            category: '',
            price: '',
            status:  'ACTIVE',
        });
        setFormErrors({});
        setSelectedProduct(null);
    };

    // Show toast
    const showToast = (message, type = 'success') => {
        setToast({ message, type });
    };

    // Close toast
    const closeToast = () => {
        setToast(null);
    };

    // Format currency
    const formatCurrency = (value) => {
        return new Intl.NumberFormat('en-US', {
            style:  'currency',
            currency:  'USD',
            minimumFractionDigits: 2,
        }).format(value || 0);
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
                <p>Loading products...</p>
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
        <div className="product-management-page">
            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">📦 Product Management</h1>
                    <p className="page-subtitle">Manage products, categories, and pricing</p>
                </div>
                {isAdminOrManager && (
                    <button className="add-product-btn" onClick={openAddModal}>
                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 5V19M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        </svg>
                        Add Product
                    </button>
                )}
            </div>

            {/* Products Section */}
            <div className="products-section">
                {/* Search and Filter */}
                <div className="section-header-combined">
                    <div className="section-title-wrapper">
                        <h2 className="section-title">All Products</h2>
                        <p className="section-subtitle">
                            {products?.totalElements || 0} total products
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
                                    placeholder="Search by product ID, name, location..."
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
                                    <option value="price">Max Price</option>
                                </select>
                            </div>

                            {filterType === 'category' && (
                                <div className="filter-group">
                                    <label className="filter-label">Select Category</label>
                                    <select
                                        className="filter-select"
                                        value={filterValue}
                                        onChange={(e) => setFilterValue(e.target.value)}
                                    >
                                        <option value="">Select value</option>
                                        {PRODUCT_CATEGORIES.map(cat => (
                                            <option key={cat.value} value={cat.value}>
                                                {cat.label}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                            )}

                            {filterType === 'status' && (
                                <div className="filter-group">
                                    <label className="filter-label">Select Status</label>
                                    <select
                                        className="filter-select"
                                        value={filterValue}
                                        onChange={(e) => setFilterValue(e.target.value)}
                                    >
                                        <option value="">Select value</option>
                                        {PRODUCT_STATUS.map(status => (
                                            <option key={status.value} value={status.value}>
                                                {status.label}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                            )}

                            {filterType === 'price' && (
                                <div className="filter-group">
                                    <label className="filter-label">Max Price</label>
                                    <input
                                        type="number"
                                        className="filter-select"
                                        placeholder="Enter max price"
                                        value={filterValue}
                                        onChange={(e) => setFilterValue(e.target.value)}
                                        step="0.01"
                                        min="0"
                                    />
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
                {! isLoading && products?.content?.length > 0 ?  (
                    <>
                        <div className="table-container">
                            <table className="products-table">
                                <thead>
                                <tr>
                                    <th>Product</th>
                                    <th>Category</th>
                                    <th>Location</th>
                                    <th>Price</th>
                                    <th>Status</th>
                                    <th>Created At</th>
                                    {(isAdminOrManager || isAdmin) && <th>Actions</th>}
                                </tr>
                                </thead>
                                <tbody>
                                {products.content.map((product) => (
                                    <tr key={product.productId}>
                                        <td>
                                            <div className="product-info">
                                                <span className="product-name-pm">{product.name}</span>
                                                <span className="product-id-pm">ID: {product.productId}</span>
                                            </div>
                                        </td>
                                        <td>
                                            <span
                                                className="category-badge-pm"
                                                style={{
                                                    backgroundColor: `${getCategoryColor(product.category)}20`,
                                                    color: getCategoryColor(product.category),
                                                    borderLeft: `3px solid ${getCategoryColor(product.category)}`
                                                }}
                                            >
                                                {getCategoryLabel(product.category)}
                                            </span>
                                        </td>
                                        <td className="location-cell">{product.location}</td>
                                        <td className="price-cell">{formatCurrency(product.price)}</td>
                                        <td>
                                            <span
                                                className="status-badge-pm"
                                                style={{
                                                    backgroundColor: `${getStatusColor(product.status)}20`,
                                                    color: getStatusColor(product.status),
                                                    border: `1px solid ${getStatusColor(product.status)}50`
                                                }}
                                            >
                                                {getStatusLabel(product.status)}
                                            </span>
                                        </td>
                                        <td className="date-cell">{formatDate(product.createdAt)}</td>
                                        {(isAdminOrManager || isAdmin) && (
                                            <td>
                                                <div className="action-buttons">
                                                    {isAdminOrManager && (
                                                        <button
                                                            className="action-btn edit-btn"
                                                            onClick={() => openEditModal(product)}
                                                            title="Edit Product"
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
                                                            onClick={() => openDeleteModal(product)}
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
                            <rect x="3" y="3" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                            <rect x="14" y="3" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                            <rect x="14" y="14" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                            <rect x="3" y="14" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                        </svg>
                        <h3>No Products Found</h3>
                        <p>
                            {searchQuery || activeFilter.value
                                ? 'Try adjusting your search or filter criteria'
                                : 'Get started by adding your first product'}
                        </p>
                        {isAdminOrManager && ! searchQuery && ! activeFilter.value && (
                            <button className="btn-add-first" onClick={openAddModal}>
                                Add Product
                            </button>
                        )}
                    </div>
                )}
            </div>

            {/* Add/Edit Product Modal */}
            {(showAddModal || showEditModal) && (
                <div className="modal-overlay" onClick={() => {
                    setShowAddModal(false);
                    setShowEditModal(false);
                    resetForm();
                }}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>{showAddModal ? 'Add New Product' : 'Edit Product'}</h2>
                            <button
                                className="modal-close-btn"
                                onClick={() => {
                                    setShowAddModal(false);
                                    setShowEditModal(false);
                                    resetForm();
                                }}
                            >
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <form onSubmit={showAddModal ? handleAddProduct : handleEditProduct}>
                            <div className="modal-body">
                                {showEditModal && selectedProduct && (
                                    <div className="product-display">
                                        <h3>{selectedProduct.name}</h3>
                                        <p className="product-id-display">ID: {selectedProduct.productId}</p>
                                    </div>
                                )}

                                {/* Product Name */}
                                <div className="form-group">
                                    <label className="form-label">Product Name *</label>
                                    <input
                                        type="text"
                                        className={`form-input ${formErrors.name ? 'input-error' : ''}`}
                                        placeholder="Enter product name"
                                        value={formData.name}
                                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                        disabled={isSubmitting}
                                    />
                                    {formErrors.name && <span className="error-text">{formErrors.name}</span>}
                                </div>

                                {/* Location */}
                                <div className="form-group">
                                    <label className="form-label">Location * (Format: A1-123)</label>
                                    <input
                                        type="text"
                                        className={`form-input ${formErrors.location ? 'input-error' : ''}`}
                                        placeholder="e.g., A1-123"
                                        value={formData.location}
                                        onChange={(e) =>
                                            setFormData({ ...formData, location: e.target.value.toUpperCase() })}
                                        disabled={isSubmitting}
                                    />
                                    {formErrors.location && <span className="error-text">{formErrors.location}</span>}
                                </div>

                                {/* Category */}
                                <div className="form-group">
                                    <label className="form-label">Category *</label>
                                    <select
                                        className={`form-input ${formErrors.category ? 'input-error' : ''}`}
                                        value={formData.category}
                                        onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                                        disabled={isSubmitting}
                                    >
                                        <option value="">Select Category</option>
                                        {PRODUCT_CATEGORIES.map(cat => (
                                            <option key={cat.value} value={cat.value}>
                                                {cat.label} - {cat.description}
                                            </option>
                                        ))}
                                    </select>
                                    {formErrors.category && <span className="error-text">{formErrors.category}</span>}
                                </div>

                                {/* Price */}
                                <div className="form-group">
                                    <label className="form-label">Price * (USD)</label>
                                    <input
                                        type="number"
                                        className={`form-input ${formErrors.price ? 'input-error' :  ''}`}
                                        placeholder="0.00"
                                        value={formData.price}
                                        onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                                        step="0.01"
                                        min="0"
                                        max="99999.99"
                                        disabled={isSubmitting}
                                    />
                                    {formErrors.price && <span className="error-text">{formErrors.price}</span>}
                                </div>

                                {/* Status */}
                                <div className="form-group">
                                    <label className="form-label">Status *</label>
                                    <select
                                        className={`form-input ${formErrors.status ? 'input-error' : ''}`}
                                        value={formData.status}
                                        onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                                        disabled={isSubmitting}
                                    >
                                        {PRODUCT_STATUS.map(status => (
                                            <option key={status.value} value={status.value}>
                                                {status.label}
                                            </option>
                                        ))}
                                    </select>
                                    {formErrors.status && <span className="error-text">{formErrors.status}</span>}
                                </div>
                            </div>

                            <div className="modal-footer">
                                <button
                                    type="button"
                                    className="btn-secondary"
                                    onClick={() => {
                                        setShowAddModal(false);
                                        setShowEditModal(false);
                                        resetForm();
                                    }}
                                    disabled={isSubmitting}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="btn-primary"
                                    disabled={isSubmitting}
                                >
                                    {isSubmitting ? (
                                        <>
                                            <div className="spinner-small"></div>
                                            {showAddModal ?  'Adding...' : 'Updating...'}
                                        </>
                                    ) : (
                                        <>
                                            {showAddModal ? 'Add Product' : 'Update Product'}
                                        </>
                                    )}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Delete Confirmation Modal */}
            {showDeleteModal && selectedProduct && (
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
                                <h3>{selectedProduct.name}</h3>
                                <p className="product-id-display">ID: {selectedProduct.productId}</p>
                            </div>
                            <p className="warning-text">
                                ⚠️ Warning: This will also delete all associated inventory records for this product!
                            </p>
                        </div>

                        <div className="modal-footer">
                            <button
                                type="button"
                                className="btn-secondary"
                                onClick={() => setShowDeleteModal(false)}
                                disabled={isSubmitting}
                            >
                                Cancel
                            </button>
                            <button
                                type="button"
                                className="btn-danger"
                                onClick={handleDeleteProduct}
                                disabled={isSubmitting}
                            >
                                {isSubmitting ? (
                                    <>
                                        <div className="spinner-small"></div>
                                        Deleting...
                                    </>
                                ) : (
                                    'Delete Product'
                                )}
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

export default ProductManagement;