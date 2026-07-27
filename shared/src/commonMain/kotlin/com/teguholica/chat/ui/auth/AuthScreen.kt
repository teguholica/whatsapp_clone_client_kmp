package com.teguholica.chat.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val otp by viewModel.otp.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Authenticated) {
            onAuthenticated()
        }
    }

    when (val state = uiState) {
        is AuthUiState.Loading -> LoadingView()
        is AuthUiState.PhoneInput -> PhoneInputScreen(
            phone = phone,
            onPhoneChange = viewModel::updatePhone,
            onRequestOtp = viewModel::requestOtp,
            error = null,
        )
        is AuthUiState.OtpSent -> OtpInputScreen(
            otp = otp,
            onOtpChange = viewModel::updateOtp,
            onVerifyOtp = viewModel::verifyOtp,
            error = null,
        )
        is AuthUiState.OtpError -> OtpInputScreen(
            otp = otp,
            onOtpChange = viewModel::updateOtp,
            onVerifyOtp = viewModel::verifyOtp,
            error = state.message,
        )
        is AuthUiState.PhoneError -> PhoneInputScreen(
            phone = phone,
            onPhoneChange = viewModel::updatePhone,
            onRequestOtp = viewModel::requestOtp,
            error = state.message,
        )
        is AuthUiState.Authenticated -> { }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PhoneInputScreen(
    phone: String,
    onPhoneChange: (String) -> Unit,
    onRequestOtp: () -> Unit,
    error: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = "Masukkan nomor telepon",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Kami akan mengirimkan kode OTP",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.all { c -> c.isDigit() || c == '+' }) onPhoneChange(it) },
            label = { Text("Nomor telepon") },
            placeholder = { Text("+628123456789") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onRequestOtp,
            enabled = phone.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Dapatkan kode OTP")
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun OtpInputScreen(
    otp: String,
    onOtpChange: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    error: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = "Masukkan kode OTP",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Kode OTP telah dikirim ke nomor Anda",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 6) onOtpChange(it) },
            label = { Text("Kode OTP") },
            placeholder = { Text("123456") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onVerifyOtp,
            enabled = otp.length == 6,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Verifikasi")
        }

        Spacer(Modifier.weight(1f))
    }
}
