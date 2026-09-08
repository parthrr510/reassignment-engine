import React from 'react';
import './Dashboard.css';

const SuggestionCard = ({ suggestion, onApprove, onReject }) => {
    return (
        <div className="suggestion-card">
            <h3>Suggestion #{suggestion.id}</h3>
            <p><strong>Affected Order:</strong> {suggestion.orderId}</p>
            <p><strong>Recommended Agent:</strong> {suggestion.proposedAgentId}</p>
            <p><strong>Confidence:</strong> {suggestion.confidenceScore}%</p>
            <p className="reasoning"><strong>AI Reasoning:</strong> {suggestion.reasoning}</p>
            
            <div className="actions">
                <button className="btn-approve" onClick={() => onApprove(suggestion.id)}>Approve</button>
                <button className="btn-reject" onClick={() => onReject(suggestion.id)}>Reject</button>
            </div>
        </div>
    );
};

export default SuggestionCard;
