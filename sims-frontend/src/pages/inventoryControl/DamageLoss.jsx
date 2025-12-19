import { useState, useEffect } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import damageLossService from '../../services/inventoryControl/damageLossService.js';
import authService from '../../services/userManagement/authService.js';
import './DamageLoss.css';

const DamageLoss = () => {
    const currentUser = authService.getCurrentUser();
    const isAdminOrManager = currentUser?.role === 'ROLE_ADMIN' || currentUser?.role === 'ROLE_MANAGER';
    const isAdmin = currentUser?.role === 'ROLE_ADMIN';

    const [dashboardData, setDashboardData] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // Pagination
    const [currentPage, setCurrentPage] = useState(0);

    // Search
    const [searchText, setSearchText] = useState('');
    const [searchQuery, setSearchQuery] = useState('');

    // Filter
    const [showFilters, setShowFilters] = useState(false);
    const [filterReason, setFilterReason] = useState('');
    const [activeFilterReason, setActiveFilterReason] = useState('');

    // Modals
    const [showAddModal, setShowAddModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [selectedReport, setSelectedReport] = useState(null);

    // Form data
    const [formData, setFormData] = useState({
        sku: '',
        quantityLost: '',
        reason:  '',
        lossDate: '',
    });
    const [formErrors, setFormErrors] = useState({});

    // Loss Reasons from backend enum
    const lossReasons = [
        { value: 'LOST', label: 'Lost', description: 'Item was lost or misplaced' },
        { value: 'DAMAGED', label:  'Damaged', description: 'Item was damaged beyond repair' },
        { value: 'SUPPLIER_FAULT', label: 'Supplier Fault', description:  'Received damaged from supplier' },
        { value: 'TRANSPORT_DAMAGE', label:  'Transport Damage', description:  'Damaged during transportation' },
    ];

    // Chart colors
    const COLORS = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A'];

    const getReasonColor = (reason) => {
        const reasonIndex = lossReasons.findIndex(r => r.value === reason);
        return reasonIndex !== -1 ? COLORS[reasonIndex % COLORS.length] :  COLORS[0];
    };

    // Fetch dashboard data
    const fetchDashboardData = async () => {
        setIsLoading(true);
        setError(null);

        try {
            const data = await damageLossService.getDashboardData(currentPage > 0 ? currentPage : undefined);
            setDashboardData(data);
        } catch (err) {
            console.error('Error fetching dashboard:', err);
            setError(err.message || 'Failed to load data');
        } finally {
            setIsLoading(false);
        }
    };

    // Search reports
    const searchReports = async () => {
        setIsLoading(true);
        setError(null);

        try {
            const data = await damageLossService.searchReports(
                searchQuery,
                currentPage > 0 ? currentPage : undefined
            );
            setDashboardData(prev => ({
                ...prev,
                reports: data,
            }));
        } catch (err) {
            console.error('Error searching:', err);
            setError(err.message || 'Failed to search');
        } finally {
            setIsLoading(false);
        }
    };

    // Filter reports
    const filterReports = async () => {
        setIsLoading(true);
        setError(null);

        try {
            const filterParams = { reason: activeFilterReason };
            if (currentPage > 0) filterParams.page = currentPage;

            const data = await damageLossService.filterReports(filterParams);
            setDashboardData(prev => ({
                ...prev,
                reports: data,
            }));
        } catch (err) {
            console.error('Error filtering:', err);
            setError(err.message || 'Failed to filter');
        } finally {
            setIsLoading(false);
        }
    };

    // useEffect for data fetching
    useEffect(() => {
        if (searchQuery) {
            searchReports();
        } else if (activeFilterReason) {
            filterReports();
        } else {
            fetchDashboardData();
        }
    }, [currentPage, searchQuery, activeFilterReason]);

    // Handle search
    const handleSearch = (e) => {
        e.preventDefault();
        if (searchText.trim()) {
            setCurrentPage(0);
            setActiveFilterReason('');
            setFilterReason('');
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
        setActiveFilterReason(filterReason);
    };

    // Clear filter
    const handleClearFilter = () => {
        setFilterReason('');
        setCurrentPage(0);
        setShowFilters(false);
        setActiveFilterReason('');
    };

    // Handle page change
    const handlePageChange = (newPage) => {
        setCurrentPage(newPage);
    };

    // Open Add Modal
    const handleOpenAddModal = () => {
        setFormData({
            sku: '',
            quantityLost: '',
            reason: '',
            lossDate: '',
        });
        setFormErrors({});
        setShowAddModal(true);
    };

    // Open Edit Modal
    const handleOpenEditModal = (report) => {
        setSelectedReport(report);
        setFormData({
            sku:  report.sku,
            quantityLost: report.quantityLost,
            reason: report.reason,
            lossDate: report.lossDate ?  new Date(report.lossDate).toISOString().slice(0, 16) : '',
        });
        setFormErrors({});
        setShowEditModal(true);
    };

    // Open Delete Modal
    const handleOpenDeleteModal = (report) => {
        setSelectedReport(report);
        setShowDeleteModal(true);
    };

    // Validate form
    const validateForm = () => {
        const errors = {};

        if (!formData.sku.trim()) {
            errors.sku = 'SKU is required';
        }

        if (!formData.quantityLost || formData.quantityLost < 1) {
            errors.quantityLost = 'Quantity must be at least 1';
        }

        if (!formData.reason) {
            errors.reason = 'Loss reason is required';
        }

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // Handle Add Report
    const handleAddReport = async (e) => {
        e.preventDefault();

        if (!validateForm()) return;

        try {
            const reportData = {
                sku:  formData.sku.trim(),
                quantityLost: parseInt(formData.quantityLost),
                reason: formData.reason,
            };

            if (formData.lossDate) {
                reportData.lossDate = new Date(formData.lossDate).toISOString();
            }

            await damageLossService.addReport(reportData);
            setShowAddModal(false);
            setCurrentPage(0);
            fetchDashboardData();
        } catch (err) {
            console.error('Error adding report:', err);
            setFormErrors({ submit: err.message || 'Failed to add report' });
        }
    };

    // Handle Update Report
    const handleUpdateReport = async (e) => {
        e.preventDefault();

        // Validate form (without SKU)
        const errors = {};

        if (!formData.quantityLost || formData.quantityLost < 1) {
            errors.quantityLost = 'Quantity must be at least 1';
        }

        if (!formData. reason) {
            errors.reason = 'Loss reason is required';
        }

        setFormErrors(errors);
        if (Object.keys(errors).length > 0) return;

        try {
            const reportData = {
                quantityLost: parseInt(formData.quantityLost),
                reason: formData.reason,
            };

            if (formData.lossDate) {
                reportData.lossDate = new Date(formData.lossDate).toISOString();
            }

            await damageLossService.updateReport(selectedReport.id, reportData);
            setShowEditModal(false);
            fetchDashboardData();
        } catch (err) {
            console.error('Error updating report:', err);
            setFormErrors({ submit: err.message || 'Failed to update report' });
        }
    };

    // Handle Delete Report
    const handleDeleteReport = async () => {
        try {
            await damageLossService.deleteReport(selectedReport.id);
            setShowDeleteModal(false);
            fetchDashboardData();
        } catch (err) {
            console.error('Error deleting report:', err);
            setError(err.message || 'Failed to delete report');
        }
    };

    // Download Excel Report
    const handleDownloadReport = async () => {
        try {
            await damageLossService.downloadReport();
        } catch (err) {
            console.error('Error downloading report:', err);
            setError(err.message || 'Failed to download report');
        }
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

    // Prepare chart data
    const getChartData = () => {
        if (!dashboardData?.reports?.content) return [];

        const reasonCounts = {};
        dashboardData.reports.content.forEach(report => {
            reasonCounts[report.reason] = (reasonCounts[report.reason] || 0) + 1;
        });

        return Object.entries(reasonCounts).map(([reason, count]) => ({
            name: reason.replace(/_/g, ' '),
            value: count,
        }));
    };

    // Generate page numbers
    const generatePageNumbers = () => {
        const totalPages = dashboardData?.reports?.totalPages || 0;
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
                <p>Loading damage/loss data...</p>
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
                <h2>Error Loading Data</h2>
                <p>{error}</p>
                <button className="retry-btn" onClick={fetchDashboardData}>
                    Try Again
                </button>
            </div>
        );
    }

    return (
        <div className="damage-loss-page">
            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">Damage & Loss Management</h1>
                    <p className="page-subtitle">Track and manage damaged or lost inventory items</p>
                </div>
                <div className="header-actions">
                    <button className="export-btn" onClick={handleDownloadReport}>
                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M7 10L12 15L17 10" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M12 15V3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                        Export Excel
                    </button>
                    {isAdminOrManager && (
                        <button className="add-btn" onClick={handleOpenAddModal}>
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M12 5V19M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                            Add Report
                        </button>
                    )}
                </div>
            </div>

            {/* Metrics & Chart Section */}
            <div className="metrics-chart-section">
                {/* Metrics */}
                <div className="metrics-cards">
                    <div className="metric-card-dl metric-danger">
                        <div className="metric-icon-wrapper-dl">
                            <svg className="metric-icon-dl" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M9 2H15M9 2V6M15 2V6M9 2C7.89543 2 7 2.89543 7 4V6H17V4C17 2.89543 16.1046 2 15 2Z" stroke="currentColor" strokeWidth="2"/>
                                <rect x="3" y="8" width="18" height="13" rx="2" stroke="currentColor" strokeWidth="2"/>
                            </svg>
                        </div>
                        <div className="metric-content-dl">
                            <p className="metric-label-dl">Total Reports</p>
                            <h2 className="metric-value-dl">{dashboardData?.totalReports || 0}</h2>
                        </div>
                    </div>

                    <div className="metric-card-dl metric-warning">
                        <div className="metric-icon-wrapper-dl">
                            <svg className="metric-icon-dl" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M20 7H4C2.9 7 2 7.9 2 9V19C2 20.1 2.9 21 4 21H20C21.1 21 22 20.1 22 19V9C22 7.9 21.1 7 20 7Z" stroke="currentColor" strokeWidth="2"/>
                                <path d="M16 7V5C16 3.9 15.1 3 14 3H10C8.9 3 8 3.9 8 5V7" stroke="currentColor" strokeWidth="2"/>
                            </svg>
                        </div>
                        <div className="metric-content-dl">
                            <p className="metric-label-dl">Total Items Lost</p>
                            <h2 className="metric-value-dl">{dashboardData?.totalItems || 0}</h2>
                        </div>
                    </div>

                    <div className="metric-card-dl metric-error">
                        <div className="metric-icon-wrapper-dl">
                            <svg className="metric-icon-dl" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M12 2V6M12 6L8 2M12 6L16 2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                <path d="M4 12C4 16.4183 7.58172 20 12 20C16.4183 20 20 16.4183 20 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            </svg>
                        </div>
                        <div className="metric-content-dl">
                            <p className="metric-label-dl">Total Loss Value</p>
                            <h2 className="metric-value-dl">${dashboardData?.totalLossValue?.toLocaleString() || '0'}</h2>
                        </div>
                    </div>
                </div>

                {/* Chart */}
                <div className="chart-card">
                    <h3 className="chart-title">Loss Distribution by Reason</h3>
                    {getChartData().length > 0 ? (
                        <ResponsiveContainer width="100%" height={250}>
                            <PieChart>
                                <Pie
                                    data={getChartData()}
                                    cx="50%"
                                    cy="50%"
                                    labelLine={false}
                                    outerRadius={80}
                                    fill="#8884d8"
                                    dataKey="value"
                                >
                                    {getChartData().map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip />
                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                    ) : (
                        <div className="no-chart-data">
                            <p>No data available for chart</p>
                        </div>
                    )}
                </div>
            </div>

            {/* Reports Table Section */}
            <div className="reports-section">
                {/* Search and Filter */}
                <div className="section-header-combined">
                    <div className="section-title-wrapper">
                        <h2 className="section-title">Damage & Loss Reports</h2>
                        <p className="section-subtitle">
                            {dashboardData?.reports?.totalElements || 0} total reports
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
                                    placeholder="Search by product name or SKU..."
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
                            className={`filter-btn ${activeFilterReason ?  'active' : ''}`}
                            onClick={() => setShowFilters(!showFilters)}
                        >
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M22 3H2L10 12.46V19L14 21V12.46L22 3Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                            Filter
                            {activeFilterReason && <span className="filter-count">1</span>}
                        </button>
                    </div>
                </div>

                {/* Filter Panel */}
                {showFilters && (
                    <div className="filter-panel">
                        <div className="filter-grid-single">
                            <div className="filter-group">
                                <label className="filter-label">Loss Reason</label>
                                <select
                                    className="filter-select"
                                    value={filterReason}
                                    onChange={(e) => setFilterReason(e.target.value)}
                                >
                                    <option value="">All Reasons</option>
                                    {lossReasons.map(reason => (
                                        <option key={reason.value} value={reason.value}>
                                            {reason.label}
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
                {isLoading && dashboardData && (
                    <div className="table-loading">
                        <div className="spinner-small"></div>
                        <span>Loading...</span>
                    </div>
                )}

                {/* Reports Table */}
                {! isLoading && dashboardData?.reports?.content?.length > 0 ?  (
                    <>
                        <div className="table-container">
                            <table className="reports-table">
                                <thead>
                                <tr>
                                    <th>Product Name</th>
                                    <th>SKU</th>
                                    <th>Quantity Lost</th>
                                    <th>Loss Reason</th>
                                    <th>Loss Date</th>
                                    <th>Reported By</th>
                                    {(isAdminOrManager || isAdmin) && <th>Actions</th>}
                                </tr>
                                </thead>
                                <tbody>
                                {dashboardData.reports.content.map((report) => (
                                    <tr key={report.id}>
                                        <td className="product-name">{report.productName}</td>
                                        <td className="sku-cell">{report.sku}</td>
                                        <td className="quantity-cell">{report.quantityLost}</td>
                                        <td>
                                            <span className="reason-badge"
                                                style={{
                                                    backgroundColor: `${getReasonColor(report. reason)}20`,
                                                    color: getReasonColor(report.reason),
                                                    borderLeft: `3px solid ${getReasonColor(report.reason)}`
                                                }}
                                            >
                                            {report.reason. replace(/_/g, ' ')}
                                            </span>
                                        </td>
                                        <td>{formatDate(report.lossDate)}</td>
                                        <td>{report.recordedBy}</td>
                                        {(isAdminOrManager || isAdmin) && (
                                            <td>
                                                <div className="action-buttons">
                                                    {isAdminOrManager && (
                                                        <button
                                                            className="action-btn edit-btn"
                                                            onClick={() => handleOpenEditModal(report)}
                                                            title="Edit Report"
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
                                                            onClick={() => handleOpenDeleteModal(report)}
                                                            title="Delete Report"
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
                        {dashboardData.reports.totalPages > 1 && (
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

                                {currentPage < dashboardData.reports.totalPages - 3 && (
                                    <>
                                        {currentPage < dashboardData.reports.totalPages - 4 && (
                                            <span className="pagination-ellipsis">...</span>
                                        )}
                                        <button
                                            className="pagination-btn pagination-number"
                                            onClick={() => handlePageChange(dashboardData.reports.totalPages - 1)}
                                        >
                                            {dashboardData.reports.totalPages}
                                        </button>
                                    </>
                                )}

                                <button
                                    className="pagination-btn pagination-arrow"
                                    onClick={() => handlePageChange(currentPage + 1)}
                                    disabled={currentPage === dashboardData.reports.totalPages - 1}
                                >
                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M9 18L15 12L9 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                </button>

                                <div className="pagination-info">
                                    Page {currentPage + 1} of {dashboardData.reports.totalPages}
                                </div>
                            </div>
                        )}
                    </>
                ) : !isLoading && (
                    <div className="empty-state">
                        <svg className="empty-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                            <path d="M12 8V12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            <circle cx="12" cy="16" r="1" fill="currentColor"/>
                        </svg>
                        <h3>No Reports Found</h3>
                        <p>There are currently no damage/loss reports to display. </p>
                    </div>
                )}
            </div>

            {/* Add Modal */}
            {showAddModal && (
                <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Add Damage/Loss Report</h2>
                            <button className="modal-close-btn" onClick={() => setShowAddModal(false)}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <form onSubmit={handleAddReport}>
                            <div className="modal-body">
                                {formErrors.submit && (
                                    <div className="error-alert">
                                        {formErrors.submit}
                                    </div>
                                )}

                                <div className="form-group">
                                    <label className="form-label">SKU *</label>
                                    <input
                                        type="text"
                                        className={`form-input ${formErrors.sku ? 'input-error' : ''}`}
                                        placeholder="Enter product SKU"
                                        value={formData.sku}
                                        onChange={(e) => setFormData({ ...formData, sku: e.target.value })}
                                    />
                                    {formErrors.sku && <span className="error-text">{formErrors.sku}</span>}
                                </div>

                                <div className="form-group">
                                    <label className="form-label">Quantity Lost *</label>
                                    <input
                                        type="number"
                                        min="1"
                                        className={`form-input ${formErrors.quantityLost ? 'input-error' : ''}`}
                                        placeholder="Enter quantity"
                                        value={formData.quantityLost}
                                        onChange={(e) => setFormData({ ...formData, quantityLost: e.target.value })}
                                    />
                                    {formErrors.quantityLost && <span className="error-text">{formErrors.quantityLost}</span>}
                                </div>

                                <div className="form-group">
                                    <label className="form-label">Loss Reason *</label>
                                    <select
                                        className={`form-select ${formErrors.reason ? 'input-error' :  ''}`}
                                        value={formData.reason}
                                        onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
                                    >
                                        <option value="">Select reason</option>
                                        {lossReasons.map(reason => (
                                            <option key={reason.value} value={reason.value}>
                                                {reason.label} - {reason.description}
                                            </option>
                                        ))}
                                    </select>
                                    {formErrors.reason && <span className="error-text">{formErrors.reason}</span>}
                                </div>

                                <div className="form-group">
                                    <label className="form-label">Loss Date (Optional)</label>
                                    <input
                                        type="datetime-local"
                                        className="form-input"
                                        value={formData.lossDate}
                                        onChange={(e) => setFormData({ ...formData, lossDate: e.target.value })}
                                    />
                                    <small className="form-help">Leave empty to use current date/time</small>
                                </div>
                            </div>

                            <div className="modal-footer">
                                <button type="button" className="btn-secondary" onClick={() => setShowAddModal(false)}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn-primary">
                                    Add Report
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Edit Modal - SKU REMOVED */}
            {showEditModal && (
                <div className="modal-overlay" onClick={() => setShowEditModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Edit Damage/Loss Report</h2>
                            <button className="modal-close-btn" onClick={() => setShowEditModal(false)}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <form onSubmit={handleUpdateReport}>
                            <div className="modal-body">
                                {formErrors.submit && (
                                    <div className="error-alert">
                                        {formErrors.submit}
                                    </div>
                                )}

                                {/* SKU Display Only - NOT EDITABLE */}
                                <div className="form-group">
                                    <label className="form-label">SKU (Cannot be changed)</label>
                                    <div className="sku-display">
                                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M12 15C13.6569 15 15 13.6569 15 12C15 10.3431 13.6569 9 12 9C10.3431 9 9 10.3431 9 12C9 13.6569 10.3431 15 12 15Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                            <path d="M19.4 15C19.2669 15.3016 19.2272 15.6362 19.286 15.9606C19.3448 16.285 19.4995 16.5843 19.73 16.82L19.79 16.88C19.976 17.0657 20.1235 17.2863 20.2241 17.5291C20.3248 17.7719 20.3766 18.0322 20.3766 18.295C20.3766 18.5578 20.3248 18.8181 20.2241 19.0609C20.1235 19.3037 19.976 19.5243 19.79 19.71C19.6043 19.896 19.3837 20.0435 19.1409 20.1441C18.8981 20.2448 18.6378 20.2966 18.375 20.2966C18.1122 20.2966 17.8519 20.2448 17.6091 20.1441C17.3663 20.0435 17.1457 19.896 16.96 19.71L16.9 19.65C16.6643 19.4195 16.365 19.2648 16.0406 19.206C15.7162 19.1472 15.3816 19.1869 15.08 19.32C14.7842 19.4468 14.532 19.6572 14.3543 19.9255C14.1766 20.1938 14.0813 20.5082 14.08 20.83V21C14.08 21.5304 13.8693 22.0391 13.4942 22.4142C13.1191 22.7893 12.6104 23 12.08 23C11.5496 23 11.0409 22.7893 10.6658 22.4142C10.2907 22.0391 10.08 21.5304 10.08 21V20.91C10.0723 20.579 9.96512 20.258 9.77251 19.9887C9.5799 19.7194 9.31074 19.5143 9 19.4C8.69838 19.2669 8.36381 19.2272 8.03941 19.286C7.71502 19.3448 7.41568 19.4995 7.18 19.73L7.12 19.79C6.93425 19.976 6.71368 20.1235 6.47088 20.2241C6.22808 20.3248 5.96783 20.3766 5.705 20.3766C5.44217 20.3766 5.18192 20.3248 4.93912 20.2241C4.69632 20.1235 4.47575 19.976 4.29 19.79C4.10405 19.6043 3.95653 19.3837 3.85588 19.1409C3.75523 18.8981 3.70343 18.6378 3.70343 18.375C3.70343 18.1122 3.75523 17.8519 3.85588 17.6091C3.95653 17.3663 4.10405 17.1457 4.29 16.96L4.35 16.9C4.58054 16.6643 4.73519 16.365 4.794 16.0406C4.85282 15.7162 4.81312 15.3816 4.68 15.08C4.55324 14.7842 4.34276 14.532 4.07447 14.3543C3.80618 14.1766 3.49179 14.0813 3.17 14.08H3C2.46957 14.08 1.96086 13.8693 1.58579 13.4942C1.21071 13.1191 1 12.6104 1 12.08C1 11.5496 1.21071 11.0409 1.58579 10.6658C1.96086 10.2907 2.46957 10.08 3 10.08H3.09C3.42099 10.0723 3.742 9.96512 4.0113 9.77251C4.28059 9.5799 4.48572 9.31074 4.6 9C4.73312 8.69838 4.77282 8.36381 4.714 8.03941C4.65519 7.71502 4.50054 7.41568 4.27 7.18L4.21 7.12C4.02405 6.93425 3.87653 6.71368 3.77588 6.47088C3.67523 6.22808 3.62343 5.96783 3.62343 5.705C3.62343 5.44217 3.67523 5.18192 3.77588 4.93912C3.87653 4.69632 4.02405 4.47575 4.21 4.29C4.39575 4.10405 4.61632 3.95653 4.85912 3.85588C5.10192 3.75523 5.36217 3.70343 5.625 3.70343C5.88783 3.70343 6.14808 3.75523 6.39088 3.85588C6.63368 3.95653 6.85425 4.10405 7.04 4.29L7.1 4.35C7.33568 4.58054 7.63502 4.73519 7.95941 4.794C8.28381 4.85282 8.61838 4.81312 8.92 4.68H9C9.29577 4.55324 9.54802 4.34276 9.72569 4.07447C9.90337 3.80618 9.99872 3.49179 10 3.17V3C10 2.46957 10.2107 1.96086 10.5858 1.58579C10.9609 1.21071 11.4696 1 12 1C12.5304 1 13.0391 1.21071 13.4142 1.58579C13.7893 1.96086 14 2.46957 14 3V3.09C14.0013 3.41179 14.0966 3.72618 14.2743 3.99447C14.452 4.26276 14.7042 4.47324 15 4.6C15.3016 4.73312 15.6362 4.77282 15.9606 4.714C16.285 4.65519 16.5843 4.50054 16.82 4.27L16.88 4.21C17.0657 4.02405 17.2863 3.87653 17.5291 3.77588C17.7719 3.67523 18.0322 3.62343 18.295 3.62343C18.5578 3.62343 18.8181 3.67523 19.0609 3.77588C19.3037 3.87653 19.5243 4.02405 19.71 4.21C19.896 4.39575 20.0435 4.61632 20.1441 4.85912C20.2448 5.10192 20.2966 5.36217 20.2966 5.625C20.2966 5.88783 20.2448 6.14808 20.1441 6.39088C20.0435 6.63368 19.896 6.85425 19.71 7.04L19.65 7.1C19.4195 7.33568 19.2648 7.63502 19.206 7.95941C19.1472 8.28381 19.1869 8.61838 19.32 8.92V9C19.4468 9.29577 19.6572 9.54802 19.9255 9.72569C20.1938 9.90337 20.5082 9.99872 20.83 10H21C21.5304 10 22.0391 10.2107 22.4142 10.5858C22.7893 10.9609 23 11.4696 23 12C23 12.5304 22.7893 13.0391 22.4142 13.4142C22.0391 13.7893 21.5304 14 21 14H20.91C20.5882 14.0013 20.2738 14.0966 20.0055 14.2743C19.7372 14.452 19.5268 14.7042 19.4 15Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                        </svg>
                                        <span className="sku-value">{selectedReport?.sku}</span>
                                        <span className="sku-product-name">({selectedReport?.productName})</span>
                                    </div>
                                    <small className="form-help">SKU cannot be changed. Create a new report to use a different SKU.</small>
                                </div>

                                <div className="form-group">
                                    <label className="form-label">Quantity Lost *</label>
                                    <input
                                        type="number"
                                        min="1"
                                        className={`form-input ${formErrors.quantityLost ? 'input-error' : ''}`}
                                        placeholder="Enter quantity"
                                        value={formData.quantityLost}
                                        onChange={(e) => setFormData({ ...formData, quantityLost: e.target.value })}
                                    />
                                    {formErrors.quantityLost && <span className="error-text">{formErrors.quantityLost}</span>}
                                </div>

                                <div className="form-group">
                                    <label className="form-label">Loss Reason *</label>
                                    <select
                                        className={`form-select ${formErrors.reason ? 'input-error' : ''}`}
                                        value={formData.reason}
                                        onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
                                    >
                                        <option value="">Select reason</option>
                                        {lossReasons.map(reason => (
                                            <option key={reason.value} value={reason.value}>
                                                {reason.label} - {reason.description}
                                            </option>
                                        ))}
                                    </select>
                                    {formErrors.reason && <span className="error-text">{formErrors.reason}</span>}
                                </div>

                                <div className="form-group">
                                    <label className="form-label">Loss Date (Optional)</label>
                                    <input
                                        type="datetime-local"
                                        className="form-input"
                                        value={formData.lossDate}
                                        onChange={(e) => setFormData({ ...formData, lossDate: e.target.value })}
                                    />
                                </div>
                            </div>

                            <div className="modal-footer">
                                <button type="button" className="btn-secondary" onClick={() => setShowEditModal(false)}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn-primary">
                                    Update Report
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
                            <h2>Delete Report</h2>
                            <button className="modal-close-btn" onClick={() => setShowDeleteModal(false)}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <div className="modal-body">
                            <p>Are you sure you want to delete this damage/loss report?</p>
                            <p className="warning-text">This action will restore the stock to inventory and cannot be undone.</p>
                        </div>

                        <div className="modal-footer">
                            <button type="button" className="btn-secondary" onClick={() => setShowDeleteModal(false)}>
                                Cancel
                            </button>
                            <button type="button" className="btn-danger" onClick={handleDeleteReport}>
                                Delete Report
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default DamageLoss;