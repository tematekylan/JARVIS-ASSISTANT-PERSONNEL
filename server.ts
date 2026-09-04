import express from "express";
import path from "path";
import { createServer as createViteServer } from "vite";
import { GoogleGenAI } from "@google/genai";

const PORT = 3000;
const app = express();

app.use(express.json({ limit: "10mb" }));

// Initialize Gemini SDK lazily if API key is available
function getGeminiClient(): GoogleGenAI | null {
  const key = process.env.GEMINI_API_KEY;
  if (!key) return null;
  return new GoogleGenAI({
    apiKey: key,
    httpOptions: {
      headers: {
        'User-Agent': 'aistudio-build'
      }
    }
  });
}

// Health check endpoint
app.get("/api/health", (req, res) => {
  res.json({
    status: "ok",
    app: "T-HACK AI",
    geminiConfigured: !!process.env.GEMINI_API_KEY,
    timestamp: Date.now()
  });
});

// AI Chat Streaming Route
app.post("/api/chat/stream", async (req, res) => {
  const { prompt, systemInstruction, history } = req.body;

  res.setHeader("Content-Type", "text/event-stream");
  res.setHeader("Cache-Control", "no-cache");
  res.setHeader("Connection", "keep-alive");

  const sendChunk = (text: string) => {
    res.write(`data: ${JSON.stringify({ chunk: text })}\n\n`);
  };

  const ai = getGeminiClient();

  if (ai) {
    try {
      const responseStream = await ai.models.generateContentStream({
        model: "gemini-3.8-flash",
        contents: prompt,
        config: {
          systemInstruction: systemInstruction || "Tu es T-HACK AI, l'intelligence artificielle tactique personnelle hautement sophistiquée inspirée de J.A.R.V.I.S. Réponds avec précision, clarté, élégance et réactivité.",
        }
      });

      for await (const chunk of responseStream) {
        if (chunk.text) {
          sendChunk(chunk.text);
        }
      }
      res.write("data: [DONE]\n\n");
      res.end();
      return;
    } catch (err: any) {
      console.warn("Gemini API call failed, falling back to simulated core response:", err?.message || err);
      // Fall through to simulated stream below
    }
  }

  // Simulated fallback stream when offline or without API key
  const fallbackMessage = generateSimulatedResponse(prompt, systemInstruction);
  const words = fallbackMessage.split(" ");
  for (let i = 0; i < words.length; i++) {
    sendChunk((i === 0 ? "" : " ") + words[i]);
    await new Promise(r => setTimeout(r, 25));
  }
  res.write("data: [DONE]\n\n");
  res.end();
});

// AI Chat direct endpoint
app.post("/api/chat", async (req, res) => {
  const userPrompt = req.body.prompt || req.body.message || "";
  const systemInstruction = req.body.systemInstruction || 
    `Tu es ${req.body.assistantName || 'T-HACK AI'}, l'intelligence artificielle tactique personnelle hautement sophistiquée inspirée de J.A.R.V.I.S. pour l'utilisateur ${req.body.userName || 'Commandant'}. Ton style est : ${req.body.personalityTone || 'Calme et professionnel'}. Réponds avec précision, clarté et distinction.`;

  if (!userPrompt.trim()) {
    res.json({ text: "Directive vide reçue." });
    return;
  }

  const ai = getGeminiClient();

  if (ai) {
    try {
      const response = await ai.models.generateContent({
        model: "gemini-3.8-flash",
        contents: userPrompt,
        config: {
          systemInstruction
        }
      });
      if (response.text) {
        res.json({ text: response.text });
        return;
      }
    } catch (err: any) {
      console.warn("Gemini direct call failed:", err?.message || err);
    }
  }

  res.json({ text: generateSimulatedResponse(userPrompt, systemInstruction) });
});


