import React, { useState } from 'react';
import CustomerInterface from './components/CustomerInterface';
import BaristaDashboard from './components/BaristaDashboard';
import SimulationDashboard from './components/SimulationDashboard';
import { Coffee, Users, Activity } from 'lucide-react';

type View = 'customer' | 'barista' | 'simulation';

function App() {
    const [currentView, setCurrentView] = useState<View>('customer');

    return (
        <div className="min-h-screen">
            {/* Navigation */}
            <nav className="bg-primary text-white shadow-lg">
                <div className="max-w-7xl mx-auto px-4">
                    <div className="flex items-center justify-between h-16">
                        <div className="flex items-center space-x-2">
                            <Coffee className="w-8 h-8" />
                            <span className="text-xl font-bold">Bean & Brew Queue System</span>
                        </div>
                        <div className="flex space-x-4">
                            <button
                                onClick={() => setCurrentView('customer')}
                                className={`px-4 py-2 rounded-lg flex items-center space-x-2 transition-colors ${currentView === 'customer'
                                        ? 'bg-white text-primary'
                                        : 'hover:bg-secondary'
                                    }`}
                            >
                                <Coffee className="w-5 h-5" />
                                <span>Customer</span>
                            </button>
                            <button
                                onClick={() => setCurrentView('barista')}
                                className={`px-4 py-2 rounded-lg flex items-center space-x-2 transition-colors ${currentView === 'barista'
                                        ? 'bg-white text-primary'
                                        : 'hover:bg-secondary'
                                    }`}
                            >
                                <Users className="w-5 h-5" />
                                <span>Barista</span>
                            </button>
                            <button
                                onClick={() => setCurrentView('simulation')}
                                className={`px-4 py-2 rounded-lg flex items-center space-x-2 transition-colors ${currentView === 'simulation'
                                        ? 'bg-white text-primary'
                                        : 'hover:bg-secondary'
                                    }`}
                            >
                                <Activity className="w-5 h-5" />
                                <span>Test Simulation</span>
                            </button>
                        </div>
                    </div>
                </div>
            </nav>

            {/* Content */}
            <div>
                {currentView === 'customer' && <CustomerInterface />}
                {currentView === 'barista' && <BaristaDashboard />}
                {currentView === 'simulation' && <SimulationDashboard />}
            </div>
        </div>
    );
}

export default App;