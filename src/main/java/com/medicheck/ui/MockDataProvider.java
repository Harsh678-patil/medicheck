package com.medicheck.ui;

import com.medicheck.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Provides Indian-centric mock data for demo/offline mode.
 */
public class MockDataProvider {

    public static List<Patient> getPatients() {
        List<Patient> list = new ArrayList<>();
        String[][] data = {
            {"Rajesh Kumar Sharma", "45", "Male", "+91-98201-34567", "Hypertension, Diabetes", "Delhi"},
            {"Priya Devi Menon", "32", "Female", "+91-97301-22134", "Thyroid Disorder", "Chennai"},
            {"Arun Patel", "58", "Male", "+91-99012-87654", "Chronic Back Pain, Arthritis", "Ahmedabad"},
            {"Sunita Yadav", "28", "Female", "+91-80456-91234", "PCOS, Anemia", "Lucknow"},
            {"Mohammed Farhan Ali", "41", "Male", "+91-70123-56789", "Asthma, Sinusitis", "Hyderabad"},
            {"Kavitha Swaminathan", "55", "Female", "+91-98765-43210", "Osteoporosis", "Bengaluru"},
            {"Vikram Singh Chauhan", "37", "Male", "+91-91234-78901", "Migraine, Anxiety", "Jaipur"},
            {"Lakshmi Narayan Iyer", "63", "Male", "+91-88901-23456", "Cardiac Disease, Hypertension", "Mumbai"},
            {"Deepika Chowdary", "24", "Female", "+91-77890-12345", "Dengue Fever", "Vijayawada"},
            {"Suresh Babu Reddy", "49", "Male", "+91-96543-21098", "Type 2 Diabetes", "Pune"},
            {"Ananya Krishnamurthy", "19", "Female", "+91-99876-54321", "Viral Fever, Cold", "Mysuru"},
            {"Harpreet Kaur Singh", "34", "Female", "+91-85234-67890", "Iron Deficiency Anemia", "Amritsar"},
            {"Ramesh Gupta", "67", "Male", "+91-98432-10987", "Chronic Kidney Disease", "Varanasi"},
            {"Nandini Pillai", "43", "Female", "+91-70987-65432", "Fibromyalgia", "Kochi"},
            {"Arjun Malhotra", "29", "Male", "+91-91876-54320", "Sports Injury, Fracture", "Chandigarh"},
        };
        for (int i = 0; i < data.length; i++) {
            Patient p = new Patient();
            p.setId(i + 1);
            p.setFullName(data[i][0]);
            p.setAge(Integer.parseInt(data[i][1]));
            p.setGender(data[i][2]);
            p.setPhone(data[i][3]);
            p.setDisease(data[i][4]);
            p.setAddress(data[i][5]);
            p.setDoctorName(getDoctors().get(i % getDoctors().size()).getFullName());
            list.add(p);
        }
        return list;
    }

    public static List<Doctor> getDoctors() {
        List<Doctor> list = new ArrayList<>();
        String[][] data = {
            {"Dr. Anand Krishnan", "Cardiologist", "MBBS, MD", "+91-98201-11111", "anand.krishnan@medicheck.in", "Apollo Hospital"},
            {"Dr. Sunita Agarwal", "General Physician", "MBBS, PGDM", "+91-98201-22222", "sunita.agarwal@medicheck.in", "Fortis Hospital"},
            {"Dr. Rajan Mehta", "Orthopedic Surgeon", "MBBS, MS Ortho", "+91-98201-33333", "rajan.mehta@medicheck.in", "AIIMS Delhi"},
            {"Dr. Preethi Nambiar", "Gynaecologist", "MBBS, DGO, MD", "+91-98201-44444", "preethi.nambiar@medicheck.in", "Manipal Hospital"},
            {"Dr. Suresh Rao", "Neurologist", "MBBS, DM Neuro", "+91-98201-55555", "suresh.rao@medicheck.in", "Narayana Health"},
            {"Dr. Kavya Pillai", "Endocrinologist", "MBBS, DM Endoc", "+91-98201-66666", "kavya.pillai@medicheck.in", "Aster Medcity"},
            {"Dr. Vivek Sharma", "Pulmonologist", "MBBS, MD Pulm", "+91-98201-77777", "vivek.sharma@medicheck.in", "Max Hospital"},
            {"Dr. Devika Menon", "Dermatologist", "MBBS, MD Derma", "+91-98201-88888", "devika.menon@medicheck.in", "Sankara Nethralaya"},
        };
        for (int i = 0; i < data.length; i++) {
            Doctor d = new Doctor();
            d.setId(i + 1);
            d.setFullName(data[i][0]);
            d.setSpecialty(data[i][1]);
            d.setQualification(data[i][2]);
            d.setPhone(data[i][3]);
            d.setEmail(data[i][4]);
            d.setAddress(data[i][5]);
            d.setActive(true);
            list.add(d);
        }
        return list;
    }

