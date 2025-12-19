import { useState, useEffect } from 'react';
import { Chart as ChartJS, ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title } from 'chart.js';
import { Pie, Doughnut } from 'react-chartjs-2';
import analyticsService from '../../services/reportAnalytics/analyticsService';
import './AnalyticsDashboard.css';

// Register Chart.js components
ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title);

const AnalyticsDashboard = () => {
    const [metrics, setMetrics] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // Chart colors
    const chartColors = {
        primary: '#1976d2',
        success: '#4caf50',
        warning: '#ff9800',
        error:  '#f44336',
        info: '#2196f3',
        purple: '#9c27b0',
        teal: '#00bcd4',
        grey: '#757575',
    };

    // Fetch dashboard metrics
    const fetchMetrics = async () => {
        setIsLoading(true);
        setError(null);

        try {
            const data = await analyticsService.getDashboardMetrics();
            setMetrics(data.data);
        } catch (err) {
            console.error('Error fetching dashboard metrics:', err);
            setError(err.message || 'Failed to load dashboard metrics');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchMetrics();
    }, []);

    // Format currency
    const formatCurrency = (value) => {
        return new Intl.NumberFormat('en-US', {
            style:  'currency',
            currency:  'USD',
            minimumFractionDigits: 2,
        }).format(value || 0);
    };

    // Products Distribution Chart (Pie)
    const productsChartData = {
        labels:  ['Active Products', 'Inactive Products'],
        datasets: [
            {
                data: [
                    metrics?.totalActiveProducts || 0,
                    metrics?.totalInactiveProducts || 0,
                ],
                backgroundColor: [chartColors.success, chartColors.grey],
                borderColor: ['#fff', '#fff'],
                borderWidth: 2,
            },
        ],
    };

    const productsChartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'bottom',
                labels: {
                    padding: 15,
                    font: {
                        size: 12,
                        weight: '600',
                    },
                },
            },
            tooltip: {
                callbacks: {
                    label: function (context) {
                        const label = context.label || '';
                        const value = context.parsed || 0;
                        const total = context.dataset.data.reduce((a, b) => a + b, 0);
                        const percentage = ((value / total) * 100).toFixed(1);
                        return `${label}: ${value} (${percentage}%)`;
                    },
                },
            },
        },
    };

    // Orders Status Chart (Doughnut)
    const ordersChartData = {
        labels: ['In Progress Sales Orders', 'Valid Purchase Orders'],
        datasets: [
            {
                data: [
                    metrics?.totalInProgressSalesOrders || 0,
                    metrics?.totalValidPurchaseOrders || 0,
                ],
                backgroundColor: [chartColors.primary, chartColors.purple],
                borderColor: ['#fff', '#fff'],
                borderWidth: 2,
            },
        ],
    };

    const ordersChartOptions = {
        responsive: true,
        maintainAspectRatio:  false,
        cutout: '60%',
        plugins: {
            legend: {
                position:  'bottom',
                labels:  {
                    padding: 15,
                    font: {
                        size: 12,
                        weight: '600',
                    },
                },
            },
            tooltip: {
                callbacks: {
                    label:  function (context) {
                        const label = context.label || '';
                        const value = context.parsed || 0;
                        return `${label}: ${value}`;
                    },
                },
            },
        },
    };

    // Loading State
    if (isLoading) {
        return (
            <div className="loading-container">
                <div className="spinner-large"></div>
                <p>Loading dashboard...</p>
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
                <h2>Error Loading Dashboard</h2>
                <p>{error}</p>
                <button className="retry-btn" onClick={fetchMetrics}>
                    Try Again
                </button>
            </div>
        );
    }

    return (
        <div className="analytics-dashboard-page">
            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">📊 Analytics Dashboard</h1>
                    <p className="page-subtitle">Overview of key business metrics and insights</p>
                </div>
                <button className="refresh-btn" onClick={fetchMetrics}>
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21.5 2V8M21.5 8H15.5M21.5 8L18 4.5C16.8 3.3 15.3 2.4 13.6 2C9.9 1 6.1 2.7 4.1 6C2.1 9.3 2.3 13.5 4.8 16.5C7.3 19.5 11.3 20.6 14.9 19.3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                    Refresh
                </button>
            </div>

            {/* Metrics Cards Grid */}
            <div className="metrics-grid-dashboard">
                {/* Active Products */}
                <div className="metric-card-dashboard metric-success">
                    <div className="metric-icon-wrapper-dashboard">
                        <svg className="metric-icon-dashboard" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 11L12 14L22 4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-dashboard">
                        <p className="metric-label-dashboard">Active Products</p>
                        <h2 className="metric-value-dashboard">{metrics?.totalActiveProducts || 0}</h2>
                        <p className="metric-sublabel-dashboard">Products in stock</p>
                    </div>
                </div>

                {/* Inactive Products */}
                <div className="metric-card-dashboard metric-grey">
                    <div className="metric-icon-wrapper-dashboard">
                        <svg className="metric-icon-dashboard" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                            <path d="M15 9L9 15M9 9L15 15" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-dashboard">
                        <p className="metric-label-dashboard">Inactive Products</p>
                        <h2 className="metric-value-dashboard">{metrics?.totalInactiveProducts || 0}</h2>
                        <p className="metric-sublabel-dashboard">Out of stock</p>
                    </div>
                </div>

                {/* Total Stock Value */}
                <div className="metric-card-dashboard metric-primary">
                    <div className="metric-icon-wrapper-dashboard">
                        <svg className="metric-icon-dashboard" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 2V22M17 5H9.5C8.57174 5 7.6815 5.36875 7.02513 6.02513C6.36875 6.6815 6 7.57174 6 8.5C6 9.42826 6.36875 10.3185 7.02513 10.9749C7.6815 11.6313 8.57174 12 9.5 12H14.5C15.4283 12 16.3185 12.3687 16.9749 13.0251C17.6313 13.6815 18 14.5717 18 15.5C18 16.4283 17.6313 17.3185 16.9749 17.9749C16.3185 18.6313 15.4283 19 14.5 19H6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-dashboard">
                        <p className="metric-label-dashboard">Total Stock Value</p>
                        <h2 className="metric-value-dashboard">{formatCurrency(metrics?.totalInventoryStockValue)}</h2>
                        <p className="metric-sublabel-dashboard">Current inventory value</p>
                    </div>
                </div>

                {/* In Progress Sales Orders */}
                <div className="metric-card-dashboard metric-info">
                    <div className="metric-icon-wrapper-dashboard">
                        <svg className="metric-icon-dashboard" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="1" y="3" width="15" height="13" rx="2" stroke="currentColor" strokeWidth="2"/>
                            <path d="M16 8H20L23 11V16H16V8Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
                            <circle cx="5.5" cy="18.5" r="2.5" stroke="currentColor" strokeWidth="2"/>
                            <circle cx="18.5" cy="18.5" r="2.5" stroke="currentColor" strokeWidth="2"/>
                        </svg>
                    </div>
                    <div className="metric-content-dashboard">
                        <p className="metric-label-dashboard">Sales Orders (In Progress)</p>
                        <h2 className="metric-value-dashboard">{metrics?.totalInProgressSalesOrders || 0}</h2>
                        <p className="metric-sublabel-dashboard">Active sales orders</p>
                    </div>
                </div>

                {/* Valid Purchase Orders */}
                <div className="metric-card-dashboard metric-purple">
                    <div className="metric-icon-wrapper-dashboard">
                        <svg className="metric-icon-dashboard" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M16 11V7C16 5.89543 15.1046 5 14 5H10C8.89543 5 8 5.89543 8 7V11" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            <rect x="3" y="9" width="18" height="13" rx="2" stroke="currentColor" strokeWidth="2"/>
                            <path d="M12 14V17" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-dashboard">
                        <p className="metric-label-dashboard">Valid Purchase Orders</p>
                        <h2 className="metric-value-dashboard">{metrics?.totalValidPurchaseOrders || 0}</h2>
                        <p className="metric-sublabel-dashboard">Active purchase orders</p>
                    </div>
                </div>

                {/* Damaged Products */}
                <div className="metric-card-dashboard metric-warning">
                    <div className="metric-icon-wrapper-dashboard">
                        <svg className="metric-icon-dashboard" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M10.29 3.86L1.82 18C1.64537 18.3024 1.55296 18.6453 1.55199 18.9945C1.55101 19.3437 1.64151 19.6871 1.81445 19.9905C1.98738 20.2939 2.23675 20.5467 2.53773 20.7239C2.83871 20.901 3.18082 20.9962 3.53 21H20.47C20.8192 20.9962 21.1613 20.901 21.4623 20.7239C21.7633 20.5467 22.0126 20.2939 22.1856 19.9905C22.3585 19.6871 22.449 19.3437 22.448 18.9945C22.447 18.6453 22.3546 18.3024 22.18 18L13.71 3.86C13.5317 3.56611 13.2807 3.32312 12.9812 3.15448C12.6817 2.98585 12.3437 2.89725 12 2.89725C11.6563 2.89725 11.3183 2.98585 11.0188 3.15448C10.7193 3.32312 10.4683 3.56611 10.29 3.86Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M12 9V13" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            <circle cx="12" cy="17" r="1" fill="currentColor"/>
                        </svg>
                    </div>
                    <div className="metric-content-dashboard">
                        <p className="metric-label-dashboard">Damaged Products</p>
                        <h2 className="metric-value-dashboard">{metrics?.totalDamagedProducts || 0}</h2>
                        <p className="metric-sublabel-dashboard">Requires attention</p>
                    </div>
                </div>
            </div>

            {/* Charts Section */}
            <div className="charts-section">
                <h2 className="section-title">📈 Visual Insights</h2>

                <div className="charts-grid">
                    {/* Products Distribution Chart */}
                    <div className="chart-card">
                        <div className="chart-header">
                            <h3 className="chart-title">Products Distribution</h3>
                            <span className="chart-subtitle">Active vs Inactive</span>
                        </div>
                        <div className="chart-wrapper">
                            <Pie data={productsChartData} options={productsChartOptions} />
                        </div>
                    </div>

                    {/* Orders Status Chart */}
                    <div className="chart-card">
                        <div className="chart-header">
                            <h3 className="chart-title">Orders Status</h3>
                            <span className="chart-subtitle">Active Orders Overview</span>
                        </div>
                        <div className="chart-wrapper">
                            <Doughnut data={ordersChartData} options={ordersChartOptions} />
                        </div>
                    </div>
                </div>
            </div>

            {/* Quick Stats Table */}
            <div className="stats-section">
                <h2 className="section-title">📋 Quick Stats</h2>

                <div className="stats-table-wrapper">
                    <table className="stats-table">
                        <thead>
                        <tr>
                            <th>Metric</th>
                            <th>Value</th>
                            <th>Status</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td className="metric-name">Total Stock Value</td>
                            <td className="metric-value-cell">{formatCurrency(metrics?.totalInventoryStockValue)}</td>
                            <td>
                                <span className="status-badge status-healthy">📈 Healthy</span>
                            </td>
                        </tr>
                        <tr>
                            <td className="metric-name">Active Products</td>
                            <td className="metric-value-cell">{metrics?.totalActiveProducts || 0}</td>
                            <td>
                                <span className="status-badge status-good">✅ Good</span>
                            </td>
                        </tr>
                        <tr>
                            <td className="metric-name">Inactive Products</td>
                            <td className="metric-value-cell">{metrics?.totalInactiveProducts || 0}</td>
                            <td>
                                <span className="status-badge status-monitor">⚠️ Monitor</span>
                            </td>
                        </tr>
                        <tr>
                            <td className="metric-name">In Progress Sales Orders</td>
                            <td className="metric-value-cell">{metrics?.totalInProgressSalesOrders || 0}</td>
                            <td>
                                <span className="status-badge status-active">🚀 Active</span>
                            </td>
                        </tr>
                        <tr>
                            <td className="metric-name">Valid Purchase Orders</td>
                            <td className="metric-value-cell">{metrics?.totalValidPurchaseOrders || 0}</td>
                            <td>
                                <span className="status-badge status-active">🚀 Active</span>
                            </td>
                        </tr>
                        <tr>
                            <td className="metric-name">Damaged Products</td>
                            <td className="metric-value-cell">{metrics?.totalDamagedProducts || 0}</td>
                            <td>
                                <span className="status-badge status-warning">⚠️ Attention</span>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default AnalyticsDashboard;