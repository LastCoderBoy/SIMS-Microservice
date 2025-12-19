import { useState, useEffect } from 'react';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement, Title, Tooltip, Legend } from 'chart.js';
import { Line, Bar, Pie } from 'react-chartjs-2';
import analyticsService from '../../services/reportAnalytics/analyticsService';
import authService from '../../services/authService';
import './FinancialOverview.css';

// Register Chart.js components
ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement, Title, Tooltip, Legend);

const FinancialOverview = () => {
    const currentUser = authService.getCurrentUser();
    const isAdminOrManager = currentUser?.role === 'ROLE_ADMIN' || currentUser?.role === 'ROLE_MANAGER';

    const [metrics, setMetrics] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // Filter state
    const [timeRange, setTimeRange] = useState('MONTHLY');
    const [customStartDate, setCustomStartDate] = useState('');
    const [customEndDate, setCustomEndDate] = useState('');
    const [showCustomDatePicker, setShowCustomDatePicker] = useState(false);

    // Time range options
    const timeRangeOptions = [
        { value: 'MONTHLY', label:  'Monthly', description: 'Current month' },
        { value: 'YEARLY', label: 'Yearly', description: 'Current year' },
        { value:  'ALL_TIME', label: 'All Time', description: 'Since beginning' },
        { value: 'CUSTOM', label: 'Custom Range', description: 'Select dates' },
    ];

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
    };

    // Fetch financial metrics
    const fetchMetrics = async (range = timeRange, startDate = null, endDate = null) => {
        setIsLoading(true);
        setError(null);

        try {
            const params = { range };

            if (range === 'CUSTOM' && startDate && endDate) {
                params.startDate = startDate;
                params.endDate = endDate;
            }

            const data = await analyticsService.getFinancialOverview(params);
            setMetrics(data.data);
        } catch (err) {
            console.error('Error fetching financial overview:', err);
            setError(err.message || 'Failed to load financial overview');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        if (isAdminOrManager) {
            fetchMetrics();
        } else {
            setError('Access Denied:  Admin or Manager role required');
            setIsLoading(false);
        }
    }, []);

    // Handle time range change
    const handleTimeRangeChange = (range) => {
        setTimeRange(range);

        if (range === 'CUSTOM') {
            setShowCustomDatePicker(true);
        } else {
            setShowCustomDatePicker(false);
            fetchMetrics(range);
        }
    };

    // Handle custom date apply
    const handleApplyCustomDates = () => {
        if (! customStartDate || !customEndDate) {
            alert('Please select both start and end dates');
            return;
        }

        if (new Date(customStartDate) > new Date(customEndDate)) {
            alert('Start date must be before end date');
            return;
        }

        fetchMetrics('CUSTOM', customStartDate, customEndDate);
    };

    // Format currency
    const formatCurrency = (value) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
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

    // Get profit margin color
    const getProfitMarginColor = (margin) => {
        if (margin >= 30) return chartColors.success;
        if (margin >= 20) return chartColors.info;
        if (margin >= 10) return chartColors.warning;
        return chartColors.error;
    };

    // Revenue vs Loss Chart (Bar)
    const revenueVsLossChartData = {
        labels:  ['Total Revenue', 'Loss Value', 'Net Profit'],
        datasets: [
            {
                label: 'Amount ($)',
                data: [
                    metrics?.totalRevenue || 0,
                    metrics?.lossValue || 0,
                    metrics?.netProfit || 0,
                ],
                backgroundColor: [
                    chartColors.success,
                    chartColors.error,
                    chartColors.primary,
                ],
                borderRadius: 8,
                barThickness: 60,
            },
        ],
    };

    const revenueVsLossChartOptions = {
        responsive:  true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: false,
            },
            tooltip: {
                callbacks: {
                    label: function (context) {
                        return `${context.label}: ${formatCurrency(context.parsed.y)}`;
                    },
                },
            },
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks:  {
                    callback: function (value) {
                        return '$' + value;
                    },
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

    // Revenue Breakdown Pie Chart
    const revenueBreakdownChartData = {
        labels:  ['Net Profit', 'Loss'],
        datasets: [
            {
                data: [
                    metrics?.netProfit || 0,
                    metrics?.lossValue || 0,
                ],
                backgroundColor: [chartColors.primary, chartColors.error],
                borderColor: ['#fff', '#fff'],
                borderWidth: 3,
            },
        ],
    };

    const revenueBreakdownChartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend:  {
                position: 'bottom',
                labels: {
                    padding: 15,
                    font: {
                        size: 12,
                        weight: '600',
                    },
                },
            },
            tooltip:  {
                callbacks: {
                    label: function (context) {
                        const label = context.label || '';
                        const value = context.parsed || 0;
                        const percentage = ((value / metrics?.totalRevenue) * 100).toFixed(1);
                        return `${label}: ${formatCurrency(value)} (${percentage}%)`;
                    },
                },
            },
        },
    };

    // Daily Average Chart (Line - simulated data based on avg)
    const generateDailyData = () => {
        const days = metrics?.daysInPeriod || 0;
        const avgRevenue = metrics?.avgRevenuePerDay || 0;

        const data = [];
        for (let i = 1; i <= Math.min(days, 30); i++) {
            // Simulate variation around average (±20%)
            const variation = (Math.random() - 0.5) * 0.4;
            data.push(avgRevenue * (1 + variation));
        }
        return data;
    };

    const dailyRevenueChartData = {
        labels:  Array.from({ length: Math.min(metrics?.daysInPeriod || 0, 30) }, (_, i) => `Day ${i + 1}`),
        datasets: [
            {
                label:  'Daily Revenue',
                data: generateDailyData(),
                borderColor: chartColors.primary,
                backgroundColor: 'rgba(25, 118, 210, 0.1)',
                tension: 0.4,
                fill: true,
                pointRadius: 4,
                pointHoverRadius: 6,
            },
        ],
    };

    const dailyRevenueChartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins:  {
            legend: {
                display: false,
            },
            tooltip: {
                callbacks: {
                    label: function (context) {
                        return `Revenue: ${formatCurrency(context.parsed.y)}`;
                    },
                },
            },
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks:  {
                    callback: function (value) {
                        return '$' + value.toFixed(0);
                    },
                },
                grid: {
                    color: 'rgba(0, 0, 0, 0.05)',
                },
            },
            x: {
                grid: {
                    display:  false,
                },
                ticks: {
                    maxTicksLimit: 10,
                },
            },
        },
    };

    // Loading State
    if (isLoading) {
        return (
            <div className="loading-container">
                <div className="spinner-large"></div>
                <p>Loading financial overview...</p>
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
                <h2>Error Loading Financial Overview</h2>
                <p>{error}</p>
                {isAdminOrManager && (
                    <button className="retry-btn" onClick={() => fetchMetrics()}>
                        Try Again
                    </button>
                )}
            </div>
        );
    }

    return (
        <div className="financial-overview-page">
            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">💰 Financial Overview</h1>
                    <p className="page-subtitle">Track revenue, profit margins, and financial performance</p>
                </div>
                <button className="refresh-btn" onClick={() => fetchMetrics(timeRange, customStartDate, customEndDate)}>
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21.5 2V8M21.5 8H15.5M21.5 8L18 4.5C16.8 3.3 15.3 2.4 13.6 2C9.9 1 6.1 2.7 4.1 6C2.1 9.3 2.3 13.5 4.8 16.5C7.3 19.5 11.3 20.6 14.9 19.3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                    Refresh
                </button>
            </div>

            {/* Time Range Filter */}
            <div className="filter-section">
                <h3 className="filter-title">📅 Time Period</h3>
                <div className="time-range-buttons">
                    {timeRangeOptions.map((option) => (
                        <button
                            key={option.value}
                            className={`time-range-btn ${timeRange === option.value ? 'active' : ''}`}
                            onClick={() => handleTimeRangeChange(option.value)}
                        >
                            <span className="time-range-label">{option.label}</span>
                            <span className="time-range-desc">{option.description}</span>
                        </button>
                    ))}
                </div>

                {/* Custom Date Picker */}
                {showCustomDatePicker && (
                    <div className="custom-date-picker">
                        <div className="date-input-group">
                            <label className="date-label">Start Date</label>
                            <input
                                type="date"
                                className="date-input"
                                value={customStartDate}
                                onChange={(e) => setCustomStartDate(e.target.value)}
                            />
                        </div>
                        <div className="date-input-group">
                            <label className="date-label">End Date</label>
                            <input
                                type="date"
                                className="date-input"
                                value={customEndDate}
                                onChange={(e) => setCustomEndDate(e.target.value)}
                            />
                        </div>
                        <button className="apply-dates-btn" onClick={handleApplyCustomDates}>
                            Apply
                        </button>
                    </div>
                )}

                {/* Period Display */}
                <div className="period-display">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" strokeWidth="2"/>
                        <path d="M16 2V6M8 2V6M3 10H21" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                    </svg>
                    <span>
                        {formatDate(metrics?.periodStart)} - {formatDate(metrics?.periodEnd)}
                        <span className="period-days"> ({metrics?.daysInPeriod} days)</span>
                    </span>
                </div>
            </div>

            {/* Metrics Cards Grid */}
            <div className="metrics-grid-financial">
                {/* Total Revenue */}
                <div className="metric-card-financial metric-success">
                    <div className="metric-icon-wrapper-financial">
                        <svg className="metric-icon-financial" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 2V22M17 5H9.5C8.57174 5 7.6815 5.36875 7.02513 6.02513C6.36875 6.6815 6 7.57174 6 8.5C6 9.42826 6.36875 10.3185 7.02513 10.9749C7.6815 11.6313 8.57174 12 9.5 12H14.5C15.4283 12 16.3185 12.3687 16.9749 13.0251C17.6313 13.6815 18 14.5717 18 15.5C18 16.4283 17.6313 17.3185 16.9749 17.9749C16.3185 18.6313 15.4283 19 14.5 19H6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-financial">
                        <p className="metric-label-financial">Total Revenue</p>
                        <h2 className="metric-value-financial">{formatCurrency(metrics?.totalRevenue)}</h2>
                        <p className="metric-sublabel-financial">Sales revenue</p>
                    </div>
                </div>

                {/* Average Order Value */}
                <div className="metric-card-financial metric-info">
                    <div className="metric-icon-wrapper-financial">
                        <svg className="metric-icon-financial" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M16 11V7C16 5.89543 15.1046 5 14 5H10C8.89543 5 8 5.89543 8 7V11" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            <rect x="3" y="9" width="18" height="13" rx="2" stroke="currentColor" strokeWidth="2"/>
                            <path d="M12 14V17" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-financial">
                        <p className="metric-label-financial">Avg Order Value</p>
                        <h2 className="metric-value-financial">{formatCurrency(metrics?.avgOrderValue)}</h2>
                        <p className="metric-sublabel-financial">Per order</p>
                    </div>
                </div>

                {/* Loss Value */}
                <div className="metric-card-financial metric-error">
                    <div className="metric-icon-wrapper-financial">
                        <svg className="metric-icon-financial" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M10.29 3.86L1.82 18C1.64537 18.3024 1.55296 18.6453 1.55199 18.9945C1.55101 19.3437 1.64151 19.6871 1.81445 19.9905C1.98738 20.2939 2.23675 20.5467 2.53773 20.7239C2.83871 20.901 3.18082 20.9962 3.53 21H20.47C20.8192 20.9962 21.1613 20.901 21.4623 20.7239C21.7633 20.5467 22.0126 20.2939 22.1856 19.9905C22.3585 19.6871 22.449 19.3437 22.448 18.9945C22.447 18.6453 22.3546 18.3024 22.18 18L13.71 3.86C13.5317 3.56611 13.2807 3.32312 12.9812 3.15448C12.6817 2.98585 12.3437 2.89725 12 2.89725C11.6563 2.89725 11.3183 2.98585 11.0188 3.15448C10.7193 3.32312 10.4683 3.56611 10.29 3.86Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M12 9V13" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            <circle cx="12" cy="17" r="1" fill="currentColor"/>
                        </svg>
                    </div>
                    <div className="metric-content-financial">
                        <p className="metric-label-financial">Loss Value</p>
                        <h2 className="metric-value-financial">{formatCurrency(metrics?.lossValue)}</h2>
                        <p className="metric-sublabel-financial">{metrics?.lossPercentage?.toFixed(1)}% of revenue</p>
                    </div>
                </div>

                {/* Profit Margin */}
                <div className="metric-card-financial metric-purple">
                    <div className="metric-icon-wrapper-financial">
                        <svg className="metric-icon-financial" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-financial">
                        <p className="metric-label-financial">Profit Margin</p>
                        <h2 className="metric-value-financial" style={{ color: getProfitMarginColor(metrics?.profitMargin) }}>
                            {metrics?.profitMargin?.toFixed(2)}%
                        </h2>
                        <p className="metric-sublabel-financial">Margin percentage</p>
                    </div>
                </div>

                {/* Net Profit */}
                <div className="metric-card-financial metric-primary">
                    <div className="metric-icon-wrapper-financial">
                        <svg className="metric-icon-financial" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-financial">
                        <p className="metric-label-financial">Net Profit</p>
                        <h2 className="metric-value-financial">{formatCurrency(metrics?.netProfit)}</h2>
                        <p className="metric-sublabel-financial">After losses</p>
                    </div>
                </div>

                {/* Avg Revenue Per Day */}
                <div className="metric-card-financial metric-teal">
                    <div className="metric-icon-wrapper-financial">
                        <svg className="metric-icon-financial" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" strokeWidth="2"/>
                            <path d="M16 2V6M8 2V6M3 10H21" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            <path d="M8 14H8.01M12 14H12.01M16 14H16.01M8 18H8.01M12 18H12.01M16 18H16.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-financial">
                        <p className="metric-label-financial">Avg Revenue/Day</p>
                        <h2 className="metric-value-financial">{formatCurrency(metrics?.avgRevenuePerDay)}</h2>
                        <p className="metric-sublabel-financial">Daily average</p>
                    </div>
                </div>
            </div>

            {/* Charts Section */}
            <div className="charts-section-financial">
                {/* Revenue vs Loss Chart */}
                <div className="chart-card-financial chart-large">
                    <div className="chart-header">
                        <h3 className="chart-title">📊 Revenue Breakdown</h3>
                        <span className="chart-subtitle">Revenue, Loss & Net Profit comparison</span>
                    </div>
                    <div className="chart-wrapper-financial">
                        <Bar data={revenueVsLossChartData} options={revenueVsLossChartOptions} />
                    </div>
                </div>

                {/* Revenue Breakdown Pie */}
                <div className="chart-card-financial">
                    <div className="chart-header">
                        <h3 className="chart-title">Profit vs Loss</h3>
                        <span className="chart-subtitle">Distribution percentage</span>
                    </div>
                    <div className="chart-wrapper-financial">
                        <Pie data={revenueBreakdownChartData} options={revenueBreakdownChartOptions} />
                    </div>
                </div>
            </div>

            {/* Daily Revenue Trend */}
            <div className="chart-card-financial chart-full">
                <div className="chart-header">
                    <h3 className="chart-title">📈 Daily Revenue Trend</h3>
                    <span className="chart-subtitle">Estimated daily revenue pattern (based on average)</span>
                </div>
                <div className="chart-wrapper-financial chart-tall">
                    <Line data={dailyRevenueChartData} options={dailyRevenueChartOptions} />
                </div>
                <div className="chart-note">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                        <path d="M12 16V12M12 8H12.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                    </svg>
                    <span>Note: Daily data is simulated based on average revenue per day</span>
                </div>
            </div>
        </div>
    );
};

export default FinancialOverview;