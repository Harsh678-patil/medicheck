-- ============================================================
-- MediCheck - FULL SETUP SCRIPT (Schema + Indian Sample Data)
-- Run this once in phpMyAdmin or MySQL CLI
-- ============================================================

-- Step 1: Drop & recreate database (clean slate)
DROP DATABASE IF EXISTS medicheck;
CREATE DATABASE medicheck CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE medicheck;

-- ============================================================
-- TABLES
-- ============================================================
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    is_active TINYINT(1) DEFAULT 1,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE IF NOT EXISTS doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    full_name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    license_no VARCHAR(50) UNIQUE,
    address TEXT,
    qualification VARCHAR(200),
    experience_years INT DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    age INT,
    gender ENUM('Male','Female','Other') DEFAULT 'Male',
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    blood_group VARCHAR(10),
    disease VARCHAR(255),
    allergies TEXT,
    doctor_id INT NULL,
    emergency_contact VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_patient_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS medicines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    generic_name VARCHAR(150),
    barcode VARCHAR(100) UNIQUE,
    batch_no VARCHAR(50),
    category VARCHAR(100),
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    cost_price DECIMAL(10,2) DEFAULT 0.00,
    quantity INT NOT NULL DEFAULT 0,
    reorder_level INT DEFAULT 10,
    expiry_date DATE,
    manufacturer VARCHAR(150),
    description TEXT,
    unit VARCHAR(50) DEFAULT 'Tablets',
    image_path VARCHAR(500),
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS prescriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    prescription_no VARCHAR(50) NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    disease VARCHAR(255),
    symptoms TEXT,
    notes TEXT,
    status ENUM('Active','Dispensed','Cancelled') DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_presc_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_presc_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

CREATE TABLE IF NOT EXISTS prescription_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    prescription_id INT NOT NULL,
    medicine_id INT NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    duration VARCHAR(100),
    quantity INT DEFAULT 1,
    notes VARCHAR(255),
    CONSTRAINT fk_pi_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE,
    CONSTRAINT fk_pi_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);

CREATE TABLE IF NOT EXISTS sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_no VARCHAR(50) NOT NULL UNIQUE,
    patient_id INT NULL,
    patient_name VARCHAR(100),
    cashier_id INT NOT NULL,
    prescription_id INT NULL,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_rate DECIMAL(5,2) DEFAULT 0.00,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    discount DECIMAL(10,2) DEFAULT 0.00,
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_method ENUM('Cash','Card','UPI','Insurance') DEFAULT 'Cash',
    payment_status ENUM('Paid','Pending','Refunded') DEFAULT 'Paid',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sale_cashier FOREIGN KEY (cashier_id) REFERENCES users(id),
    CONSTRAINT fk_sale_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE SET NULL,
    CONSTRAINT fk_sale_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS sale_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    medicine_id INT NOT NULL,
    medicine_name VARCHAR(150),
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_si_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    CONSTRAINT fk_si_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);

CREATE TABLE IF NOT EXISTS inventory_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id INT NOT NULL,
    medicine_name VARCHAR(150),
    action ENUM('RESTOCK','SALE','ADJUSTMENT','EXPIRED','DAMAGED') NOT NULL,
    quantity_before INT,
    quantity_change INT,
    quantity_after INT,
    notes TEXT,
    user_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(id),
    CONSTRAINT fk_inv_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    username VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id INT,
    details TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    description VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_medicines_barcode ON medicines(barcode);
CREATE INDEX idx_medicines_expiry ON medicines(expiry_date);
CREATE INDEX idx_medicines_quantity ON medicines(quantity);
CREATE INDEX idx_sales_created ON sales(created_at);
CREATE INDEX idx_sales_invoice ON sales(invoice_no);
CREATE INDEX idx_patients_name ON patients(full_name);
CREATE INDEX idx_doctors_name ON doctors(full_name);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);
CREATE INDEX idx_inventory_medicine ON inventory_logs(medicine_id);

-- ============================================================
-- SEED DATA: ROLES
-- ============================================================
INSERT IGNORE INTO roles (id, name, description) VALUES
(1, 'Admin', 'Full system access'),
(2, 'Doctor', 'Clinical access - patients and prescriptions'),
(3, 'Pharmacist', 'Inventory and billing access');

