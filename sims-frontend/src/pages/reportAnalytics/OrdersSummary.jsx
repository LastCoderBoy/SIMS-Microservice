import { useState, useEffect } from 'react';
import { Chart as ChartJS, ArcElement, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js';
import { Doughnut, Bar } from 'react-chartjs-2';
import analyticsService from '../../services/reportAnalytics/analyticsService';
import authService from '../../services/userManagement/authService.js';
import './OrdersSummary.css';

// Register Chart.js components
ChartJS.register(ArcElement, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const OrdersSummary = () => {
    const currentUser = authService.getCurrentUser();
    const isAdminOrManager = currentUser?.role === 'ROLE_ADMIN' || currentUser?.role === 'ROLE_MANAGER';

    const [metrics, setMetrics] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // Chart colors
    const chartColors = {
        primary: '#1976d2',
        success: '#4caf50',
        warning: '#ff9800',
        error: '#f44336',
        info: '#2196f3',
        purple: '#9c27b0',
        teal: '#00bcd4',
        grey: '#757575',
        pink: '#e91e63',
        indigo: '#3f51b5',
        cyan: '#00bcd4',
        amber: '#ffc107',
    };

    // Fetch order summary metrics
    const fetchMetrics = async () => {
        setIsLoading(true);
        setError(null);

        try {
            const data = await analyticsService.getOrderSummary();
            setMetrics(data.data);
        } catch (err) {
            console.error('Error fetching orders summary:', err);
            setError(err.message || 'Failed to load orders summary');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        if (isAdminOrManager) {
            fetchMetrics();
        } else {
            setError('Access Denied: Admin or Manager role required');
            setIsLoading(false);
        }
    }, []);

    // Sales Orders Status Chart (Doughnut)
    const salesOrdersChartData = {
            labels:  ['Completed', 'Delivered', 'Approved', 'Pending', 'Partially Approved', 'Partially Delivered', 'Delivery In Process', 'Cancelled'],
            datasets: [
                {
                    data: [
                        metrics?.salesOrderSummary?.totalCompleted || 0,
                        metrics?.salesOrderSummary?.totalDelivered || 0,
                    metrics?.salesOrderSummary?.totalApproved || 0,
            metrics?.salesOrderSummary?.totalPending || 0,
        metrics?.salesOrderSummary?.totalPartiallyApproved || 0,
    metrics?.salesOrderSummary?.totalPartiallyDelivered || 0,
    metrics?.salesOrderSummary?.totalDeliveryInProcess || 0,
    metrics?.salesOrderSummary?.totalCancelled || 0,
],
    backgroundColor: [
        chartColors.success,
        chartColors.teal,
        chartColors.primary,
        chartColors.warning,
        chartColors.purple,
        chartColors.indigo,
        chartColors.info,
        chartColors.error,
    ],
        borderColor: '#fff',
        borderWidth: 3,
},
],
};

    const salesOrdersChartOptions = {
        responsive: true,
        maintainAspectRatio:  false,
        plugins: {
            legend: {
                position: 'bottom',
                labels: {
                    padding: 12,
                    font: {
                        size: 11,
                        weight: '600',
                    },
                    boxWidth: 12,
                },
            },
            tooltip: {
                callbacks: {
                    label: function (context) {
                        const label = context.label || '';
                        const value = context.parsed || 0;
                        const total = context.dataset.data.reduce((a, b) => a + b, 0);
                        const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
                        return `${label}: ${value} (${percentage}%)`;
                    },
                },
            },
        },
    };

    // Purchase Orders Status Chart (Doughnut)
    const purchaseOrdersChartData = {
            labels: ['Received', 'Delivery In Process', 'Partially Received', 'Awaiting Approval', 'Failed', 'Cancelled'],
            datasets: [
                {
                    data: [
                        metrics?.purchaseOrderSummary?.totalReceived || 0,
                    metrics?.purchaseOrderSummary?.totalDeliveryInProcess || 0,
                metrics?.purchaseOrderSummary?.totalPartiallyReceived || 0,
                metrics?.purchaseOrderSummary?.totalAwaitingApproval || 0,
            metrics?.purchaseOrderSummary?.totalFailed || 0,
        metrics?.purchaseOrderSummary?.totalCancelled || 0,
],
    backgroundColor:  [
        chartColors.success,
        chartColors.info,
        chartColors.purple,
        chartColors.warning,
        chartColors.error,
        chartColors.grey,
    ],
        borderColor: '#fff',
        borderWidth: 3,
},
],
};

    const purchaseOrdersChartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'bottom',
                labels: {
                    padding: 12,
                    font: {
                        size: 11,
                        weight: '600',
                    },
                    boxWidth: 12,
                },
            },
            tooltip: {
                callbacks: {
                    label: function (context) {
                        const label = context.label || '';
                        const value = context.parsed || 0;
                        const total = context.dataset.data.reduce((a, b) => a + b, 0);
                        const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
                        return `${label}: ${value} (${percentage}%)`;
                    },
                },
            },
        },
    };

    // Comparison Bar Chart
    const comparisonChartData = {
            labels:  ['Total Orders', 'In Progress/Valid', 'Completed/Received', 'Cancelled/Failed'],
            datasets: [
                {
                    label: 'Sales Orders',
                    data: [
                        metrics?.salesOrderSummary?.totalOrders || 0,
                        metrics?.salesOrderSummary?.totalInProgress || 0,
                        metrics?.salesOrderSummary?.totalCompleted || 0,
                        metrics?.salesOrderSummary?.totalCancelled || 0,
                    ],
                    backgroundColor: chartColors.primary,
                    borderRadius: 6,
                },
                {
                    label:  'Purchase Orders',
                    data: [
                        metrics?.purchaseOrderSummary?.totalOrders || 0,
                    metrics?.purchaseOrderSummary?.totalValid || 0,
                metrics?.purchaseOrderSummary?.totalReceived || 0,
        (metrics?.purchaseOrderSummary?.totalCancelled || 0) + (metrics?.purchaseOrderSummary?.totalFailed || 0),
        ],
        backgroundColor:  chartColors.purple,
        borderRadius: 6,
        },
        ],
        };

    const comparisonChartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend:  {
                position: 'top',
                labels: {
                    padding: 15,
                    font: {
                        size: 12,
                        weight: '600',
                    },
                },
            },
            tooltip: {
                callbacks:  {
                    label: function (context) {
                        return `${context.dataset.label}: ${context.parsed.y}`;
                    },
                },
            },
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks: {
                    stepSize: 1,
                },
                grid: {
                    color: 'rgba(0, 0, 0, 0.05)',
                },
            },
            x: {
                grid: {
                    display: false,
                },
            },
        },
    };

    // Loading State
    if (isLoading) {
        return (
            <div className="loading-container">
                <div className="spinner-large"></div>
                <p>Loading orders summary...</p>
            </div>
        );
    }

    // Error State
    if (error) {
        return (
            <div className="error-container">
                <svg className="error-icon-large" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                    <path d="M12 8V12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                </svg>
                <h2>Error Loading Orders Summary</h2>
                <p>{error}</p>
                {isAdminOrManager && (
                    <button className="retry-btn" onClick={fetchMetrics}>
                        Try Again
                    </button>
                )}
            </div>
        );
    }

    return (
        <div className="orders-summary-page">
            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">📦 Orders Summary</h1>
                    <p className="page-subtitle">Overview of sales and purchase orders performance</p>
                </div>
                <button className="refresh-btn" onClick={fetchMetrics}>
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21.5 2V8M21.5 8H15.5M21.5 8L18 4.5C16.8 3.3 15.3 2.4 13.6 2C9.9 1 6.1 2.7 4.1 6C2.1 9.3 2.3 13.5 4.8 16.5C7.3 19.5 11.3 20.6 14.9 19.3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                    Refresh
                </button>
            </div>

            {/* Overview Cards */}
            <div className="overview-cards-grid">
                {/* Sales Orders Overview */}
                <div className="overview-card overview-sales">
                    <div className="overview-header">
                        <div className="overview-icon">
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <rect x="1" y="3" width="15" height="13" rx="2" stroke="currentColor" strokeWidth="2"/>
                                <path d="M16 8H20L23 11V16H16V8Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
                                <circle cx="5.5" cy="18.5" r="2.5" stroke="currentColor" strokeWidth="2"/>
                                <circle cx="18.5" cy="18.5" r="2.5" stroke="currentColor" strokeWidth="2"/>
                            </svg>
                        </div>
                        <h2>Sales Orders</h2>
                    </div>
                    <div className="overview-stats">
                        <div className="overview-stat-main">
                            <span className="stat-value-large">{metrics?.salesOrderSummary?.totalOrders || 0}</span>
                            <span className="stat-label">Total Orders</span>
                        </div>
                        <div className="overview-stat-secondary">
                            <div className="stat-item">
                                <span className="stat-value">{metrics?.salesOrderSummary?.totalInProgress || 0}</span>
                                <span className="stat-label-small">In Progress</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-value">{metrics?.salesOrderSummary?.totalCompleted || 0}</span>
                                <span className="stat-label-small">Completed</span>
                            </div>
                        </div>
                    </div>
                    <div className="overview-progress">
                        <div className="progress-header">
                            <span>Completion Rate</span>
                            <span className="progress-percentage">{metrics?.salesOrderSummary?.completionRate?.toFixed(1)}%</span>
                        </div>
                        <div className="progress-bar-summary">
                            <div
                                className="progress-fill-summary progress-sales"
                                style={{ width:  `${metrics?.salesOrderSummary?.completionRate || 0}%` }}
                            ></div>
                        </div>
                    </div>
                </div>

                {/* Purchase Orders Overview */}
                <div className="overview-card overview-purchase">
                    <div className="overview-header">
                        <div className="overview-icon">
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M16 11V7C16 5.89543 15.1046 5 14 5H10C8.89543 5 8 5.89543 8 7V11" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                <rect x="3" y="9" width="18" height="13" rx="2" stroke="currentColor" strokeWidth="2"/>
                                <path d="M12 14V17" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            </svg>
                        </div>
                        <h2>Purchase Orders</h2>
                    </div>
                    <div className="overview-stats">
                        <div className="overview-stat-main">
                            <span className="stat-value-large">{metrics?.purchaseOrderSummary?.totalOrders || 0}</span>
                            <span className="stat-label">Total Orders</span>
                        </div>
                        <div className="overview-stat-secondary">
                            <div className="stat-item">
                                <span className="stat-value">{metrics?.purchaseOrderSummary?.totalValid || 0}</span>
                                <span className="stat-label-small">Valid</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-value">{metrics?.purchaseOrderSummary?.totalReceived || 0}</span>
                                <span className="stat-label-small">Received</span>
                            </div>
                        </div>
                    </div>
                    <div className="overview-progress">
                        <div className="progress-header">
                            <span>Success Rate</span>
                            <span className="progress-percentage">{metrics?.purchaseOrderSummary?.successRate?.toFixed(1)}%</span>
                        </div>
                        <div className="progress-bar-summary">
                            <div
                                className="progress-fill-summary progress-purchase"
                                style={{ width: `${metrics?.purchaseOrderSummary?.successRate || 0}%` }}
                            ></div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Status Distribution Charts */}
            <div className="charts-section-summary">
                {/* Sales Orders Chart */}
                <div className="chart-card-summary">
                    <div className="chart-header">
                        <h3 className="chart-title">📊 Sales Orders Status Distribution</h3>
                        <span className="chart-subtitle">Breakdown by order status</span>
                    </div>
                    <div className="chart-wrapper-summary">
                        <Doughnut data={salesOrdersChartData} options={salesOrdersChartOptions} />
                    </div>
                </div>

                {/* Purchase Orders Chart */}
                <div className="chart-card-summary">
                    <div className="chart-header">
                        <h3 className="chart-title">📊 Purchase Orders Status Distribution</h3>
                        <span className="chart-subtitle">Breakdown by order status</span>
                    </div>
                    <div className="chart-wrapper-summary">
                        <Doughnut data={purchaseOrdersChartData} options={purchaseOrdersChartOptions} />
                    </div>
                </div>
            </div>

            {/* Comparison Chart */}
            <div className="chart-card-summary chart-full">
                <div className="chart-header">
                    <h3 className="chart-title">📈 Sales vs Purchase Orders Comparison</h3>
                    <span className="chart-subtitle">Side-by-side performance metrics</span>
                </div>
                <div className="chart-wrapper-summary chart-tall">
                    <Bar data={comparisonChartData} options={comparisonChartOptions} />
                </div>
            </div>

            {/* Detailed Stats Tables */}
            <div className="stats-tables-section">
                {/* Sales Orders Table */}
                <div className="stats-table-card">
                    <h3 className="table-title">📋 Sales Orders Details</h3>
                    <div className="stats-table-wrapper">
                        <table className="stats-table">
                            <thead>
                            <tr>
                                <th>Status</th>
                                <th>Count</th>
                                <th>Percentage</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td><span className="status-dot status-success"></span> Completed</td>
                                <td className="count-cell">{metrics?.salesOrderSummary?.totalCompleted || 0}</td>
                                <td className="percentage-cell">
                                    {((metrics?.salesOrderSummary?.totalCompleted / metrics?.salesOrderSummary?.totalOrders) * 100 || 0).toFixed(1)}%
                                </td>
                            </tr>
                            <tr>
                                <td><span className="status-dot status-teal"></span> Delivered</td>
                                <td className="count-cell">{metrics?.salesOrderSummary?.totalDelivered || 0}</td>
                                <td className="percentage-cell">
                                    {((metrics?.salesOrderSummary?.totalDelivered / metrics?.salesOrderSummary?.totalOrders) * 100 || 0).toFixed(1)}%
                                </td>
                            </tr>
                            <tr>
                                <td><span className="status-dot status-primary"></span> Approved</td>
                                <td className="count-cell">{metrics?.salesOrderSummary?.totalApproved || 0}</td>
                                <td className="percentage-cell">
                                    {((metrics?.salesOrderSummary?.totalApproved / metrics?.salesOrderSummary?.totalOrders) * 100 || 0).toFixed(1)}%
                                </td>
                            </tr>
                            <tr>
                                <td><span className="status-dot status-warning"></span> Pending</td>
                                <td className="count-cell">{metrics?.salesOrderSummary?.totalPending || 0}</td>
                                <td className="percentage-cell">
                                    {((metrics?.salesOrderSummary?.totalPending / metrics?.salesOrderSummary?.totalOrders) * 100 || 0).toFixed(1)}%
                                </td>
                            </tr>
                            <tr>
                                <td><span className="status-dot status-purple"></span> Partially Approved</td>
                                <td className="count-cell">{metrics?.salesOrderSummary?.totalPartiallyApproved || 0}</td>
                                <td className="percentage-cell">
                                    {((metrics?.salesOrderSummary?.totalPartiallyApproved / metrics?.salesOrderSummary?.totalOrders) * 100 || 0).toFixed(1)}%
                                </td>
                            </tr>
                            <tr>
                                <td><span className="status-dot status-indigo"></span> Partially Delivered</td>
                                <td className="count-cell">{metrics?.salesOrderSummary?.totalPartiallyDelivered || 0}</td>
                                <td className="percentage-cell">
                                    {((metrics?.salesOrderSummary?.totalPartiallyDelivered / metrics?.salesOrderSummary?.totalOrders) * 100 || 0).toFixed(1)}%
                                </td>
                            </tr>
                            <tr>
                                <td><span className="status-dot status-info"></span> Delivery In Process</td>
                                <td className="count-cell">{metrics?.salesOrderSummary?.totalDeliveryInProcess || 0}</td>
                                <td className="percentage-cell">
                                    {((metrics?.salesOrderSummary?.totalDeliveryInProcess / metrics?.salesOrderSummary?.totalOrders) * 100 || 0).toFixed(1)}%
                                </td>
                            </tr>
                            <tr>
                                <td><span className="status-dot status-error"></span> Cancelled</td>
                                <td className="count-cell">{metrics?.salesOrderSummary?.totalCancelled || 0}</td>
                                <td className="percentage-cell">
                                    {((metrics?.salesOrderSummary?.totalCancelled / metrics?.salesOrderSummary?.totalOrders) * 100 || 0).toFixed(1)}%
                                </td>
                            </tr>
                            <tr className="table-total">
                                <td><strong>Total</strong></td>
                                <td className="count-cell"><strong>{metrics?.salesOrderSummary?.totalOrders || 0}</strong></td>
                                <td className="percentage-cell"><strong>100%</strong></td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Purchase Orders Table */}
                <div className="stats-table-card">
                    <h3 className="table-title">📋 Purchase Orders Details</h3>
                    <div className="stats-table-wrapper">
                        <table className="stats-table">
                            <thead>
                            <tr>
                                <th>Status</th>
                                <th>Count</th>
                                <th>Percentage</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td><span className="status-dot status-success"></span> Received</td>
                                <td className="count-cell">{metrics?.purchaseOrderSummary?.totalReceived || 0}</td>
                                <td className="percentage-cell">
                                    {((metrics?.purchaseOrderSummary?.totalReceived / metrics?.purchaseOrderSummary?.totalOrders) * 100 || 0).toFixed(1)}%
                                </td>
                            </tr>
                            <tr>
                                <td><span className="status-dot status-info"></span> Delivery In Process</td>
                                <td className="count-cell">{metrics?.purchaseOrderSummary?.totalDeliveryInProcess || 0}</td>
                                <td className="percentage-cell">
                                    {((metrics?.purchaseOrderSummary?.totalDeliveryInProcess / metrics?.purchaseOrderSummary?.totalOrders) * 100 || 0).toFixed(1)}%
                                </td>
                            </tr>
                            <tr>
                                <td><span className="status-dot status-purple"></span> Partially Received</td>
                                <td className="count-cell">{metrics?.purchaseOrderSummary?.totalPartiallyReceived || 0}</td>
                                <td className="percentage-cell">
                                    {((metrics?.purchaseOrderSummary?.totalPartiallyReceived / metrics?.purchaseOrderSummary?.totalOrders) * 100 || 0).toFixed(1)}%
                                </td>
                            </tr>
                            <tr>
                                <td><span className="status-dot status-warning"></span> Awaiting Approval</td>
                                <td className="count-cell">{metrics?.purchaseOrderSummary?.totalAwaitingApproval || 0}</td>
                                <td className="percentage-cell">
                                    {((metrics?.purchaseOrderSummary?.totalAwaitingApproval / metrics?.purchaseOrderSummary?.totalOrders) * 100 || 0).toFixed(1)}%
                                </td>
                            </tr>
                            <tr>
                                <td><span className="status-dot status-error"></span> Failed</td>
                                <td className="count-cell">{metrics?.purchaseOrderSummary?.totalFailed || 0}</td>
                                <td className="percentage-cell">
                                    {((metrics?.purchaseOrderSummary?.totalFailed / metrics?.purchaseOrderSummary?.totalOrders) * 100 || 0).toFixed(1)}%
                                </td>
                            </tr>
                            <tr>
                                <td><span className="status-dot status-grey"></span> Cancelled</td>
                                <td className="count-cell">{metrics?.purchaseOrderSummary?.totalCancelled || 0}</td>
                                <td className="percentage-cell">
                                    {((metrics?.purchaseOrderSummary?.totalCancelled / metrics?.purchaseOrderSummary?.totalOrders) * 100 || 0).toFixed(1)}%
                                </td>
                            </tr>
                            <tr className="table-total">
                                <td><strong>Total</strong></td>
                                <td className="count-cell"><strong>{metrics?.purchaseOrderSummary?.totalOrders || 0}</strong></td>
                                <td className="percentage-cell"><strong>100%</strong></td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default OrdersSummary;