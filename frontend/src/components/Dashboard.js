import React, { useState, useEffect } from 'react';
import { 
    fetchPendingSuggestions, 
    approveSuggestion, 
    rejectSuggestion, 
    markAgentOffline, 
    fetchAgents, 
    toggleStrategy, 
    fetchActiveStrategy 
} from '../services/api';
import SuggestionCard from './SuggestionCard';

const Dashboard = () => {
    const [suggestions, setSuggestions] = useState([]);
    const [agents, setAgents] = useState([]);
    const [activeStrategy, setActiveStrategy] = useState('RULE');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [offlineReason, setOfflineReason] = useState('Bike Breakdown');

    useEffect(() => {
        loadDashboardData();
        const interval = setInterval(loadDashboardData, 5000);
        return () => clearInterval(interval);
    }, []);

    const loadDashboardData = async () => {
        try {
            const [suggs, agts, strat] = await Promise.all([
                fetchPendingSuggestions().catch(() => []),
                fetchAgents().catch(() => []),
                fetchActiveStrategy().catch(() => ({ activeStrategy: 'RULE' }))
            ]);
            setSuggestions(suggs || []);
            setAgents(agts || []);
            if (strat && strat.activeStrategy) {
                setActiveStrategy(strat.activeStrategy);
            }
            setError(null);
        } catch (err) {
            setError('Failed to load dashboard data. Ensure backend is running.');
        }
    };

    const handleApprove = async (id) => {
        try {
            await approveSuggestion(id);
            await loadDashboardData();
        } catch (err) {
            setError('Failed to approve suggestion');
        }
    };

    const handleReject = async (id) => {
        try {
            await rejectSuggestion(id);
            await loadDashboardData();
        } catch (err) {
            setError('Failed to reject suggestion');
        }
    };

    const handleMarkOffline = async (agentId) => {
        setLoading(true);
        try {
            await markAgentOffline(agentId, offlineReason);
            await loadDashboardData();
        } catch (err) {
            setError('Failed to mark agent offline');
        } finally {
            setLoading(false);
        }
    };

    const handleStrategyChange = async (e) => {
        const newStrategy = e.target.value;
        try {
            await toggleStrategy(newStrategy);
            setActiveStrategy(newStrategy);
        } catch (err) {
            setError('Failed to toggle strategy');
        }
    };

    return (
        <div className="dashboard">
            <header className="dashboard-header">
                <h1>React Ops Dashboard</h1>
                <div className="strategy-toggle">
                    <label>Active Strategy: </label>
                    <select value={activeStrategy} onChange={handleStrategyChange}>
                        <option value="RULE">Rule-Based</option>
                        <option value="AI">AI-Powered</option>
                    </select>
                </div>
            </header>

            {error && <div className="error-message">{error}</div>}

            <div className="dashboard-content">
                <section className="simulation-section">
                    <h2>Offline Simulation</h2>
                    <div className="agent-list">
                        {agents.length === 0 && <p>No agents found or API not connected.</p>}
                        {agents.map(agent => (
                            <div key={agent.id} className={`agent-row ${agent.status.toLowerCase()}`}>
                                <span>{agent.name} ({agent.status}) - Orders: {agent.activeOrderCount}</span>
                                {agent.status !== 'OFFLINE' && (
                                    <button 
                                        disabled={loading} 
                                        onClick={() => handleMarkOffline(agent.id)}
                                        className="btn-offline"
                                    >
                                        Mark Offline
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>
                    <div className="offline-reason">
                        <label>Reason: </label>
                        <input 
                            type="text" 
                            value={offlineReason} 
                            onChange={(e) => setOfflineReason(e.target.value)} 
                        />
                    </div>
                </section>

                <section className="suggestions-section">
                    <h2>Pending Reassignments ({suggestions.length})</h2>
                    <div className="suggestions-grid">
                        {suggestions.length === 0 && <p>No pending suggestions.</p>}
                        {suggestions.map(sugg => (
                            <SuggestionCard 
                                key={sugg.id} 
                                suggestion={sugg} 
                                onApprove={handleApprove} 
                                onReject={handleReject} 
                            />
                        ))}
                    </div>
                </section>
            </div>
        </div>
    );
};

export default Dashboard;