    public static List<Medicine> getMedicines() {
        List<Medicine> list = new ArrayList<>();
        Object[][] data = {
            {"Dolo 650", "Paracetamol", "Analgesic/Antipyretic", "MF10010001", new BigDecimal("32.50"), new BigDecimal("18.00"), 450, 50, "2026-12-31", "Micro Labs", "Strip of 15 tablets"},
            {"Crocin 500", "Paracetamol", "Analgesic/Antipyretic", "GS10010002", new BigDecimal("28.00"), new BigDecimal("15.50"), 320, 50, "2026-10-31", "GSK", "Strip of 20 tablets"},
            {"Combiflam", "Ibuprofen + Paracetamol", "NSAID Combination", "SN10010003", new BigDecimal("45.00"), new BigDecimal("26.00"), 210, 40, "2025-08-31", "Sanofi India", "Strip of 20 tablets"},
            {"Metformin 500", "Metformin HCl", "Antidiabetic", "FP10010004", new BigDecimal("65.00"), new BigDecimal("38.00"), 180, 30, "2026-06-30", "Franco-Indian Pharma", "Strip of 10 tablets"},
            {"Amlodipine 5mg", "Amlodipine Besylate", "Antihypertensive", "UA10010005", new BigDecimal("55.00"), new BigDecimal("30.00"), 80, 25, "2026-09-30", "USV Ltd", "Strip of 10 tablets"},
            {"Pantoprazole 40", "Pantoprazole Sodium", "Proton Pump Inhibitor", "SN10010006", new BigDecimal("72.00"), new BigDecimal("42.00"), 150, 30, "2026-11-30", "Sun Pharma", "Strip of 15 tablets"},
            {"Azithromycin 500", "Azithromycin", "Antibiotic Macrolide", "CI10010007", new BigDecimal("110.00"), new BigDecimal("65.00"), 95, 20, "2025-07-31", "Cipla", "Strip of 3 tablets"},
            {"Cetirizine 10mg", "Cetirizine HCl", "Antihistamine", "AL10010008", new BigDecimal("22.00"), new BigDecimal("12.00"), 230, 40, "2026-12-31", "Alembic Pharma", "Strip of 10 tablets"},
            {"Atorvastatin 10", "Atorvastatin Calcium", "Lipid Lowering", "TP10010009", new BigDecimal("89.00"), new BigDecimal("52.00"), 60, 20, "2026-08-31", "Torrent Pharma", "Strip of 10 tablets"},
            {"ORS Electral", "Oral Rehydration Salts", "Electrolyte", "FR10010010", new BigDecimal("38.00"), new BigDecimal("20.00"), 300, 50, "2025-12-31", "Franco-Indian", "Pack of 4 sachets"},
            {"Salbutamol Inhaler", "Salbutamol Sulphate", "Bronchodilator", "CI10010011", new BigDecimal("156.00"), new BigDecimal("95.00"), 35, 10, "2026-03-31", "Cipla", "100 mcg per dose inhaler"},
            {"Amoxicillin 500", "Amoxicillin Trihydrate", "Penicillin Antibiotic", "AL10010012", new BigDecimal("95.00"), new BigDecimal("56.00"), 120, 25, "2025-09-30", "Alembic Pharma", "Strip of 10 capsules"},
            {"Omeprazole 20", "Omeprazole", "Proton Pump Inhibitor", "AS10010013", new BigDecimal("48.00"), new BigDecimal("27.00"), 175, 35, "2026-07-31", "AstraZeneca India", "Strip of 15 capsules"},
            {"Losartan 50", "Losartan Potassium", "ARB Antihypertensive", "GZ10010014", new BigDecimal("78.00"), new BigDecimal("45.00"), 12, 25, "2026-05-31", "Glenmark Pharma", "Strip of 10 tablets"},
            {"Glimepiride 1mg", "Glimepiride", "Sulfonylurea Antidiabetic", "SN10010015", new BigDecimal("62.00"), new BigDecimal("36.00"), 8, 20, "2026-02-28", "Sun Pharma", "Strip of 10 tablets"},
            {"Vitamin D3 1000IU", "Cholecalciferol", "Vitamin Supplement", "AB10010016", new BigDecimal("189.00"), new BigDecimal("110.00"), 90, 15, "2027-01-31", "Abbott India", "Strip of 4 soft gels"},
            {"Iron Folic Acid", "Ferrous Fumarate + Folic Acid", "Haematinics", "PF10010017", new BigDecimal("42.00"), new BigDecimal("22.00"), 220, 40, "2026-10-31", "Pfizer India", "Strip of 30 tablets"},
            {"Diclofenac 50", "Diclofenac Sodium", "NSAID Anti-inflammatory", "NV10010018", new BigDecimal("35.00"), new BigDecimal("18.00"), 140, 30, "2025-11-30", "Novartis India", "Strip of 10 tablets"},
            {"Montelukast 10", "Montelukast Sodium", "Leukotriene Inhibitor", "MK10010019", new BigDecimal("135.00"), new BigDecimal("82.00"), 55, 15, "2026-09-30", "Merck India", "Strip of 10 tablets"},
            {"Multivitamin NatB", "Multivitamins & Minerals", "Nutritional Supplement", "HW10010020", new BigDecimal("220.00"), new BigDecimal("135.00"), 110, 20, "2027-03-31", "Himalaya Wellness", "Strip of 15 tablets"},
        };
        for (int i = 0; i < data.length; i++) {
            Medicine m = new Medicine();
            m.setId(i + 1);
            m.setName((String) data[i][0]);
            m.setGenericName((String) data[i][1]);
            m.setCategory((String) data[i][2]);
            m.setBarcode((String) data[i][3]);
            m.setPrice((BigDecimal) data[i][4]);
            m.setCostPrice((BigDecimal) data[i][5]);
            m.setQuantity((int) data[i][6]);
            m.setReorderLevel((int) data[i][7]);
            m.setExpiryDate(LocalDate.parse((String) data[i][8]));
            m.setManufacturer((String) data[i][9]);
            m.setUnit((String) data[i][10]);
            m.setActive(true);
            list.add(m);
        }
        return list;
    }