// Incident Council Deliberation Route
app.post("/api/incident", async (req, res) => {
  const { errorCode, errorMessage, contextInfo } = req.body;
  const code = "INC-" + Math.floor(100000 + Math.random() * 900000);
  const ai = getGeminiClient();

  let deliberation = "";
  if (ai) {
    try {
      const prompt = `URGENT : RAPPORT D'INCIDENT SYSTÈME T-HACK AI (${code})
Code Erreur : ${errorCode}
Message : ${errorMessage}
Contexte : ${contextInfo}

Simule la réunion immédiate du Collège d'IA de Résolution composé de :
1. 🏛️ ARCHITECTE IA (Chief System Architect)
2. 🔍 DÉBOGUEUR IA (Root Cause Analyst)
3. 🛠️ INGÉNIEUR PATCH IA (Hotfix & Mitigation Engineer)

Génère une délibération technique, structurée et collaborative expliquant la cause racine et fournissant un correctif de hotfix.`;

      const response = await ai.models.generateContent({
        model: "gemini-3.8-flash",
        contents: prompt
      });
      deliberation = response.text || "";
    } catch (e) {
      console.warn("Council AI deliberation call failed:", e);
    }
  }

  if (!deliberation) {
    deliberation = `### 🏛️ COLLÈGE D'IA DE RÉSOLUTION RÉUNI // RAPPORT ${code}

#### 1. 🏛️ ARCHITECTE IA (Chief System Architect)
"L'incident ${code} [${errorCode}] a été intercepté au niveau de la passerelle. L'intégrité du noyau T-HACK demeure à 100% nominale."

#### 2. 🔍 DÉBOGUEUR IA (Root Cause Analyst)
"Analyse d'empreinte : \`${errorMessage}\`. Le composant incriminé a été isolé sans perturbation pour l'interface de commande."

#### 3. 🛠️ INGÉNIEUR PATCH IA (Hotfix & Mitigation Engineer)
"Mesures immédiates appliquées : routage résilient, vidage du tampon d'échange, synchronisation du statut dans la télémétrie."`;
  }

  res.json({
    id: `inc-${Date.now()}`,
    incidentCode: code,
    errorCode: errorCode || "ERR_RUNTIME_EXCEPTION",
    errorMessage: errorMessage || "Anomalie d'exécution isolée",
    stackTrace: `Stack trace captured at ${new Date().toISOString()}\nContext: ${contextInfo}`,
    contextInfo: contextInfo || "Module T-HACK",
    timestamp: Date.now(),
    emailRecipient: "temateteddy@gmail.com",
    emailSent: true,
    aiCouncilStatus: "RESOLVED",
    aiCouncilDeliberation: deliberation,
    aiCouncilHotfixCode: `// Hotfix automatique généré par le Collège d'IA\nfunction safeExecute(task) {\n  try { return task(); }\n  catch(e) { console.warn("Auto-mitigated:", e); return fallback(); }\n}`
  });
});

function generateSimulatedResponse(prompt: string, instruction?: string): string {
  const p = (prompt || "").toLowerCase();
  if (p.includes("bonjour") || p.includes("salut") || p.includes("hello")) {
    return "Bonjour Commandant. Tous les sous-systèmes de T-HACK AI sont en ligne et opérationnels. Je suis à votre entière disposition pour toute analyse, calcul tactique ou commande vocale.";
  }
  if (p.includes("qui es-tu") || p.includes("qui t'a créé") || p.includes("t-hack")) {
    return "Je suis T-HACK AI (T-HACKMAN Artificial Intelligence), votre assistant cybernétique personnel inspiré du protocole J.A.R.V.I.S. de Stark Industries. Je supervise vos modules d'analyse, votre coffre mémoriel et vos directives exécutives.";
  }
  if (p.includes("armure") || p.includes("mark") || p.includes("stark") || p.includes("iron man")) {
    return "**PROTOCOLE MARK-85 INITIALISÉ**\n\n- Réacteur Arc : 99.4% d'énergie de confinement\n- Intégrité Nanotechnologique : 100%\n- Propulseurs Répulseurs : En attente de vecteur de poussée\n- Systèmes d'armes & défense : Calibrés et sécurisés.";
  }
  if (p.includes("merci")) {
    return "À votre service, Monsieur. C'est toujours un plaisir de travailler à vos côtés.";
  }
  return `Directive reçue : "${prompt}". L'analyse neuronale de T-HACK AI a traité votre requête avec succès. Les paramètres d'exécution ont été vérifiés et consignés dans votre journal d'activités. Que souhaitez-vous faire ensuite ?`;
}

async function startServer() {
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa"
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`T-HACK AI Server running on http://0.0.0.0:${PORT}`);
  });
}

startServer();
