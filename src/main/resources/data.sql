-- ==========================================
-- Urimaigal Seed Data
-- ==========================================

INSERT IGNORE INTO lawyers (id, name, avatar, specialization, experience, rating, review_count, location, languages, consultation_fee, bio, available, badge) VALUES
('l1', 'Priya Venkataraman',
 'https://api.dicebear.com/7.x/avataaars/svg?seed=Priya&backgroundColor=b6e3f4',
 'Criminal', 12, 4.9, 234, 'Chennai', 'Tamil,English,Hindi', 1500.00,
 'Senior advocate at Madras High Court with 12 years handling criminal defense, bail matters, and appeals. Known for thorough case preparation and client communication.',
 1, 'Top Rated'),

('l2', 'Arjun Subramaniam',
 'https://api.dicebear.com/7.x/avataaars/svg?seed=Arjun&backgroundColor=c0aede',
 'Corporate', 8, 4.7, 189, 'Chennai', 'Tamil,English', 2000.00,
 'Corporate law specialist focused on startup legal structuring, M&A due diligence, and commercial contracts. Former in-house counsel at a Fortune 500.',
 1, 'Rising Star'),

('l3', 'Meena Krishnamurthy',
 'https://api.dicebear.com/7.x/avataaars/svg?seed=Meena&backgroundColor=ffd5dc',
 'Family', 15, 4.8, 312, 'Coimbatore', 'Tamil,English,Telugu', 1200.00,
 'Specializes in family law matters — divorce, child custody, maintenance, and domestic violence cases. Compassionate approach with strong courtroom presence.',
 0, 'Top Rated'),

('l4', 'Ravi Chandrasekaran',
 'https://api.dicebear.com/7.x/avataaars/svg?seed=Ravi&backgroundColor=d1f4e0',
 'Civil', 20, 4.6, 401, 'Madurai', 'Tamil,English', 1000.00,
 'Two decades of experience in civil disputes — property matters, injunctions, and recovery suits. Extensively practiced before the Sub-courts and District Courts.',
 1, NULL),

('l5', 'Divya Rajagopal',
 'https://api.dicebear.com/7.x/avataaars/svg?seed=Divya&backgroundColor=ffdfbf',
 'Intellectual Property', 6, 4.5, 98, 'Chennai', 'Tamil,English', 2500.00,
 'IP attorney specializing in trademarks, patents, and copyright for tech startups and creative professionals. Registered Patent Agent with the Indian Patent Office.',
 1, 'Rising Star'),

('l6', 'Senthil Nathan',
 'https://api.dicebear.com/7.x/avataaars/svg?seed=Senthil&backgroundColor=b6e3f4',
 'Labour', 10, 4.4, 156, 'Tirupur', 'Tamil,English', 800.00,
 'Labour law practitioner handling employee disputes, wrongful termination, POSH matters, and trade union issues. Strong advocate for workers rights.',
 1, 'Pro Bono'),

('l7', 'Kavitha Annamalai',
 'https://api.dicebear.com/7.x/avataaars/svg?seed=Kavitha&backgroundColor=c0aede',
 'Immigration', 9, 4.7, 203, 'Chennai', 'Tamil,English,French', 3000.00,
 'Immigration specialist handling work visas, PR applications, OCI, and student visa matters across US, Canada, UK, and EU destinations.',
 1, NULL),

('l8', 'Balamurugan Pillai',
 'https://api.dicebear.com/7.x/avataaars/svg?seed=Bala&backgroundColor=ffd5dc',
 'Public Interest', 18, 4.9, 87, 'Chennai', 'Tamil,English,Malayalam', 0.00,
 'Public interest litigator with 18 years of PIL and constitutional law experience. Committed to social justice, environmental law, and rights of marginalized communities.',
 1, 'Pro Bono');