    public static List<Sale> getSales() {
        List<Sale> list = new ArrayList<>();
        String[][] data = {
            {"INV-2024-001", "Rajesh Kumar Sharma", "Dr. Anand Krishnan", "₹320.50", "Cash", "Paid"},
            {"INV-2024-002", "Priya Devi Menon", "Dr. Sunita Agarwal", "₹789.00", "UPI", "Paid"},
            {"INV-2024-003", "Arun Patel", "Dr. Rajan Mehta", "₹1,245.00", "Card", "Paid"},
            {"INV-2024-004", "Walk-in Customer", "N/A", "₹156.00", "Cash", "Paid"},
            {"INV-2024-005", "Sunita Yadav", "Dr. Preethi Nambiar", "₹432.50", "UPI", "Paid"},
            {"INV-2024-006", "Mohammed Farhan Ali", "Dr. Vivek Sharma", "₹988.00", "Card", "Paid"},
            {"INV-2024-007", "Lakshmi Narayan Iyer", "Dr. Anand Krishnan", "₹2,156.00", "Insurance", "Paid"},
            {"INV-2024-008", "Walk-in Customer", "N/A", "₹89.00", "Cash", "Paid"},
            {"INV-2024-009", "Deepika Chowdary", "Dr. Sunita Agarwal", "₹345.00", "UPI", "Paid"},
            {"INV-2024-010", "Suresh Babu Reddy", "Dr. Kavya Pillai", "₹1,890.00", "Card", "Paid"},
        };
        for (int i = 0; i < data.length; i++) {
            Sale s = new Sale();
            s.setId(i + 1);
            s.setInvoiceNo(data[i][0]);
            s.setPatientName(data[i][1]);
            s.setCashierName("Rahul Pharmacist");
            String amountStr = data[i][3].replace("₹", "").replace(",", "");
            s.setTotal(new BigDecimal(amountStr));
            s.setPaymentMethod(data[i][4]);
            s.setPaymentStatus(data[i][5]);
            s.setCreatedAt(LocalDateTime.now().minusHours(i * 2));
            list.add(s);
        }
        return list;
    }

