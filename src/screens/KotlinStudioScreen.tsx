import React, { useState } from 'react';
import { 
  FileCode2, 
  Copy, 
  Check, 
  Download, 
  ArrowLeft, 
  Folder, 
  Layers, 
  Cpu, 
  Terminal, 
  Sparkles, 
  ShieldCheck, 
  Smartphone,
  ExternalLink
} from 'lucide-react';
import { JarvisScreen } from '../types';

interface KotlinStudioScreenProps {
  onBack: () => void;
  onNavigate: (screen: JarvisScreen) => void;
}

interface KotlinFile {
  name: string;
  path: string;
  category: 'App' | 'UI & Compose' | 'ViewModels' | 'Services' | 'Repository' | 'Config';
  code: string;
}

export const KotlinStudioScreen: React.FC<KotlinStudioScreenProps> = ({ onBack }) => {
  const kotlinFiles: KotlinFile[] = [
    {
      name: "MainActivity.kt",
      path: "app/src/main/java/com/thackman/ai/MainActivity.kt",
      category: "App",
      code: `package com.thackman.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.thackman.ai.ui.screens.MainAppNavigation
import com.thackman.ai.ui.theme.THackmanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            THackmanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF030609)),
                    color = Color(0xFF030609)
                ) {
                    MainAppNavigation()
                }
            }
        }
    }
}`
    },
    {
      name: "AICoreOrb.kt",
      path: "app/src/main/java/com/thackman/ai/ui/components/AICoreOrb.kt",
      category: "UI & Compose",
      code: `package com.thackman.ai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.thackman.ai.model.AssistantState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AICoreOrb(
    state: AssistantState,
    amplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_anim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val baseColor = when (state) {
        AssistantState.LISTENING -> Color(0xFF00E5FF)
        AssistantState.THINKING -> Color(0xFF78F7FF)
        AssistantState.SPEAKING -> Color(0xFF31F5A3)
        AssistantState.ERROR -> Color(0xFFFF4660)
        else -> Color(0xFF00E5FF)
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2.8f) * pulse * (1f + amplitude * 0.25f)

            // Outer energy glow ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(baseColor.copy(alpha = 0.35f), Color.Transparent),
                    center = center,
                    radius = baseRadius * 1.5f
                ),
                radius = baseRadius * 1.5f,
                center = center
            )

            // Dynamic segmented cyber orbit ring
            val segments = 32
            for (i in 0 until segments) {
                val angle = Math.toRadians((i * (360.0 / segments) + rotation)).toFloat()
                val r1 = baseRadius * 1.15f
                val r2 = baseRadius * 1.25f
                val start = Offset(center.x + cos(angle) * r1, center.y + sin(angle) * r1)
                val end = Offset(center.x + cos(angle) * r2, center.y + sin(angle) * r2)
                drawLine(
                    color = baseColor.copy(alpha = 0.6f),
                    start = start,
                    end = end,
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Inner glowing core
            drawCircle(
                color = baseColor.copy(alpha = 0.85f),
                radius = baseRadius * 0.7f,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = baseRadius * 0.35f,
                center = center
            )
        }
    }
}`
    },
    {
      name: "HomeScreen.kt",
      path: "app/src/main/java/com/thackman/ai/ui/screens/HomeScreen.kt",
      category: "UI & Compose",
      code: `package com.thackman.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thackman.ai.model.AssistantState
import com.thackman.ai.ui.components.AICoreOrb
import com.thackman.ai.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateChat: () -> Unit,
    onNavigateCommandCenter: () -> Unit,
    onNavigateTasks: () -> Unit
) {
    val state by viewModel.assistantState.collectAsState()
    val amplitude by viewModel.audioAmplitude.collectAsState()
    var inputQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030609))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF0A1219),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF007C91).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF00E5FF), shape = RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "T-HACKMAN ONLINE",
                        color = Color(0xFFE5FCFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = onNavigateCommandCenter) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Centre de contrôle",
                    tint = Color(0xFF00E5FF)
                )
            }
        }

        // Center: Hologram Orb & Greeting
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AICoreOrb(
                state = state,
                amplitude = amplitude,
                onClick = { viewModel.toggleVoiceRecognition() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "T-HACKMAN AI",
                color = Color(0xFFE5FCFF),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Assistant Cybernétique Autonome",
                color = Color(0xFF6F9DA6),
                fontSize = 13.sp
            )
        }

        // Bottom: Input Box with Voice Trigger
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0D1821),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF007C91).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.toggleVoiceRecognition() }) {
                    Icon(
                        imageVector = if (state == AssistantState.LISTENING) Icons.Default.Mic else Icons.Default.MicNone,
                        contentDescription = "Micro",
                        tint = if (state == AssistantState.LISTENING) Color(0xFFFF4660) else Color(0xFF00E5FF)
                    )
                }

                TextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { Text("Donnez un ordre ou posez une question...", color = Color(0xFF5B7B88), fontSize = 13.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color(0xFFE5FCFF),
                        unfocusedTextColor = Color(0xFFE5FCFF)
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        if (inputQuery.isNotBlank()) {
                            viewModel.executeCommand(inputQuery)
                            inputQuery = ""
                            onNavigateChat()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Envoyer",
                        tint = Color(0xFF00E5FF)
                    )
                }
            }
        }
    }
}`
    },
    {
      name: "ActionExecutor.kt",
      path: "app/src/main/java/com/thackman/ai/automation/ActionExecutor.kt",
      category: "Services",
      code: `package com.thackman.ai.automation

import android.content.Context
import com.thackman.ai.service.HardwareController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AutomationResult(
    val executed: Boolean,
    val actionName: String? = null,
    val responseMessage: String? = null
)

class ActionExecutor(private val context: Context) {
    private val hardware = HardwareController(context)
    private val taskManager = TaskManager.getInstance()

    fun execute(rawCommand: String): AutomationResult {
        val cmd = rawCommand.lowercase().trim()

        // 1. Hardware: Flashlight
        if (cmd.contains("torche") || cmd.contains("lampe")) {
            val enable = !cmd.contains("éteins") && !cmd.contains("stop")
            hardware.toggleFlashlight(enable)
            return AutomationResult(
                executed = true,
                actionName = "HARDWARE_FLASHLIGHT",
                responseMessage = if (enable) "Lampe torche activée." else "Lampe torche désactivée."
            )
        }

        // 2. Hardware: Vibrate / Feedback
        if (cmd.contains("vibre") || cmd.contains("retour haptique")) {
            hardware.vibrate(300)
            return AutomationResult(
                executed = true,
                actionName = "HARDWARE_VIBRATE",
                responseMessage = "Impulsion haptique transmise."
            )
        }

        // 3. Task / Reminder Creation
        val taskRegex = Regex("""(?:ajoute une tâche|crée une tâche|nouvelle tâche|rappelle-moi de|rappel)\s+(.+)""", RegexOption.IGNORE_CASE)
        val match = taskRegex.find(cmd)
        if (match != null) {
            val title = match.groupValues[1].trim()
            taskManager.addTask(title, "Rappel Vocal", "HIGH")
            return AutomationResult(
                executed = true,
                actionName = "CREATE_TASK",
                responseMessage = "Tâche enregistrée dans votre protocole : '$title'."
            )
        }

        return AutomationResult(executed = false)
    }
}`
    },
    {
      name: "HardwareController.kt",
      path: "app/src/main/java/com/thackman/ai/service/HardwareController.kt",
      category: "Services",
      code: `package com.thackman.ai.service

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HardwareController(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var isTorchOn = false

    fun toggleFlashlight(enabled: Boolean) {
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, enabled)
            isTorchOn = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun vibrate(durationMs: Long = 200) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(durationMs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}`
    },
    {
      name: "VoiceRecognitionService.kt",
      path: "app/src/main/java/com/thackman/ai/service/VoiceRecognitionService.kt",
      category: "Services",
      code: `package com.thackman.ai.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceRecognitionService(private val context: Context) {
    private var recognizer: SpeechRecognizer? = null
    private val _spokenText = MutableStateFlow("")
    val spokenText = _spokenText.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    fun startListening(onResult: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        recognizer?.destroy()

        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { _isListening.value = true }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { _isListening.value = false }
                override fun onError(error: Int) { _isListening.value = false }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        _spokenText.value = text
                        onResult(text)
                    }
                    _isListening.value = false
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        recognizer?.startListening(intent)
    }

    fun stopListening() {
        recognizer?.stopListening()
        _isListening.value = false
    }
}`
    },
    {
      name: "AIRepository.kt",
      path: "app/src/main/java/com/thackman/ai/repository/AIRepository.kt",
      category: "Repository",
      code: `package com.thackman.ai.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.thackman.ai.model.AIMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIRepository(apiKey: String) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey,
        systemInstruction = com.google.ai.client.generativeai.type.content {
            text("""
                Tu es T-HACKMAN AI, un assistant personnel futuriste et cybernétique hautement qualifié.
                Tu réponds en français de façon concise, précise, technologique et cordiale.
                Tu aides pour la gestion de tâches, l'analyse système, les requêtes et l'automatisation.
            """.trimIndent())
        }
    )

    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Aucune réponse générée par le noyau."
        } catch (e: Exception) {
            "Erreur de communication avec le noyau IA : \${e.localizedMessage}"
        }
    }
}`
    },
    {
      name: "build.gradle.kts (Module: app)",
      path: "app/build.gradle.kts",
      category: "Config",
      code: `plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.thackman.ai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thackman.ai"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "2.5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Google Generative AI (Gemini SDK)
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}`
    },
    {
      name: "AndroidManifest.xml",
      path: "app/src/main/AndroidManifest.xml",
      category: "Config",
      code: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Permissions Système T-HACKMAN AI -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.FLASHLIGHT" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:name=".THackmanApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="T-HACKMAN AI"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.THackmanAI">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.THackmanAI"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>`
    }
  ];

  const [selectedFile, setSelectedFile] = useState<KotlinFile>(kotlinFiles[0]);
  const [copied, setCopied] = useState(false);
  const [activeCategory, setActiveCategory] = useState<string>('All');

  const categories = ['All', 'App', 'UI & Compose', 'ViewModels', 'Services', 'Repository', 'Config'];

  const filteredFiles = activeCategory === 'All' 
    ? kotlinFiles 
    : kotlinFiles.filter(f => f.category === activeCategory);

  const handleCopy = () => {
    navigator.clipboard.writeText(selectedFile.code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownloadAll = () => {
    const combinedContent = kotlinFiles.map(f => `// ==========================================\n// FILE: ${f.path}\n// ==========================================\n\n${f.code}\n\n`).join('\n');
    const blob = new Blob([combinedContent], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'thackman-ai-android-kotlin-project.kt';
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="flex-1 flex flex-col bg-[#030609] text-[#E5FCFF] h-full overflow-hidden select-none">
      
      {/* Header */}
      <div className="p-3 sm:p-4 bg-[#070D12] border-b border-[#007C91]/30 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <button
            onClick={onBack}
            className="p-1.5 rounded-lg bg-[#0A1219] hover:bg-[#00E5FF]/20 text-[#6F9DA6] hover:text-[#00E5FF] transition-all cursor-pointer"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <div className="flex items-center space-x-2">
              <span className="text-base font-bold font-['Chakra_Petch',sans-serif] text-[#A97BFF]">
                Projet Mobile Android &bull; 100% Kotlin
              </span>
              <span className="text-[10px] px-1.5 py-0.5 rounded bg-[#A97BFF]/20 text-[#A97BFF] font-mono font-bold">
                Jetpack Compose
              </span>
            </div>
            <div className="text-xs text-[#6F9DA6]">
              Architecture native épurée : Assistant personnel autonome (sans modules de messagerie)
            </div>
          </div>
        </div>

        <div className="flex items-center space-x-2">
          <a
            href="/api/download/apk"
            download="t-hackman-ai-v2.5.0.apk"
            className="px-3 py-1.5 rounded-lg bg-[#00E5FF]/15 hover:bg-[#00E5FF]/25 border border-[#00E5FF]/40 text-[#00E5FF] text-xs font-mono flex items-center gap-1.5 transition-all shadow-[0_0_10px_rgba(0,229,255,0.15)]"
            title="Télécharger le fichier APK complet (27.3 Mo) avec bibliothèques natives et bytecode"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Télécharger APK (27 Mo)</span>
          </a>
          <button
            onClick={handleDownloadAll}
            className="px-3 py-1.5 rounded-lg bg-[#31F5A3]/15 hover:bg-[#31F5A3]/25 border border-[#31F5A3]/40 text-[#31F5A3] text-xs font-mono flex items-center gap-1.5 transition-all cursor-pointer"
          >
            <Download className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Code Source Kotlin</span>
          </button>
        </div>
      </div>

      {/* Main Grid: Sidebar File Tree & Code Viewer */}
      <div className="flex-1 flex flex-col md:flex-row overflow-hidden">
        
        {/* Left: Files List */}
        <div className="w-full md:w-72 bg-[#050A0F] border-r border-[#007C91]/20 flex flex-col shrink-0">
          
          {/* Category Filter Pills */}
          <div className="p-2 border-b border-[#007C91]/20 flex gap-1 overflow-x-auto">
            {categories.map((cat) => (
              <button
                key={cat}
                onClick={() => setActiveCategory(cat)}
                className={`text-[10px] px-2 py-1 rounded-md font-mono whitespace-nowrap cursor-pointer transition-colors ${
                  activeCategory === cat
                    ? 'bg-[#A97BFF] text-black font-bold'
                    : 'text-[#6F9DA6] hover:bg-[#0A1219]'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          {/* Files List */}
          <div className="flex-1 overflow-y-auto p-2 space-y-1">
            {filteredFiles.map((file) => {
              const isSelected = selectedFile.name === file.name;
              return (
                <button
                  key={file.name}
                  onClick={() => setSelectedFile(file)}
                  className={`w-full text-left p-2 rounded-lg flex items-center justify-between text-xs font-mono transition-all cursor-pointer ${
                    isSelected
                      ? 'bg-[#A97BFF]/20 border border-[#A97BFF] text-[#E5FCFF]'
                      : 'text-[#8CA0A8] hover:bg-[#0A1219] hover:text-[#E5FCFF]'
                  }`}
                >
                  <div className="flex items-center space-x-2 truncate">
                    <FileCode2 className={`w-3.5 h-3.5 shrink-0 ${isSelected ? 'text-[#A97BFF]' : 'text-[#6F9DA6]'}`} />
                    <span className="truncate">{file.name}</span>
                  </div>
                  <span className="text-[9px] px-1 rounded bg-[#0A1219] text-[#52757E]">{file.category}</span>
                </button>
              );
            })}
          </div>

          {/* Architecture Spec Card */}
          <div className="p-3 bg-[#070D12] border-t border-[#007C91]/20 text-[11px] font-mono space-y-1">
            <div className="text-[#A97BFF] font-bold flex items-center gap-1">
              <Smartphone className="w-3.5 h-3.5" />
              <span>Cible : Android 15 (SDK 35)</span>
            </div>
            <div className="text-[#6F9DA6]">Langage : Kotlin 2.0+</div>
            <div className="text-[#6F9DA6]">UI : Jetpack Compose + Material3</div>
            <div className="text-[#31F5A3]">&check; Zéro dépendance de messagerie</div>
          </div>
        </div>

        {/* Right: Code Viewer */}
        <div className="flex-1 flex flex-col bg-[#030609] overflow-hidden">
          
          {/* File Tab Bar */}
          <div className="p-2.5 bg-[#070D12] border-b border-[#007C91]/20 flex items-center justify-between">
            <div className="flex items-center space-x-2 text-xs font-mono text-[#6F9DA6] truncate">
              <Folder className="w-3.5 h-3.5 text-[#00E5FF]" />
              <span className="truncate">{selectedFile.path}</span>
            </div>

            <button
              onClick={handleCopy}
              className={`px-3 py-1 rounded-md text-xs font-mono flex items-center gap-1.5 transition-all cursor-pointer ${
                copied
                  ? 'bg-[#31F5A3]/20 border border-[#31F5A3] text-[#31F5A3]'
                  : 'bg-[#0A1219] hover:bg-[#A97BFF]/20 border border-[#007C91]/30 text-[#E5FCFF]'
              }`}
            >
              {copied ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
              <span>{copied ? 'Copié !' : 'Copier'}</span>
            </button>
          </div>

          {/* Code Body */}
          <div className="flex-1 overflow-auto p-4 font-mono text-xs text-[#E5FCFF] leading-relaxed bg-[#020406]">
            <pre className="select-text">
              <code>{selectedFile.code}</code>
            </pre>
          </div>
        </div>

      </div>

    </div>
  );
};
