package com.medicheck.service;

import com.medicheck.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * AI Assistant service with rule-based medical knowledge base.
 * Optional OpenAI integration behind a simple interface.
 * DISCLAIMER: For informational purposes only. Final decisions must be made by a licensed doctor.
 */
public class AIAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AIAssistantService.class);

    // Rule-based knowledge base: disease/symptom -> [medicines, advice, warnings]
    private static final Map<String, String[][]> KNOWLEDGE_BASE = new LinkedHashMap<>();

    static {
        // Format: {medicines[], dosage_hints[], warnings[], advice[]}
        KNOWLEDGE_BASE.put("fever", new String[][]{
            {"Paracetamol 500mg", "Ibuprofen 400mg", "Aspirin 75mg"},
            {"Paracetamol 500mg: 1 tablet every 4-6 hours (max 4g/day)", "Ibuprofen: 400mg every 8 hours with food"},
            {"Do NOT use Aspirin in children under 16", "Ibuprofen contraindicated in kidney disease"},
            {"Rest, hydration. If fever > 103°F or lasts > 3 days, see a doctor immediately."}
        });
        KNOWLEDGE_BASE.put("headache", new String[][]{
            {"Paracetamol 500mg", "Ibuprofen 400mg", "Aspirin 75mg"},
            {"Paracetamol: 500-1000mg every 4-6 hours", "Ibuprofen: 200-400mg every 6-8 hours"},
            {"Avoid overuse - medication overuse headache can develop with frequent use"},
            {"Ensure adequate sleep and hydration. Migraine may need specialist evaluation."}
        });
        KNOWLEDGE_BASE.put("cold", new String[][]{
            {"Cetirizine 10mg", "Paracetamol 500mg", "Vitamin C 500mg"},
            {"Cetirizine: 1 tablet once daily at night", "Paracetamol for fever/body ache"},
            {"Cetirizine may cause drowsiness. Avoid driving."},
            {"Rest, warm fluids, and steam inhalation help. Antibiotics do NOT treat viral colds."}
        });
        KNOWLEDGE_BASE.put("cough", new String[][]{
            {"Dextromethorphan syrup", "Salbutamol inhaler (if wheezing)", "Amoxicillin 500mg (if bacterial)"},
            {"Dextromethorphan: as per label", "Salbutamol: 1-2 puffs every 4-6 hours as needed"},
            {"Antibiotics only if bacterial infection confirmed by doctor", "Dextromethorphan not for productive cough"},
            {"Honey and lemon in warm water can soothe. Persistent cough (>2 weeks) needs evaluation."}
        });
        KNOWLEDGE_BASE.put("diarrhea", new String[][]{
            {"ORS Sachets", "Metronidazole 400mg (if infective)", "Loperamide 2mg"},
            {"ORS: 1 sachet per 200ml water, drink frequently", "Metronidazole: 400mg 3x daily for 5-7 days"},
            {"Metronidazole: avoid alcohol completely during treatment", "Loperamide: not for bloody diarrhea"},
            {"Stay hydrated. ORS is the primary treatment."}
        });
        KNOWLEDGE_BASE.put("hypertension", new String[][]{
            {"Amlodipine 5mg", "Atenolol 50mg", "Losartan 50mg"},
            {"Amlodipine: 5-10mg once daily", "Atenolol: 25-50mg once daily"},
            {"Never stop antihypertensives abruptly", "Monitor BP regularly", "Avoid NSAIDs"},
            {"Low salt diet, regular exercise, avoid stress. Regular monitoring essential."}
        });
        KNOWLEDGE_BASE.put("diabetes", new String[][]{
            {"Metformin 500mg", "Glimepiride 1mg", "Insulin (as prescribed)"},
            {"Metformin: 500mg twice daily with meals initially", "Glimepiride: once daily before breakfast"},
            {"Metformin: avoid with contrast dye/surgery", "Monitor blood sugar regularly", "Risk of hypoglycemia with sulfonylureas"},
            {"Diet control, exercise (30 min/day), and regular HbA1c checks are essential."}
        });
        KNOWLEDGE_BASE.put("allergy", new String[][]{
            {"Cetirizine 10mg", "Loratadine 10mg", "Chlorpheniramine 4mg"},
            {"Cetirizine: 10mg once daily", "Loratadine: 10mg once daily (less sedating)"},
            {"Antihistamines may cause drowsiness. Avoid alcohol and driving."},
            {"Identify and avoid allergens. Seek medical help for severe reactions (anaphylaxis)."}
        });
        KNOWLEDGE_BASE.put("acidity", new String[][]{
            {"Omeprazole 20mg", "Pantoprazole 40mg", "Ranitidine 150mg", "Antacid suspension"},
            {"Omeprazole: 20mg once daily before breakfast for 4-8 weeks", "Antacid: as needed after meals"},
            {"PPIs: long-term use may affect bone density and B12 absorption"},
            {"Avoid spicy food, coffee, alcohol. Eat small meals. Don't lie down immediately after eating."}
        });
        KNOWLEDGE_BASE.put("infection", new String[][]{
            {"Amoxicillin 500mg", "Azithromycin 500mg", "Ciprofloxacin 500mg"},
            {"Amoxicillin: 500mg 3x daily for 5-7 days", "Azithromycin: 500mg once daily for 3-5 days"},
            {"Complete full antibiotic course. Never self-prescribe antibiotics.", "Allergy cross-reactivity possible."},
            {"Antibiotic choice depends on infection site and sensitivity. Doctor must confirm bacterial infection."}
        });
        KNOWLEDGE_BASE.put("pain", new String[][]{
            {"Paracetamol 500mg", "Ibuprofen 400mg", "Diclofenac 50mg"},
            {"Paracetamol: 500-1000mg every 4-6 hours (max 4g/day)", "Ibuprofen: 400mg every 8 hours with food"},
            {"NSAIDs (Ibuprofen, Diclofenac): contraindicated in GI ulcers, kidney disease", "Avoid with blood thinners"},
            {"Identify and treat underlying cause. Chronic pain needs medical evaluation."}
        });
        KNOWLEDGE_BASE.put("asthma", new String[][]{
            {"Salbutamol inhaler", "Budesonide inhaler", "Montelukast 10mg"},
            {"Salbutamol: 1-2 puffs every 4-6 hours as needed (rescue inhaler)", "Budesonide: daily preventer inhaler"},
            {"Never stop preventer without doctor advice", "Monitor peak flow regularly"},
            {"Avoid triggers. Always carry rescue inhaler. Emergency: >10 puffs within 1 hour - go to ER."}
        });
    }

    /**
     * Get AI suggestions for a disease/symptom query.
     * Returns formatted response as HTML string.
     */
    public String getSuggestion(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "<p>Please enter a symptom or disease name to get information.</p>";
        }

        String normalizedQuery = query.trim().toLowerCase();
        String matchedKey = findBestMatch(normalizedQuery);

        if (matchedKey != null) {
            return formatKnowledgeResponse(matchedKey, KNOWLEDGE_BASE.get(matchedKey));
        }

        // Try OpenAI if configured
        AppConfig config = AppConfig.getInstance();
        if (config.getBoolean("openai.enabled", false) && !config.get("openai.api.key").isEmpty()) {
            return callOpenAI(query);
        }

        return formatNotFoundResponse(query);
    }

    private String findBestMatch(String query) {
        // Exact match first
        if (KNOWLEDGE_BASE.containsKey(query)) return query;
        // Partial match
        for (String key : KNOWLEDGE_BASE.keySet()) {
            if (query.contains(key) || key.contains(query)) return key;
        }
        return null;
    }

    private String formatKnowledgeResponse(String condition, String[][] data) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: Segoe UI; font-size: 13px; color: #E2E8F0; background: transparent;'>");
        sb.append("<h3 style='color: #6366F1;'>🔍 Condition: ").append(capitalize(condition)).append("</h3>");

        // Medicines
        sb.append("<h4 style='color: #22C55E;'>💊 Commonly Used Medicines</h4><ul>");
        for (String med : data[0]) sb.append("<li>").append(med).append("</li>");
        sb.append("</ul>");

        // Dosage hints
        sb.append("<h4 style='color: #F59E0B;'>⚕️ Dosage Information</h4><ul>");
        for (String dose : data[1]) sb.append("<li>").append(dose).append("</li>");
        sb.append("</ul>");

        // Warnings
        sb.append("<h4 style='color: #EF4444;'>⚠️ Warnings & Precautions</h4><ul>");
        for (String warn : data[2]) sb.append("<li>").append(warn).append("</li>");
        sb.append("</ul>");

        // General advice
        sb.append("<h4 style='color: #00D4AA;'>💡 General Advice</h4><ul>");
        for (String adv : data[3]) sb.append("<li>").append(adv).append("</li>");
        sb.append("</ul>");

        sb.append("<p style='background:#1A1A36; padding:8px; border-radius:6px; color:#94A3B8; font-size:11px; margin-top:10px;'>");
        sb.append("⚕️ <b>MEDICAL DISCLAIMER:</b> This information is for educational purposes only. ");
        sb.append("It does NOT replace professional medical advice. Always consult a licensed doctor before taking any medication.");
        sb.append("</p></body></html>");
        return sb.toString();
    }

    private String formatNotFoundResponse(String query) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: Segoe UI; color: #E2E8F0;'>");
        sb.append("<p>No specific information found for: <b>").append(query).append("</b></p>");
        sb.append("<p>Try searching for: fever, headache, cold, cough, diarrhea, hypertension, diabetes, allergy, acidity, infection, pain, asthma</p>");
        sb.append("<p style='color: #94A3B8; font-size: 11px;'>To enable AI-powered responses, configure the OpenAI API key in Settings.</p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private String callOpenAI(String query) {
        try {
            AppConfig config = AppConfig.getInstance();
            String apiKey = config.get("openai.api.key").trim();
            String model   = config.get("openai.model", "gpt-3.5-turbo");

            // ── Detect Gemini key (starts with AIza or AQ.) ───────────
            if (apiKey.startsWith("AIza") || apiKey.startsWith("AQ.")) {
                return callGemini(query, apiKey);
            }

            // ── OpenAI path ────────────────────────────────────────────
            String prompt = buildMedicalPrompt(query);
            String requestBody = "{\"model\":\"" + model + "\",\"messages\":[{\"role\":\"user\",\"content\":\""
                    + escapeJson(prompt) + "\"}],\"max_tokens\":600}";

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseAndFormatOpenAIResponse(response.body(), query);
            }
            log.warn("OpenAI returned status {}", response.statusCode());
        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
        }
        return formatNotFoundResponse(query);
    }

    private String callGemini(String query, String apiKey) {
        try {
            String prompt = buildMedicalPrompt(query);
            String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapeJson(prompt) + "\"}]}]}";

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String body = response.body();
                // Parse: {"candidates":[{"content":{"parts":[{"text":"..."}]}}]}
                int textStart = body.indexOf("\"text\":\"") + 8;
                int textEnd   = body.lastIndexOf("\"}]},");
                if (textStart > 8 && textEnd > textStart) {
                    String text = body.substring(textStart, textEnd)
                            .replace("\\n", "<br>")
                            .replace("\\\"", "\"")
                            .replace("**", "")   // remove markdown bold
                            .replace("##", "");  // remove markdown headers
                    return "<html><body style='font-family:Segoe UI;font-size:13px;color:#E2E8F0;background:transparent;'>"
                         + "<h3 style='color:#6366F1;'>🤖 Gemini AI — " + capitalize(query) + "</h3>"
                         + "<p>" + text + "</p>"
                         + "<p style='background:#1A1A36;padding:8px;border-radius:6px;color:#94A3B8;font-size:11px;margin-top:10px;'>"
                         + "⚕️ <b>DISCLAIMER:</b> AI-generated information is for educational purposes only. "
                         + "Always consult a licensed doctor before taking any medication.</p>"
                         + "</body></html>";
                }
            }
            log.warn("Gemini returned status {}: {}", response.statusCode(), response.body());
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
        }
        return formatNotFoundResponse(query);
    }

    private String buildMedicalPrompt(String query) {
        return "You are a medical information assistant for a pharmacy management system in India. "
             + "The user asked about: " + query + ". "
             + "Provide concise information about: "
             + "1) Common medicines used in India (use Indian brand names where possible) "
             + "2) Typical dosage "
             + "3) Warnings and precautions "
             + "4) General lifestyle advice. "
             + "Keep it brief, factual, and end with: 'Consult a licensed doctor before taking any medication.'";
    }

    private String parseAndFormatOpenAIResponse(String body, String query) {
        int contentStart = body.indexOf("\"content\":\"") + 11;
        int contentEnd   = body.indexOf("\",\"", contentStart);
        if (contentStart > 11 && contentEnd > 0) {
            String content = body.substring(contentStart, contentEnd)
                    .replace("\\n", "<br>").replace("\\\"", "\"");
            return "<html><body style='font-family:Segoe UI;font-size:13px;color:#E2E8F0;'>"
                 + "<h3 style='color:#6366F1;'>🤖 AI Assistant — " + capitalize(query) + "</h3>"
                 + "<p>" + content + "</p></body></html>";
        }
        return formatNotFoundResponse(query);
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public Set<String> getAvailableConditions() {
        return Collections.unmodifiableSet(KNOWLEDGE_BASE.keySet());
    }
}