-- ============================================================
-- SEED DATA: USERS (passwords hashed with BCrypt)
-- admin      -> Admin@123
-- dr.krishnan -> Doctor@123
-- pharmrajeev -> Pharma@123
-- ============================================================
INSERT IGNORE INTO users (id, username, full_name, email, password_hash, role_id, is_active) VALUES
(1, 'admin', 'Suresh Administraror', 'admin@medicheck.in',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWa', 1, 1),
(2, 'dr.krishnan', 'Dr. Anand Krishnan', 'anand.krishnan@medicheck.in',
 '$2a$10$lInXaQFuMvivIRB2mlWqOua/5wjCIxTEizJxnqDkEZgBFajVN5rCK', 2, 1),
(3, 'pharmrajeev', 'Rajeev Pharmacist', 'rajeev.pharma@medicheck.in',
 '$2a$10$RxC5WYm1K8Z7CqMbAvn3c.mRCYGqiIIZr5VDekF4iQmWn5bhBR4yW', 3, 1);

-- ============================================================
-- SEED DATA: DOCTORS (Indian doctors)
-- ============================================================
INSERT IGNORE INTO doctors (id, full_name, specialty, phone, email, license_no, address, qualification, experience_years, is_active) VALUES
(1, 'Dr. Anand Krishnan', 'Cardiologist', '+91-98201-11111', 'anand.krishnan@medicheck.in', 'MCI-2001-KA-10234', 'Apollo Hospital, Banashankari, Bengaluru', 'MBBS, MD (Cardiology), DM', 22, 1),
(2, 'Dr. Sunita Agarwal', 'General Physician', '+91-98201-22222', 'sunita.agarwal@medicheck.in', 'MCI-2008-DL-45678', 'Fortis Hospital, Vasant Kunj, New Delhi', 'MBBS, PGDM (Family Medicine)', 15, 1),
(3, 'Dr. Rajan Mehta', 'Orthopedic Surgeon', '+91-98201-33333', 'rajan.mehta@medicheck.in', 'MCI-1998-MH-78901', 'AIIMS Hospital, Ansari Nagar, New Delhi', 'MBBS, MS (Orthopaedics)', 25, 1),
(4, 'Dr. Preethi Nambiar', 'Gynaecologist', '+91-98201-44444', 'preethi.nambiar@medicheck.in', 'MCI-2005-KL-23456', 'Manipal Hospital, HAL Old Airport Road, Bengaluru', 'MBBS, DGO, MD (OBG)', 18, 1),
(5, 'Dr. Suresh Rao', 'Neurologist', '+91-98201-55555', 'suresh.rao@medicheck.in', 'MCI-2003-AP-34567', 'Narayana Health, Bommasandra, Bengaluru', 'MBBS, MD, DM (Neurology)', 20, 1),
(6, 'Dr. Kavya Pillai', 'Endocrinologist', '+91-98201-66666', 'kavya.pillai@medicheck.in', 'MCI-2010-KL-56789', 'Aster Medcity, Kuttisaahib Road, Kochi', 'MBBS, MD (Internal Medicine), DM (Endocrinology)', 13, 1),
(7, 'Dr. Vivek Sharma', 'Pulmonologist', '+91-98201-77777', 'vivek.sharma@medicheck.in', 'MCI-2006-RJ-67890', 'Max Super Speciality Hospital, Saket, New Delhi', 'MBBS, MD (Pulmonary Medicine)', 17, 1),
(8, 'Dr. Devika Menon', 'Dermatologist', '+91-98201-88888', 'devika.menon@medicheck.in', 'MCI-2012-TN-12345', 'Sankara Nethralaya, College Road, Chennai', 'MBBS, MD (Dermatology)', 11, 1);

-- ============================================================
-- SEED DATA: PATIENTS (Indian patients)
-- ============================================================
INSERT IGNORE INTO patients (id, full_name, age, gender, phone, address, blood_group, disease, doctor_id) VALUES
(1, 'Rajesh Kumar Sharma', 45, 'Male', '+91-98201-34567', 'Sector 15, Rohini, New Delhi - 110085', 'B+', 'Hypertension, Type 2 Diabetes', 1),
(2, 'Priya Devi Menon', 32, 'Female', '+91-97301-22134', 'T Nagar, Chennai - 600017', 'O+', 'Hashimoto Thyroiditis', 2),
(3, 'Arun Patel', 58, 'Male', '+91-99012-87654', 'Navrangpura, Ahmedabad - 380009', 'A+', 'Lumbar Disc Herniation, Arthritis', 3),
(4, 'Sunita Yadav', 28, 'Female', '+91-80456-91234', 'Hazratganj, Lucknow - 226001', 'AB+', 'PCOS, Iron Deficiency Anaemia', 4),
(5, 'Mohammed Farhan Ali', 41, 'Male', '+91-70123-56789', 'Banjara Hills, Hyderabad - 500034', 'O-', 'Moderate Persistent Asthma, Sinusitis', 7),
(6, 'Kavitha Swaminathan', 55, 'Female', '+91-98765-43210', 'Jayanagar, Bengaluru - 560041', 'B-', 'Post-menopausal Osteoporosis', 6),
(7, 'Vikram Singh Chauhan', 37, 'Male', '+91-91234-78901', 'Vaishali Nagar, Jaipur - 302021', 'A-', 'Chronic Migraine with Aura', 5),
(8, 'Lakshmi Narayan Iyer', 63, 'Male', '+91-88901-23456', 'Worli, Mumbai - 400018', 'B+', 'Ischaemic Heart Disease, Hypertension', 1),
(9, 'Deepika Chowdary', 24, 'Female', '+91-77890-12345', 'Governorpet, Vijayawada - 520002', 'O+', 'Viral Fever, Dehydration', 2),
(10, 'Suresh Babu Reddy', 49, 'Male', '+91-96543-21098', 'Kothrud, Pune - 411038', 'AB-', 'Type 2 Diabetes Mellitus', 6),
(11, 'Ananya Krishnamurthy', 19, 'Female', '+91-99876-54321', 'Vijayanagar, Mysuru - 570017', 'A+', 'Viral Fever, Upper Respiratory Tract Infection', 2),
(12, 'Harpreet Kaur Singh', 34, 'Female', '+91-85234-67890', 'Lawrence Road, Amritsar - 143001', 'B+', 'Iron Deficiency Anaemia', 4),
(13, 'Ramesh Gupta', 67, 'Male', '+91-98432-10987', 'Sigra, Varanasi - 221001', 'O+', 'Chronic Kidney Disease Stage 3', 5),
(14, 'Nandini Pillai', 43, 'Female', '+91-70987-65432', 'Ernakulam, Kochi - 682016', 'A-', 'Fibromyalgia Syndrome', 6),
(15, 'Arjun Malhotra', 29, 'Male', '+91-91876-54320', 'Sector 22, Chandigarh - 160022', 'O+', 'Fracture Tibia, Sports Injury', 3);

-- ============================================================
-- SEED DATA: MEDICINES (Indian brands)
-- ============================================================
INSERT IGNORE INTO medicines (id, name, generic_name, barcode, category, price, cost_price, quantity, reorder_level, expiry_date, manufacturer, unit, is_active) VALUES
(1, 'Dolo 650', 'Paracetamol 650mg', 'MF10010001', 'Analgesic/Antipyretic', 32.50, 18.00, 450, 50, '2026-12-31', 'Micro Labs Ltd', 'Strip of 15 tablets', 1),
(2, 'Crocin 500', 'Paracetamol 500mg', 'GS10010002', 'Analgesic/Antipyretic', 28.00, 15.50, 320, 50, '2026-10-31', 'GlaxoSmithKline India', 'Strip of 20 tablets', 1),
(3, 'Combiflam', 'Ibuprofen 400mg + Paracetamol 325mg', 'SN10010003', 'NSAID', 45.00, 26.00, 210, 40, '2025-08-31', 'Sanofi India Ltd', 'Strip of 20 tablets', 1),
(4, 'Metformin 500', 'Metformin HCl 500mg', 'FP10010004', 'Antidiabetic', 65.00, 38.00, 180, 30, '2026-06-30', 'Franco-Indian Pharma', 'Strip of 10 tablets', 1),
(5, 'Amlodipine 5mg', 'Amlodipine Besylate 5mg', 'UA10010005', 'Antihypertensive', 55.00, 30.00, 80, 25, '2026-09-30', 'USV Ltd', 'Strip of 10 tablets', 1),
(6, 'Pan 40', 'Pantoprazole Sodium 40mg', 'SN10010006', 'Proton Pump Inhibitor', 72.00, 42.00, 150, 30, '2026-11-30', 'Sun Pharmaceutical Industries', 'Strip of 15 tablets', 1),
(7, 'Azithral 500', 'Azithromycin 500mg', 'CI10010007', 'Antibiotic - Macrolide', 110.00, 65.00, 95, 20, '2025-07-31', 'Cipla Ltd', 'Strip of 3 tablets', 1),
(8, 'Zyrtec 10mg', 'Cetirizine HCl 10mg', 'AL10010008', 'Antihistamine', 22.00, 12.00, 230, 40, '2026-12-31', 'Alembic Pharmaceuticals', 'Strip of 10 tablets', 1),
(9, 'Atorva 10', 'Atorvastatin Calcium 10mg', 'TP10010009', 'Lipid-Lowering Agent', 89.00, 52.00, 60, 20, '2026-08-31', 'Torrent Pharmaceuticals', 'Strip of 10 tablets', 1),
(10, 'Electral ORS', 'Oral Rehydration Salts (WHO formula)', 'FR10010010', 'Electrolyte Replenisher', 38.00, 20.00, 300, 50, '2025-12-31', 'Franco-Indian Pharma', 'Pack of 4 sachets (21.8g each)', 1),
(11, 'Asthalin Inhaler', 'Salbutamol Sulphate 100mcg/dose', 'CI10010011', 'Bronchodilator', 156.00, 95.00, 35, 10, '2026-03-31', 'Cipla Ltd', '200 doses per inhaler', 1),
(12, 'Amoxil 500', 'Amoxicillin Trihydrate 500mg', 'AL10010012', 'Antibiotic - Penicillin', 95.00, 56.00, 120, 25, '2025-09-30', 'Alembic Pharmaceuticals', 'Strip of 10 capsules', 1),
(13, 'Omez 20', 'Omeprazole 20mg', 'AS10010013', 'Proton Pump Inhibitor', 48.00, 27.00, 175, 35, '2026-07-31', 'Dr Reddys Laboratories', 'Strip of 15 capsules', 1),
(14, 'Losar 50', 'Losartan Potassium 50mg', 'GZ10010014', 'ARB Antihypertensive', 78.00, 45.00, 12, 25, '2026-05-31', 'Glenmark Pharmaceuticals', 'Strip of 10 tablets', 1),
(15, 'Glimperide 1mg', 'Glimepiride 1mg', 'SN10010015', 'Antidiabetic - Sulfonylurea', 62.00, 36.00, 8, 20, '2026-02-28', 'Sun Pharmaceutical Industries', 'Strip of 10 tablets', 1),
(16, 'Calcirol 60K', 'Cholecalciferol 60000IU (Vitamin D3)', 'AB10010016', 'Vitamin Supplement', 189.00, 110.00, 90, 15, '2027-01-31', 'Abbott India Ltd', 'Pack of 4 sachets', 1),
(17, 'Ferium XT', 'Ferrous Ascorbate 100mg + Folic Acid 1.5mg', 'PF10010017', 'Haematinic', 142.00, 82.00, 220, 40, '2026-10-31', 'Pfizer India Ltd', 'Strip of 10 tablets', 1),
(18, 'Voveran 50', 'Diclofenac Sodium 50mg', 'NV10010018', 'NSAID Anti-inflammatory', 35.00, 18.00, 140, 30, '2025-11-30', 'Novartis India Ltd', 'Strip of 10 tablets', 1),
(19, 'Montair 10', 'Montelukast Sodium 10mg', 'CI10010019', 'Leukotriene Receptor Antagonist', 135.00, 82.00, 55, 15, '2026-09-30', 'Cipla Ltd', 'Strip of 10 tablets', 1),
(20, 'Supradyn', 'Multivitamins, Minerals & Antioxidants', 'AB10010020', 'Nutritional Supplement', 220.00, 135.00, 110, 20, '2027-03-31', 'Abbott India Ltd', 'Strip of 15 effervescent tablets', 1);

-- ============================================================
-- SEED DATA: PRESCRIPTIONS
-- ============================================================
INSERT IGNORE INTO prescriptions (id, prescription_no, patient_id, doctor_id, disease, symptoms, status) VALUES
(1, 'RX-2024-001', 1, 1, 'Hypertension Stage 2', 'Persistent headache, dizziness, BP 160/100 mmHg', 'Active'),
(2, 'RX-2024-002', 2, 2, 'Hashimoto Thyroiditis', 'Fatigue, weight gain, cold intolerance, TSH elevated', 'Dispensed'),
(3, 'RX-2024-003', 3, 3, 'Lumbar Disc Herniation L4-L5', 'Lower back pain radiating to left leg, difficulty walking', 'Active'),
(4, 'RX-2024-004', 4, 4, 'PCOS with Insulin Resistance', 'Irregular periods, weight gain, acne, elevated androgens', 'Active'),
(5, 'RX-2024-005', 5, 7, 'Moderate Persistent Asthma', 'Wheezing, breathlessness on exertion, nocturnal cough', 'Dispensed'),
(6, 'RX-2024-006', 6, 6, 'Post-menopausal Osteoporosis', 'Back pain, height loss, T-score -2.8 on DEXA scan', 'Active'),
(7, 'RX-2024-007', 7, 5, 'Chronic Migraine with Aura', 'Severe throbbing headache, visual aura, nausea, photophobia', 'Active'),
(8, 'RX-2024-008', 8, 1, 'Ischaemic Heart Disease', 'Chest pain on exertion, dyspnoea, elevated troponin', 'Dispensed');

-- ============================================================
-- SEED DATA: PRESCRIPTION ITEMS
-- ============================================================
INSERT IGNORE INTO prescription_items (prescription_id, medicine_id, dosage, frequency, duration, quantity) VALUES
(1, 5, '5mg', 'Once daily (morning)', '30 days', 30),
(1, 4, '500mg', 'Twice daily (after meals)', '30 days', 60),
(2, 8, '10mg', 'Once daily at night', '30 days', 30),
(3, 18, '50mg', 'Twice daily after meals', '10 days', 20),
(3, 1, '650mg', 'Thrice daily if pain', '5 days', 15),
(4, 4, '500mg', 'Twice daily', '90 days', 180),
(4, 17, '1 tablet', 'Once daily', '90 days', 90),
(5, 11, '2 puffs', 'Every 4-6 hours as needed', '30 days', 1),
(5, 19, '10mg', 'Once daily at night', '30 days', 30),
(6, 16, '60000 IU', 'Once weekly', '8 weeks', 8),
(7, 1, '650mg', 'Twice daily as needed', '5 days', 10),
(8, 9, '10mg', 'Once daily at bedtime', '90 days', 90);

-- ============================================================
-- SEED DATA: SALES
-- ============================================================
INSERT IGNORE INTO sales (id, invoice_no, patient_id, patient_name, cashier_id, subtotal, tax_rate, tax_amount, discount, total, payment_method, payment_status) VALUES
(1,  'INV-2024-001', 1,    'Rajesh Kumar Sharma',    3, 271.00,  18.00,  48.78,  0.00,  319.78,   'Cash',      'Paid'),
(2,  'INV-2024-002', 2,    'Priya Devi Menon',       3, 668.00,  18.00, 120.24,  0.00,  788.24,   'UPI',       'Paid'),
(3,  'INV-2024-003', 3,    'Arun Patel',             3, 1055.00, 18.00, 189.90,  0.00,  1244.90,  'Card',      'Paid'),
(4,  'INV-2024-004', NULL, 'Walk-in Customer',       3, 132.00,  18.00,  23.76,  0.00,  155.76,   'Cash',      'Paid'),
(5,  'INV-2024-005', 4,    'Sunita Yadav',           3, 367.00,  18.00,  66.06,  0.00,  433.06,   'UPI',       'Paid'),
(6,  'INV-2024-006', 5,    'Mohammed Farhan Ali',    3, 838.00,  18.00, 150.84,  0.00,  988.84,   'Card',      'Paid'),
(7,  'INV-2024-007', 8,    'Lakshmi Narayan Iyer',   3, 1827.00, 18.00, 328.86, 50.00, 2105.86,   'Insurance', 'Paid'),
(8,  'INV-2024-008', NULL, 'Walk-in Customer',       3, 75.50,   18.00,  13.59,  0.00,   89.09,   'Cash',      'Paid'),
(9,  'INV-2024-009', 9,    'Deepika Chowdary',       3, 292.00,  18.00,  52.56,  0.00,  344.56,   'UPI',       'Paid'),
(10, 'INV-2024-010', 10,   'Suresh Babu Reddy',      3, 1602.00, 18.00, 288.36,  0.00, 1890.36,   'Card',      'Paid');


-- ============================================================
-- SEED DATA: SALE ITEMS
-- ============================================================
INSERT IGNORE INTO sale_items (sale_id, medicine_id, medicine_name, quantity, unit_price, total_price) VALUES
(1, 5, 'Amlodipine 5mg', 2, 55.00, 110.00),
(1, 4, 'Metformin 500', 3, 65.00, 195.00),
(2, 8, 'Zyrtec 10mg', 2, 22.00, 44.00),
(2, 13, 'Omez 20', 4, 48.00, 192.00),
(2, 16, 'Calcirol 60K', 2, 189.00, 378.00),
(2, 4, 'Metformin 500', 1, 65.00, 65.00),
(3, 18, 'Voveran 50', 2, 35.00, 70.00),
(3, 1, 'Dolo 650', 3, 32.50, 97.50),
(3, 9, 'Atorva 10', 5, 89.00, 445.00),
(3, 13, 'Omez 20', 3, 48.00, 144.00),
(3, 6, 'Pan 40', 4, 72.00, 288.00),
(4, 1, 'Dolo 650', 2, 32.50, 65.00),
(4, 10, 'Electral ORS', 1, 38.00, 38.00),
(5, 4, 'Metformin 500', 2, 65.00, 130.00),
(5, 17, 'Ferium XT', 1, 142.00, 142.00),
(5, 8, 'Zyrtec 10mg', 2, 22.00, 44.00),
(6, 11, 'Asthalin Inhaler', 2, 156.00, 312.00),
(6, 19, 'Montair 10', 3, 135.00, 405.00),
(6, 20, 'Supradyn', 1, 220.00, 220.00),
(7, 9, 'Atorva 10', 5, 89.00, 445.00),
(7, 5, 'Amlodipine 5mg', 4, 55.00, 220.00),
(7, 4, 'Metformin 500', 6, 65.00, 390.00),
(7, 6, 'Pan 40', 8, 72.00, 576.00),
(7, 20, 'Supradyn', 1, 220.00, 220.00),
(8, 10, 'Electral ORS', 1, 38.00, 38.00),
(8, 1, 'Dolo 650', 1, 32.50, 32.50),
(9, 1, 'Dolo 650', 3, 32.50, 97.50),
(9, 10, 'Electral ORS', 3, 38.00, 114.00),
(9, 8, 'Zyrtec 10mg', 2, 22.00, 44.00),
(10, 4, 'Metformin 500', 6, 65.00, 390.00),
(10, 15, 'Glimperide 1mg', 3, 62.00, 186.00),
(10, 9, 'Atorva 10', 5, 89.00, 445.00),
(10, 6, 'Pan 40', 8, 72.00, 576.00);

-- ============================================================
-- SEED DATA: SETTINGS
-- ============================================================
INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES
('clinic.name', 'MediCheck Pharmacy & Clinic', 'Name of the pharmacy/clinic'),
('clinic.address', 'Shop No. 42, MG Road, Bengaluru - 560001, Karnataka', 'Address for invoices'),
('clinic.phone', '+91-80-4567-8901', 'Contact number'),
('clinic.gstin', '29AAACM9617R1ZX', 'GST Identification Number'),
('currency.symbol', '₹', 'Currency symbol'),
('tax.rate', '18.0', 'Default GST rate (%)'),
('invoice.prefix', 'INV', 'Invoice number prefix'),
('prescription.prefix', 'RX', 'Prescription number prefix'),
('low.stock.threshold', '10', 'Default low stock alert threshold');

SELECT 'MediCheck database setup complete!' AS status;