    public static List<Prescription> getPrescriptions() {
        List<Prescription> list = new ArrayList<>();
        String[][] data = {
            {"RX-2024-001", "Rajesh Kumar Sharma", "Dr. Anand Krishnan", "Hypertension Stage 2", "Active"},
            {"RX-2024-002", "Priya Devi Menon", "Dr. Sunita Agarwal", "Hashimoto's Thyroiditis", "Dispensed"},
            {"RX-2024-003", "Arun Patel", "Dr. Rajan Mehta", "Lumbar Disc Herniation", "Active"},
            {"RX-2024-004", "Sunita Yadav", "Dr. Preethi Nambiar", "PCOS with Insulin Resistance", "Active"},
            {"RX-2024-005", "Mohammed Farhan Ali", "Dr. Vivek Sharma", "Moderate Persistent Asthma", "Dispensed"},
            {"RX-2024-006", "Kavitha Swaminathan", "Dr. Kavya Pillai", "Osteoporosis - Post menopausal", "Active"},
            {"RX-2024-007", "Vikram Singh Chauhan", "Dr. Suresh Rao", "Chronic Migraine with Aura", "Expired"},
            {"RX-2024-008", "Lakshmi Narayan Iyer", "Dr. Anand Krishnan", "Ischemic Heart Disease", "Dispensed"},
        };
        for (int i = 0; i < data.length; i++) {
            Prescription p = new Prescription();
            p.setId(i + 1);
            p.setPrescriptionNo(data[i][0]);
            p.setPatientName(data[i][1]);
            p.setDoctorName(data[i][2]);
            p.setDisease(data[i][3]);
            p.setStatus(data[i][4]);
            p.setCreatedAt(LocalDateTime.now().minusDays(i * 3));
            list.add(p);
        }
        return list;
    }

    // Dashboard stats
    public static int getTotalPatients() { return 1247; }
    public static int getTotalDoctors() { return 8; }
    public static int getTotalMedicines() { return 20; }
    public static int getLowStockCount() { return 3; } // Losartan, Glimepiride, Salbutamol
    public static BigDecimal getTodaySales() { return new BigDecimal("28450.75"); }
    public static BigDecimal getMonthRevenue() { return new BigDecimal("645230.50"); }

    public static Map<String, BigDecimal> getSalesTrend() {
        Map<String, BigDecimal> trend = new LinkedHashMap<>();
        String[] days = {"10 Apr", "11 Apr", "12 Apr", "13 Apr", "14 Apr", "15 Apr"};
        BigDecimal[] vals = {new BigDecimal("18200"), new BigDecimal("22500"), new BigDecimal("19800"),
                new BigDecimal("31400"), new BigDecimal("24600"), new BigDecimal("28450")};
        for (int i = 0; i < days.length; i++) trend.put(days[i], vals[i]);
        return trend;
    }

    public static Map<String, Integer> getTopMedicines() {
        Map<String, Integer> top = new LinkedHashMap<>();
        top.put("Dolo 650", 892);
        top.put("Metformin 500", 654);
        top.put("ORS Electral", 512);
        top.put("Crocin 500", 488);
        top.put("Cetirizine 10mg", 423);
        return top;
    }
}
