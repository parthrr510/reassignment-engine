const API_BASE_URL = 'http://localhost:8080/api';

export const fetchPendingSuggestions = async () => {
    const response = await fetch(`${API_BASE_URL}/suggestions/pending`);
    if (!response.ok) throw new Error('Failed to fetch suggestions');
    return response.json();
};

export const approveSuggestion = async (id) => {
    const response = await fetch(`${API_BASE_URL}/suggestions/${id}/approve`, {
        method: 'POST'
    });
    if (!response.ok) throw new Error('Failed to approve suggestion');
};

export const rejectSuggestion = async (id) => {
    const response = await fetch(`${API_BASE_URL}/suggestions/${id}/reject`, {
        method: 'POST'
    });
    if (!response.ok) throw new Error('Failed to reject suggestion');
};

export const markAgentOffline = async (id, reason) => {
    const response = await fetch(`${API_BASE_URL}/agents/${id}/offline`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ reason })
    });
    if (!response.ok) throw new Error('Failed to mark agent offline');
};

export const fetchAgents = async () => {
    const response = await fetch(`${API_BASE_URL}/agents`);
    if (!response.ok) throw new Error('Failed to fetch agents');
    return response.json();
};

export const toggleStrategy = async (strategy) => {
    const response = await fetch(`${API_BASE_URL}/strategy/active`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ strategy })
    });
    if (!response.ok) throw new Error('Failed to toggle strategy');
};

export const fetchActiveStrategy = async () => {
    const response = await fetch(`${API_BASE_URL}/strategy/active`);
    if (!response.ok) throw new Error('Failed to fetch strategy');
    return response.json();
};
