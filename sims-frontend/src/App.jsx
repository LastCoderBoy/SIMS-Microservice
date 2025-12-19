import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import MainLayout from './components/layout/MainLayout';
import InventoryDashboard from './pages/InventoryDashboard';
import DamageLoss from './pages/DamageLoss';
import LowStock from './pages/LowStock';
import TotalItems from './pages/TotalItems';
import IncomingStock from './pages/IncomingStock';
import OutgoingStock from './pages/OutgoingStock';

// Orders Management imports
import PurchaseOrders from './pages/PurchaseOrders';
import SalesOrders from './pages/SalesOrders';
import QrOrderTracker from './pages/QrOrderTracker';

// Reports & Analytics imports
import AnalyticsDashboard from './pages/reportAnalytics/AnalyticsDashboard';
import InventoryHealth from './pages/reportAnalytics/InventoryHealth';
import FinancialOverview from './pages/reportAnalytics/FinancialOverview';
import OrdersSummary from './pages/reportAnalytics/OrdersSummary';


import ProtectedRoute from './components/auth/ProtectedRoute';
import './App.css';

function App() {
    return (
        <Router>
            <Routes>
                {/* Public Routes */}
                <Route path="/login" element={<Login />} />

                {/* Protected Routes with Layout */}
                <Route
                    path="/"
                    element={
                        <ProtectedRoute>
                            <MainLayout />
                        </ProtectedRoute>
                    }
                >
                    {/* Dashboard - Default Route */}
                    <Route index element={<Navigate to="/dashboard" replace />} />
                    <Route path="dashboard" element={<InventoryDashboard />} />

                    {/* Inventory Control Sub-routes */}
                    <Route path="inventory/total-items" element={<TotalItems />} />
                    <Route path="inventory/low-stock" element={<LowStock />} />
                    <Route path="inventory/damage-loss" element={<DamageLoss />} />
                    <Route path="inventory/incoming-stock" element={<IncomingStock />} />
                    <Route path="inventory/outgoing-stock" element={<OutgoingStock />} />

                    {/* Order Management Sub-routes */}
                    <Route path="orders/purchase-orders" element={<PurchaseOrders />} />
                    <Route path="orders/sales-orders" element={<SalesOrders />} />
                    <Route path="orders/qr-tracker" element={<QrOrderTracker />} />


                    {/* Reports & Analytics Sub-routes */}
                    <Route path="analytics/dashboard" element={<AnalyticsDashboard />} />
                    <Route path="analytics/inventory-health" element={<InventoryHealth />} />
                    <Route path="analytics/financial-overview" element={<FinancialOverview />} />
                    <Route path="analytics/orders-summary" element={<OrdersSummary />} />

                    {/* User Profile */}
                    <Route path="profile" element={<div className="coming-soon">User Profile Page (Coming Soon)</div>} />
                </Route>

                {/* Catch all - Redirect to Dashboard */}
                <Route path="*" element={<Navigate to="/dashboard" replace />} />
            </Routes>
        </Router>
    );
}

export default App;