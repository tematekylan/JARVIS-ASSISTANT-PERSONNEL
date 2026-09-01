package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBgCard
import com.example.ui.theme.JarvisBgSurface
import com.example.ui.theme.JarvisBgVoid
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun JarvisAuthDialog(
    onDismiss: () -> Unit,
    onSignInGoogle: (name: String, email: String) -> Unit,
    onLoginEmail: (email: String, password: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onRegisterEmail: (email: String, password: String, name: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onSignInPhone: (phone: String, name: String, onResult: (Boolean, String) -> Unit) -> Unit,
    currentUserName: String = "Sir",
    currentUserEmail: String = "",
    currentUserPhone: String = "",
    isLoggedIn: Boolean = false,
    authProvider: String = "guest",
    onSignOut: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Email/Compte, 1: Google, 2: Téléphone
    var isRegisterMode by remember { mutableStateOf(false) }

    // Email Form State
    var emailInput by remember { mutableStateOf(currentUserEmail) }
    var emailPassword by remember { mutableStateOf("") }
    var emailNameInput by remember { mutableStateOf(if (currentUserName != "Sir") currentUserName else "") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var emailStatusMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Google Form State
    var googleAccountEmail by remember { mutableStateOf(if (currentUserEmail.isNotBlank()) currentUserEmail else "tony.stark@jarvis.ai") }
    var googleAccountName by remember { mutableStateOf(if (currentUserName != "Sir") currentUserName else "Tony Stark") }

    // Phone Form State
    var phoneCountryCode by remember { mutableStateOf("+33") }
    var phoneNumberInput by remember { mutableStateOf(currentUserPhone.removePrefix("+33").removePrefix("+237")) }
    var phoneNameInput by remember { mutableStateOf(if (currentUserName != "Sir") currentUserName else "") }
    var otpSent by remember { mutableStateOf(false) }
    var otpCodeInput by remember { mutableStateOf("") }
    var phoneStatusMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = JarvisBgSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(JarvisCyan)
                        )
                        Column {
                            Text(
                                text = "AUTHENTIFICATION JARVIS",
                                color = JarvisCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "PORTAIL D'ACCÈS SÉCURISÉ STARK",
                                color = JarvisTextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = JarvisTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = JarvisBorderGlow, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // If logged in, display active identity badge
                if (isLoggedIn) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisEmerald),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(JarvisEmerald.copy(alpha = 0.2f))
                                        .border(1.dp, JarvisEmerald, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = JarvisEmerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = currentUserName,
                                        color = JarvisTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val contactInfo = if (currentUserEmail.isNotBlank()) currentUserEmail else currentUserPhone
                                    Text(
                                        text = if (contactInfo.isNotBlank()) contactInfo else "Session Active",
                                        color = JarvisCyanGlow,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "AUTH: ${authProvider.uppercase()} // ACCÈS NIVEAU COMMANDANT ACTIF",
                                color = JarvisEmerald,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    onSignOut()
                                    emailStatusMessage = Pair(true, "Déconnexion effectuée avec succès.")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisCrimson.copy(alpha = 0.85f)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "SE DÉCONNECTER",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "OU CHANGER DE COMPTE :",
                        color = JarvisTextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Method Selector Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AuthTabButton(
                        title = "Email / Compte",
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1.2f)
                    )
                    AuthTabButton(
                        title = "Google SSO",
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    AuthTabButton(
                        title = "Téléphone",
                        isSelected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TAB 0: EMAIL & PASSWORD (LOGIN / REGISTER)
                if (selectedTab == 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isRegisterMode) "Création d'un nouveau compte JARVIS" else "Connexion à votre compte",
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        AnimatedVisibility(visible = isRegisterMode) {
                            OutlinedTextField(
                                value = emailNameInput,
                                onValueChange = { emailNameInput = it },
                                label = { Text("Votre Nom ou Pseudo", color = JarvisTextMuted) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = JarvisCyan) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JarvisCyan,
                                    unfocusedBorderColor = JarvisBorderGlow,
                                    focusedTextColor = JarvisTextPrimary,
                                    unfocusedTextColor = JarvisTextPrimary
                                ),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                emailStatusMessage = null
                            },
                            label = { Text("Adresse Email", color = JarvisTextMuted) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = JarvisCyan) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisCyan,
                                unfocusedBorderColor = JarvisBorderGlow,
                                focusedTextColor = JarvisTextPrimary,
                                unfocusedTextColor = JarvisTextPrimary
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        OutlinedTextField(
                            value = emailPassword,
                            onValueChange = {
                                emailPassword = it
                                emailStatusMessage = null
                            },
                            label = { Text("Mot de passe", color = JarvisTextMuted) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = JarvisCyan) },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Afficher mot de passe",
                                        tint = JarvisTextMuted
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisCyan,
                                unfocusedBorderColor = JarvisBorderGlow,
                                focusedTextColor = JarvisTextPrimary,
                                unfocusedTextColor = JarvisTextPrimary
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        emailStatusMessage?.let { (isSuccess, msg) ->
                            Text(
                                text = msg,
                                color = if (isSuccess) JarvisEmerald else JarvisCrimson,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                isLoading = true
                                if (isRegisterMode) {
                                    onRegisterEmail(emailInput, emailPassword, emailNameInput) { success, msg ->
                                        isLoading = false
                                        emailStatusMessage = Pair(success, msg)
                                    }
                                } else {
                                    onLoginEmail(emailInput, emailPassword) { success, msg ->
                                        isLoading = false
                                        emailStatusMessage = Pair(success, msg)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_auth_email_confirm")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = JarvisBgVoid,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isRegisterMode) "VALIDER L'INSCRIPTION" else "SE CONNECTER",
                                    color = JarvisBgVoid,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(onClick = {
                                isRegisterMode = !isRegisterMode
                                emailStatusMessage = null
                            }) {
                                Text(
                                    text = if (isRegisterMode) "Déjà un compte ? Cliquez ici pour vous connecter" else "Pas de compte ? Créer un nouveau compte",
                                    color = JarvisCyanGlow,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // TAB 1: GOOGLE SIGN-IN
                if (selectedTab == 1) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Authentification Google Identity / SSO",
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Connexion directe avec synchronisation instantanée de votre profil.",
                            color = JarvisEmerald,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )

                        OutlinedTextField(
                            value = googleAccountName,
                            onValueChange = { googleAccountName = it },
                            label = { Text("Nom du Titulaire", color = JarvisTextMuted) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = JarvisCyan) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisCyan,
                                unfocusedBorderColor = JarvisBorderGlow,
                                focusedTextColor = JarvisTextPrimary,
                                unfocusedTextColor = JarvisTextPrimary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = googleAccountEmail,
                            onValueChange = { googleAccountEmail = it },
                            label = { Text("Adresse Email Google", color = JarvisTextMuted) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = JarvisCyan) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisCyan,
                                unfocusedBorderColor = JarvisBorderGlow,
                                focusedTextColor = JarvisTextPrimary,
                                unfocusedTextColor = JarvisTextPrimary
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                val email = googleAccountEmail.ifBlank { "tony.stark@jarvis.ai" }
                                val name = googleAccountName.ifBlank { "Tony Stark" }
                                onSignInGoogle(name, email)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_auth_google_confirm")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = JarvisBgVoid)
                                Text(
                                    text = "SE CONNECTER AVEC GOOGLE",
                                    color = JarvisBgVoid,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // TAB 2: PHONE NUMBER
                if (selectedTab == 2) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Authentification par Numéro Mobile & SMS",
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = phoneNameInput,
                            onValueChange = { phoneNameInput = it },
                            label = { Text("Votre Nom / Identifiant", color = JarvisTextMuted) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = JarvisCyan) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisCyan,
                                unfocusedBorderColor = JarvisBorderGlow,
                                focusedTextColor = JarvisTextPrimary,
                                unfocusedTextColor = JarvisTextPrimary
                            ),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = phoneCountryCode,
                                onValueChange = { phoneCountryCode = it },
                                label = { Text("Indicatif", color = JarvisTextMuted) },
                                modifier = Modifier.width(90.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JarvisCyan,
                                    unfocusedBorderColor = JarvisBorderGlow,
                                    focusedTextColor = JarvisTextPrimary,
                                    unfocusedTextColor = JarvisTextPrimary
                                ),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = phoneNumberInput,
                                onValueChange = {
                                    phoneNumberInput = it
                                    phoneStatusMessage = null
                                },
                                label = { Text("Numéro mobile", color = JarvisTextMuted) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = JarvisCyan) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JarvisCyan,
                                    unfocusedBorderColor = JarvisBorderGlow,
                                    focusedTextColor = JarvisTextPrimary,
                                    unfocusedTextColor = JarvisTextPrimary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }

                        if (!otpSent) {
                            Button(
                                onClick = {
                                    if (phoneNumberInput.isBlank()) {
                                        phoneStatusMessage = Pair(false, "Veuillez entrer un numéro de téléphone valide.")
                                        return@Button
                                    }
                                    otpSent = true
                                    otpCodeInput = "482910"
                                    phoneStatusMessage = Pair(true, "Code SMS transmis avec succès.")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                            ) {
                                Text(
                                    text = "ENVOYER LE CODE SMS",
                                    color = JarvisBgVoid,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            Text(
                                text = "Code de validation SMS (Exemple : 482910)",
                                color = JarvisEmerald,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            OutlinedTextField(
                                value = otpCodeInput,
                                onValueChange = { otpCodeInput = it },
                                label = { Text("Code à 6 chiffres", color = JarvisTextMuted) },
                                leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = JarvisAmber) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JarvisAmber,
                                    unfocusedBorderColor = JarvisBorderGlow,
                                    focusedTextColor = JarvisTextPrimary,
                                    unfocusedTextColor = JarvisTextPrimary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            Button(
                                onClick = {
                                    val fullPhone = "$phoneCountryCode ${phoneNumberInput.trim()}"
                                    onSignInPhone(fullPhone, phoneNameInput.trim()) { success, msg ->
                                        phoneStatusMessage = Pair(success, msg)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisEmerald),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("btn_auth_phone_confirm")
                            ) {
                                Text(
                                    text = "VALIDER ET SE CONNECTER",
                                    color = JarvisBgVoid,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        phoneStatusMessage?.let { (isSuccess, msg) ->
                            Text(
                                text = msg,
                                color = if (isSuccess) JarvisEmerald else JarvisCrimson,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisBgCard)
            .border(
                1.dp,
                if (isSelected) JarvisCyan else JarvisBorderGlow,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) JarvisCyanGlow else JarvisTextMuted,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
        )
    }
}
