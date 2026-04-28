-- ============================================================
-- MediCheck - Sample / Seed Data
-- Run this AFTER schema.sql
-- Passwords are BCrypt hashes of: Admin@123, Doctor@123, Pharma@123
-- ============================================================
USE medicheck;

-- ============================================================
-- ROLES
-- ============================================================
INSERT IGNORE INTO roles (id, name, description) VALUES
(1, 'Admin', 'Full system access'),
(2, 'Doctor', 'Clinical access - patients and prescriptions'),
(3, 'Pharmacist', 'Pharmacy access - inventory and billing');

-- ============================================================
-- USERS (passwords are bcrypt hashed)
-- Admin@123 | Doctor@123 | Pharma@123
-- ============================================================
INSERT IGNORE INTO users (id, username, full_name, email, password_hash, role_id, is_active) VALUES
(1, 'admin', 'System Administrator', 'admin@medicheck.com',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfVoqFG.KH.3YkS', 1, 1),
(2, 'dr.smith', 'Dr. John Smith', 'john.smith@medicheck.com',
 '$2a$12$8K1p/a0dR1xbLf9MMZW9Huw3nR5k.9DqA6SYpP3mOjLP0Nz2yCr5K', 2, 1),
(3, 'dr.patel', 'Dr. Priya Patel', 'priya.patel@medicheck.com',
 '$2a$12$8K1p/a0dR1xbLf9MMZW9Huw3nR5k.9DqA6SYpP3mOjLP0Nz2yCr5K', 2, 1),
(4, 'pharma1', 'Rahul Sharma', 'rahul.sharma@medicheck.com',
 '$2a$12$xLf9a0dR1xbMZLoP3mOjuwKnR5k.3YkSQv8/LewHf9pP3mOjLP0Nz', 3, 1);

-- ============================================================
-- DOCTORS
-- ============================================================
INSERT IGNORE INTO doctors (id, user_id, full_name, specialty, phone, email, license_no, qualification, experience_years) VALUES
(1, 2, 'Dr. John Smith', 'General Medicine', '+91-9876543210', 'john.smith@medicheck.com', 'MCI-2024-001', 'MBBS, MD (Medicine)', 12),
(2, 3, 'Dr. Priya Patel', 'Pediatrics', '+91-9876543211', 'priya.patel@medicheck.com', 'MCI-2024-002', 'MBBS, MD (Pediatrics)', 8),
(3, NULL, 'Dr. Arun Kumar', 'Cardiology', '+91-9876543212', 'arun.kumar@medicheck.com', 'MCI-2024-003', 'MBBS, MD, DM (Cardiology)', 15),
(4, NULL, 'Dr. Sunita Reddy', 'Gynecology', '+91-9876543213', 'sunita.reddy@medicheck.com', 'MCI-2024-004', 'MBBS, MS (OBG)', 10);

-- ============================================================
-- PATIENTS
-- ============================================================
INSERT IGNORE INTO patients (id, full_name, age, gender, phone, email, address, blood_group, disease, doctor_id) VALUES
(1, 'Amit Kumar', 35, 'Male', '+91-9812345601', 'amit.kumar@email.com', '12 MG Road, Bangalore', 'O+', 'Hypertension', 1),
(2, 'Sunita Devi', 28, 'Female', '+91-9812345602', 'sunita.devi@email.com', '45 Park Street, Mumbai', 'B+', 'Diabetes Type 2', 1),
(3, 'Ravi Shankar', 52, 'Male', '+91-9812345603', 'ravi.shankar@email.com', '7 Nehru Nagar, Delhi', 'A+', 'Cardiac Arrhythmia', 3),
(4, 'Meera Joshi', 42, 'Female', '+91-9812345604', 'meera.joshi@email.com', '23 Gandhi Road, Pune', 'AB+', 'Arthritis', 2),
(5, 'Arjun Singh', 8, 'Male', '+91-9812345605', 'parent@email.com', '56 Sector 12, Noida', 'O-', 'Asthma', 2),
(6, 'Kavya Reddy', 31, 'Female', '+91-9812345606', 'kavya.reddy@email.com', '89 Banjara Hills, Hyderabad', 'B-', 'Thyroid (Hypothyroid)', 4),
(7, 'Manoj Gupta', 60, 'Male', '+91-9812345607', 'manoj.gupta@email.com', '34 Civil Lines, Allahabad', 'A-', 'Type 2 Diabetes', 1),
(8, 'Priya Nair', 25, 'Female', '+91-9812345608', 'priya.nair@email.com', '12 Mavoor Road, Kozhikode', 'O+', 'Migraine', 1);

-- ============================================================
-- MEDICINES
-- ============================================================
INSERT IGNORE INTO medicines (id, name, generic_name, barcode, batch_no, category, price, cost_price, quantity, reorder_level, expiry_date, manufacturer, description, unit) VALUES
(1, 'Paracetamol 500mg', 'Acetaminophen', 'MED-0001', 'BATCH-2024-001', 'Analgesic', 12.50, 6.00, 500, 50, '2026-12-31', 'GSK Pharma', 'Used for fever and mild to moderate pain', 'Tablets'),
(2, 'Amoxicillin 500mg', 'Amoxicillin Trihydrate', 'MED-0002', 'BATCH-2024-002', 'Antibiotic', 45.00, 22.00, 200, 30, '2026-06-30', 'Cipla Ltd', 'Broad spectrum antibiotic', 'Capsules'),
(3, 'Metformin 500mg', 'Metformin HCl', 'MED-0003', 'BATCH-2024-003', 'Antidiabetic', 30.00, 14.00, 350, 40, '2026-09-30', 'Sun Pharma', 'For Type 2 Diabetes management', 'Tablets'),
(4, 'Amlodipine 5mg', 'Amlodipine Besylate', 'MED-0004', 'BATCH-2024-004', 'Antihypertensive', 65.00, 30.00, 120, 20, '2027-03-31', 'Ranbaxy', 'Calcium channel blocker for hypertension', 'Tablets'),
(5, 'Cetirizine 10mg', 'Cetirizine HCl', 'MED-0005', 'BATCH-2024-005', 'Antihistamine', 18.00, 8.00, 400, 50, '2026-11-30', 'Zydus Cadila', 'For allergies and hay fever', 'Tablets'),
(6, 'Omeprazole 20mg', 'Omeprazole', 'MED-0006', 'BATCH-2024-006', 'Antacid/PPI', 55.00, 25.00, 180, 25, '2026-08-31', 'Dr. Reddys', 'Proton pump inhibitor for acidity', 'Capsules'),
(7, 'Atorvastatin 10mg', 'Atorvastatin Calcium', 'MED-0007', 'BATCH-2024-007', 'Statin', 85.00, 40.00, 90, 15, '2027-01-31', 'Torrent Pharma', 'For high cholesterol management', 'Tablets'),
(8, 'Azithromycin 500mg', 'Azithromycin', 'MED-0008', 'BATCH-2024-008', 'Antibiotic', 95.00, 45.00, 8, 20, '2025-12-31', 'Lupin Ltd', 'Macrolide antibiotic', 'Tablets'),
(9, 'Salbutamol Inhaler', 'Salbutamol Sulphate', 'MED-0009', 'BATCH-2024-009', 'Bronchodilator', 220.00, 110.00, 45, 10, '2026-07-31', 'Cipla Ltd', 'Relief inhaler for asthma', 'Inhaler'),
(10, 'Levothyroxine 50mcg', 'Levothyroxine Sodium', 'MED-0010', 'BATCH-2024-010', 'Thyroid', 80.00, 38.00, 5, 15, '2025-11-30', 'Abbott', 'Thyroid hormone replacement', 'Tablets'),
(11, 'Vitamin D3 60000IU', 'Cholecalciferol', 'MED-0011', 'BATCH-2024-011', 'Supplement', 125.00, 60.00, 160, 20, '2026-10-31', 'Pfizer', 'Vitamin D3 supplement', 'Capsules'),
(12, 'Ibuprofen 400mg', 'Ibuprofen', 'MED-0012', 'BATCH-2024-012', 'NSAID', 22.00, 10.00, 300, 40, '2026-12-31', 'Wockhardt', 'Anti-inflammatory for pain and fever', 'Tablets'),
(13, 'Pantoprazole 40mg', 'Pantoprazole Sodium', 'MED-0013', 'BATCH-2024-013', 'Antacid/PPI', 48.00, 22.00, 210, 30, '2026-09-30', 'GlaxoSmithKline', 'Proton pump inhibitor', 'Tablets'),
(14, 'Clopidogrel 75mg', 'Clopidogrel Bisulfate', 'MED-0014', 'BATCH-2024-014', 'Antiplatelet', 110.00, 52.00, 75, 15, '2026-08-31', 'Sanofi', 'Antiplatelet for cardiac patients', 'Tablets'),
(15, 'ORS Sachets', 'Oral Rehydration Salts', 'MED-0015', 'BATCH-2024-015', 'Rehydration', 8.00, 3.50, 600, 100, '2027-06-30', 'Merck', 'For dehydration due to diarrhea/vomiting', 'Sachets');

-- ============================================================
-- PRESCRIPTIONS
-- ============================================================
INSERT IGNORE INTO prescriptions (id, prescription_no, patient_id, doctor_id, disease, symptoms, notes, status) VALUES
(1, 'RX-2024-0001', 1, 1, 'Hypertension', 'High blood pressure, headache', 'Monitor BP daily. Return if symptoms worsen.', 'Active'),
(2, 'RX-2024-0002', 2, 1, 'Type 2 Diabetes', 'High blood sugar, frequent urination', 'Dietary restrictions. Follow up in 2 weeks.', 'Dispensed'),
(3, 'RX-2024-0003', 5, 2, 'Asthma', 'Wheezing, shortness of breath', 'Use inhaler as needed. Avoid allergens.', 'Active');

-- ============================================================
-- PRESCRIPTION ITEMS
-- ============================================================
INSERT IGNORE INTO prescription_items (prescription_id, medicine_id, dosage, frequency, duration, quantity) VALUES
(1, 4, '5mg', 'Once daily', '30 days', 30),
(1, 1, '500mg', 'As needed for headache', '7 days', 10),
(2, 3, '500mg', 'Twice daily', '30 days', 60),
(2, 7, '10mg', 'Once daily at night', '30 days', 30),
(3, 9, '100mcg/dose', 'As needed', '1 month', 1);

-- ============================================================
-- SALES
-- ============================================================
INSERT IGNORE INTO sales (id, invoice_no, patient_id, patient_name, cashier_id, subtotal, tax_rate, tax_amount, discount, total, payment_method, payment_status) VALUES
(1, 'INV-2024-00001', 1, 'Amit Kumar', 4, 950.00, 18.00, 171.00, 50.00, 1071.00, 'Cash', 'Paid'),
(2, 'INV-2024-00002', 2, 'Sunita Devi', 4, 1650.00, 18.00, 297.00, 0.00, 1947.00, 'Card', 'Paid'),
(3, 'INV-2024-00003', NULL, 'Walk-in Customer', 4, 400.00, 18.00, 72.00, 0.00, 472.00, 'Cash', 'Paid');

-- ============================================================
-- SALE ITEMS
-- ============================================================
INSERT IGNORE INTO sale_items (sale_id, medicine_id, medicine_name, quantity, unit_price, total_price) VALUES
(1, 4, 'Amlodipine 5mg', 30, 65.00, 1950.00),
(1, 1, 'Paracetamol 500mg', 20, 12.50, 250.00),
(2, 3, 'Metformin 500mg', 60, 30.00, 1800.00),
(2, 7, 'Atorvastatin 10mg', 30, 85.00, 2550.00),
(3, 5, 'Cetirizine 10mg', 10, 18.00, 180.00),
(3, 12, 'Ibuprofen 400mg', 10, 22.00, 220.00);

-- ============================================================
-- SETTINGS (default values)
-- ============================================================
INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES
('pharmacy_name', 'MediCheck Pharmacy & Clinic', 'Name of the pharmacy/clinic'),
('pharmacy_address', '123 Health Street, Medical District, City - 560001', 'Pharmacy address'),
('pharmacy_phone', '+91-80-12345678', 'Contact phone number'),
('pharmacy_email', 'info@medicheck.com', 'Contact email'),
('pharmacy_gstin', '29AABCU9603R1ZX', 'GST Identification Number'),
('tax_rate', '18.0', 'Default tax rate in percentage'),
('currency_symbol', '₹', 'Currency symbol'),
('low_stock_threshold', '10', 'Global low stock alert threshold'),
('invoice_prefix', 'INV', 'Invoice number prefix'),
('prescription_prefix', 'RX', 'Prescription number prefix'),
('theme', 'dark', 'UI theme: dark or light'),
('openai_api_key', '', 'OpenAI API key for AI assistant (optional)'),
('openai_enabled', 'false', 'Whether OpenAI assistant is enabled'),
('backup_path', './backups', 'Path for database backups');
