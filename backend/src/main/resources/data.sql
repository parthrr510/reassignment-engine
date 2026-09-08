-- 5 agents: mix of AVAILABLE and BUSY
INSERT INTO agents (id, name, active_order_count, status) VALUES
('AGT-001', 'Priya Sharma', 2, 'BUSY'),
('AGT-002', 'Rahul Verma', 0, 'AVAILABLE'),
('AGT-003', 'Ananya Iyer', 3, 'BUSY'),
('AGT-004', 'Kiran Nair', 0, 'AVAILABLE'),
('AGT-005', 'Deepak Mehta', 3, 'BUSY');

-- 8 orders: all ASSIGNED, spread across agents
INSERT INTO orders (id, description, assigned_agent_id, status, created_at) VALUES
('ORD-001', 'Electronics - Koramangala to Indiranagar', 'AGT-001', 'ASSIGNED', CURRENT_TIMESTAMP()),
('ORD-002', 'Groceries - HSR Layout to BTM', 'AGT-001', 'ASSIGNED', CURRENT_TIMESTAMP()),
('ORD-003', 'Pharma - Whitefield to Marathahalli', 'AGT-003', 'ASSIGNED', CURRENT_TIMESTAMP()),
('ORD-004', 'Documents - MG Road to Jayanagar', 'AGT-005', 'ASSIGNED', CURRENT_TIMESTAMP()),
('ORD-005', 'Food - Bellandur to Electronic City', 'AGT-005', 'ASSIGNED', CURRENT_TIMESTAMP()),
('ORD-006', 'Apparel - Malleshwaram to Rajajinagar', 'AGT-005', 'ASSIGNED', CURRENT_TIMESTAMP()),
('ORD-007', 'Books - Banashankari to JP Nagar', 'AGT-003', 'ASSIGNED', CURRENT_TIMESTAMP()),
('ORD-008', 'Hardware - Peenya to Yeshwanthpur', 'AGT-003', 'ASSIGNED', CURRENT_TIMESTAMP());
