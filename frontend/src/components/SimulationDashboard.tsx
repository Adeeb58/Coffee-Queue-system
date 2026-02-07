import React, { useState, useEffect, useCallback } from 'react';
import { Play, RotateCcw, Activity, TrendingUp } from 'lucide-react';
import { BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface TestMetrics {
    totalOrders: number;
    pendingOrders: number;
    inProgressOrders: number;
    completedOrders: number;
    avgWaitTime: number;
    maxWaitTime: number;
    minWaitTime: number;
    timeoutRate: number;
    baristaMetrics: {
        [key: string]: BaristaMetrics;
    };
    drinkDistribution: {
        [key: string]: number;
    };
}

interface BaristaMetrics {
    baristaName: string;
    currentWorkload: number;
    totalOrdersServed: number;
    pendingCount: number;
    inProgressCount: number;
    completedCount: number;
    ordersByDrinkType: {
        [key: string]: number;
    };
}

const SimulationDashboard: React.FC = () => {
    const [metrics, setMetrics] = useState<TestMetrics | null>(null);
    const [isRunning, setIsRunning] = useState(false);
    const [testProgress, setTestProgress] = useState(0);
    const [loading, setLoading] = useState(false);

    const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

    const fetchMetrics = useCallback(async () => {
        try {
            const response = await fetch(`${API_URL}/test/metrics`);
            const data = await response.json();
            setMetrics(data);

            // Calculate progress
            if (data.totalOrders > 0) {
                const progress = (data.completedOrders / data.totalOrders) * 100;
                setTestProgress(progress);
            }
        } catch (error) {
            console.error('Error fetching metrics:', error);
        }
    }, [API_URL]);

    // Fetch metrics periodically
    useEffect(() => {
        let interval: NodeJS.Timeout;
        if (isRunning) {
            interval = setInterval(fetchMetrics, 2000);
        }
        return () => {
            if (interval) clearInterval(interval);
        };
    }, [isRunning, fetchMetrics]);

    const generateTestOrders = async () => {
        setLoading(true);
        try {
            const response = await fetch(`${API_URL}/test/generate`, {
                method: 'POST',
            });
            const message = await response.text();
            alert(message);

            // Auto-fetch metrics after generation
            await fetchMetrics();

            // Auto-start monitoring
            setIsRunning(true);
        } catch (error) {
            console.error('Error generating test orders:', error);
            alert('Failed to generate test orders');
        } finally {
            setLoading(false);
        }
    };

    const clearTestData = async () => {
        if (!window.confirm('Are you sure you want to clear all test data?')) {
            return;
        }

        setLoading(true);
        try {
            const response = await fetch(`${API_URL}/test/clear`, {
                method: 'DELETE',
            });
            const message = await response.text();
            alert(message);
            setMetrics(null);
            setTestProgress(0);
            setIsRunning(false);
        } catch (error) {
            console.error('Error clearing test data:', error);
            alert('Failed to clear test data');
        } finally {
            setLoading(false);
        }
    };

    // Prepare data for charts
    const getBaristaWorkloadData = () => {
        if (!metrics?.baristaMetrics) return [];

        return Object.values(metrics.baristaMetrics).map(barista => ({
            name: barista.baristaName,
            workload: barista.currentWorkload,
            completed: barista.completedCount,
            inProgress: barista.inProgressCount,
            pending: barista.pendingCount,
        }));
    };

    const getDrinkDistributionData = () => {
        if (!metrics?.drinkDistribution) return [];

        return Object.entries(metrics.drinkDistribution).map(([name, value]) => ({
            name,
            value,
        }));
    };

    const getOrderStatusData = () => {
        if (!metrics) return [];

        return [
            { name: 'Pending', value: metrics.pendingOrders, color: '#FFA500' },
            { name: 'In Progress', value: metrics.inProgressOrders, color: '#4169E1' },
            { name: 'Completed', value: metrics.completedOrders, color: '#32CD32' },
        ];
    };

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="bg-white rounded-lg shadow-lg p-6 mb-6">
                    <div className="flex items-center justify-between mb-4">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-800 flex items-center">
                                <Activity className="mr-3 w-8 h-8 text-primary" />
                                Test Simulation Dashboard
                            </h1>
                            <p className="text-gray-600 mt-2">
                                Generate and analyze 100+ orders with real-time metrics
                            </p>
                        </div>
                    </div>

                    {/* Control Buttons */}
                    <div className="flex space-x-4">
                        <button
                            onClick={generateTestOrders}
                            disabled={loading}
                            className="bg-primary text-white px-6 py-3 rounded-lg font-bold hover:bg-secondary transition-colors disabled:bg-gray-400 flex items-center space-x-2"
                        >
                            <Play className="w-5 h-5" />
                            <span>{loading ? 'Generating...' : 'Generate 100 Orders'}</span>
                        </button>

                        <button
                            onClick={clearTestData}
                            disabled={loading}
                            className="bg-red-500 text-white px-6 py-3 rounded-lg font-bold hover:bg-red-600 transition-colors disabled:bg-gray-400 flex items-center space-x-2"
                        >
                            <RotateCcw className="w-5 h-5" />
                            <span>Clear Data</span>
                        </button>
                    </div>

                    {/* Progress Bar */}
                    {metrics && (
                        <div className="mt-6">
                            <div className="flex justify-between text-sm mb-2">
                                <span className="font-medium">Test Progress</span>
                                <span className="text-gray-600">
                                    {metrics.completedOrders} / {metrics.totalOrders} orders completed
                                </span>
                            </div>
                            <div className="w-full bg-gray-200 rounded-full h-4">
                                <div
                                    className="bg-green-500 rounded-full h-4 transition-all duration-500"
                                    style={{ width: `${testProgress}%` }}
                                />
                            </div>
                        </div>
                    )}
                </div>

                {metrics && (
                    <>
                        {/* Summary Stats */}
                        <div className="grid grid-cols-4 gap-4 mb-6">
                            <div className="bg-white rounded-lg shadow p-6">
                                <div className="text-sm text-gray-600 mb-1">Total Orders</div>
                                <div className="text-3xl font-bold text-gray-800">
                                    {metrics.totalOrders}
                                </div>
                            </div>

                            <div className="bg-white rounded-lg shadow p-6">
                                <div className="text-sm text-gray-600 mb-1">Avg Wait Time</div>
                                <div className="text-3xl font-bold text-blue-600">
                                    {metrics.avgWaitTime?.toFixed(1)} min
                                </div>
                            </div>

                            <div className="bg-white rounded-lg shadow p-6">
                                <div className="text-sm text-gray-600 mb-1">Max Wait Time</div>
                                <div className="text-3xl font-bold text-orange-600">
                                    {metrics.maxWaitTime} min
                                </div>
                            </div>

                            <div className="bg-white rounded-lg shadow p-6">
                                <div className="text-sm text-gray-600 mb-1">Timeout Rate</div>
                                <div className="text-3xl font-bold text-red-600">
                                    {metrics.timeoutRate?.toFixed(1)}%
                                </div>
                            </div>
                        </div>

                        {/* Barista Workload Analysis */}
                        <div className="bg-white rounded-lg shadow-lg p-6 mb-6">
                            <h2 className="text-2xl font-bold mb-4 flex items-center">
                                <TrendingUp className="mr-2" />
                                Barista Workload Analysis
                            </h2>

                            <div className="grid grid-cols-3 gap-6 mb-6">
                                {Object.values(metrics.baristaMetrics).map((barista) => (
                                    <div key={barista.baristaName} className="border-2 border-gray-200 rounded-lg p-4">
                                        <h3 className="font-bold text-lg mb-3">{barista.baristaName}</h3>

                                        {/* Workload Bar */}
                                        <div className="mb-4">
                                            <div className="flex justify-between text-sm mb-1">
                                                <span>Current Workload</span>
                                                <span className="font-bold">{barista.currentWorkload} min</span>
                                            </div>
                                            <div className="w-full bg-gray-200 rounded-full h-3">
                                                <div
                                                    className="bg-primary rounded-full h-3 transition-all"
                                                    style={{ width: `${Math.min((barista.currentWorkload / 30) * 100, 100)}%` }}
                                                />
                                            </div>
                                        </div>

                                        {/* Order Stats */}
                                        <div className="space-y-2 text-sm">
                                            <div className="flex justify-between">
                                                <span>Total Served:</span>
                                                <span className="font-bold text-green-600">{barista.totalOrdersServed}</span>
                                            </div>
                                            <div className="flex justify-between">
                                                <span>Completed:</span>
                                                <span className="font-bold text-green-600">{barista.completedCount}</span>
                                            </div>
                                            <div className="flex justify-between">
                                                <span>In Progress:</span>
                                                <span className="font-bold text-blue-600">{barista.inProgressCount}</span>
                                            </div>
                                            <div className="flex justify-between">
                                                <span>Pending:</span>
                                                <span className="font-bold text-orange-600">{barista.pendingCount}</span>
                                            </div>
                                        </div>

                                        {/* Drink Type Breakdown */}
                                        <div className="mt-4 pt-4 border-t border-gray-200">
                                            <div className="text-xs font-bold mb-2">Orders by Type:</div>
                                            {Object.entries(barista.ordersByDrinkType).map(([drink, count]) => (
                                                <div key={drink} className="flex justify-between text-xs py-1">
                                                    <span className="text-gray-600">{drink}:</span>
                                                    <span className="font-bold">{count}</span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                ))}
                            </div>

                            {/* Workload Comparison Chart */}
                            <div className="mt-6">
                                <h3 className="text-lg font-bold mb-4">Workload Comparison</h3>
                                <ResponsiveContainer width="100%" height={300}>
                                    <BarChart data={getBaristaWorkloadData()}>
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis dataKey="name" />
                                        <YAxis />
                                        <Tooltip />
                                        <Legend />
                                        <Bar dataKey="completed" stackId="a" fill="#32CD32" name="Completed" />
                                        <Bar dataKey="inProgress" stackId="a" fill="#4169E1" name="In Progress" />
                                        <Bar dataKey="pending" stackId="a" fill="#FFA500" name="Pending" />
                                    </BarChart>
                                </ResponsiveContainer>
                            </div>
                        </div>

                        {/* Charts Row */}
                        <div className="grid grid-cols-2 gap-6 mb-6">
                            {/* Order Status Distribution */}
                            <div className="bg-white rounded-lg shadow-lg p-6">
                                <h2 className="text-xl font-bold mb-4">Order Status Distribution</h2>
                                <ResponsiveContainer width="100%" height={300}>
                                    <PieChart>
                                        <Pie
                                            data={getOrderStatusData()}
                                            cx="50%"
                                            cy="50%"
                                            labelLine={false}
                                            label={({ name, value, percent }) =>
                                                `${name}: ${value} (${(percent * 100).toFixed(0)}%)`
                                            }
                                            outerRadius={100}
                                            fill="#8884d8"
                                            dataKey="value"
                                        >
                                            {getOrderStatusData().map((entry, index) => (
                                                <Cell key={`cell-${index}`} fill={entry.color} />
                                            ))}
                                        </Pie>
                                        <Tooltip />
                                    </PieChart>
                                </ResponsiveContainer>
                            </div>

                            {/* Drink Distribution */}
                            <div className="bg-white rounded-lg shadow-lg p-6">
                                <h2 className="text-xl font-bold mb-4">Drink Distribution</h2>
                                <ResponsiveContainer width="100%" height={300}>
                                    <BarChart data={getDrinkDistributionData()}>
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis dataKey="name" angle={-45} textAnchor="end" height={100} />
                                        <YAxis />
                                        <Tooltip />
                                        <Bar dataKey="value" fill="#8B4513" />
                                    </BarChart>
                                </ResponsiveContainer>
                            </div>
                        </div>

                        {/* Detailed Table */}
                        <div className="bg-white rounded-lg shadow-lg p-6">
                            <h2 className="text-2xl font-bold mb-4">Barista Performance Table</h2>
                            <div className="overflow-x-auto">
                                <table className="min-w-full table-auto">
                                    <thead className="bg-gray-100">
                                        <tr>
                                            <th className="px-4 py-3 text-left font-bold">Barista</th>
                                            <th className="px-4 py-3 text-center font-bold">Workload (min)</th>
                                            <th className="px-4 py-3 text-center font-bold">Total Served</th>
                                            <th className="px-4 py-3 text-center font-bold">Completed</th>
                                            <th className="px-4 py-3 text-center font-bold">In Progress</th>
                                            <th className="px-4 py-3 text-center font-bold">Pending</th>
                                            <th className="px-4 py-3 text-left font-bold">Top Drink</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {Object.values(metrics.baristaMetrics).map((barista, index) => {
                                            const topDrink = Object.entries(barista.ordersByDrinkType)
                                                .sort(([, a], [, b]) => b - a)[0];

                                            return (
                                                <tr key={index} className="border-b hover:bg-gray-50">
                                                    <td className="px-4 py-3 font-bold">{barista.baristaName}</td>
                                                    <td className="px-4 py-3 text-center">{barista.currentWorkload}</td>
                                                    <td className="px-4 py-3 text-center">{barista.totalOrdersServed}</td>
                                                    <td className="px-4 py-3 text-center text-green-600 font-bold">
                                                        {barista.completedCount}
                                                    </td>
                                                    <td className="px-4 py-3 text-center text-blue-600 font-bold">
                                                        {barista.inProgressCount}
                                                    </td>
                                                    <td className="px-4 py-3 text-center text-orange-600 font-bold">
                                                        {barista.pendingCount}
                                                    </td>
                                                    <td className="px-4 py-3">
                                                        {topDrink ? `${topDrink[0]} (${topDrink[1]})` : '-'}
                                                    </td>
                                                </tr>
                                            );
                                        })}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </>
                )}

                {!metrics && !loading && (
                    <div className="bg-white rounded-lg shadow-lg p-12 text-center">
                        <Activity className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                        <h3 className="text-xl font-bold text-gray-600 mb-2">
                            No Test Data Available
                        </h3>
                        <p className="text-gray-500 mb-6">
                            Click "Generate 100 Orders" to start the simulation
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default SimulationDashboard;