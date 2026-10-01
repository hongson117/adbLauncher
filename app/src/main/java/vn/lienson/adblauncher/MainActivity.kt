package vn.lienson.adblauncher

import android.content.ComponentName
import android.content.Intent
import java.net.NetworkInterface
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var tvAdbStatus: TextView
    private lateinit var tvAdbInstruction: TextView
    private lateinit var btnOpenDevOptions: Button
    private lateinit var btnOpenAbout: Button
    private lateinit var btnOpenStockSettings: Button
    private lateinit var btnEnableAdbTcp: Button
    private lateinit var btnCheckAdb: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAdbStatus = findViewById(R.id.tvAdbStatus)
        tvAdbInstruction = findViewById(R.id.tvAdbInstruction)
        btnOpenDevOptions = findViewById(R.id.btnOpenDevOptions)
        btnOpenAbout = findViewById(R.id.btnOpenAbout)
        btnOpenStockSettings = findViewById(R.id.btnOpenStockSettings)
        btnEnableAdbTcp = findViewById(R.id.btnEnableAdbTcp)
        btnCheckAdb = findViewById(R.id.btnCheckAdb)

        btnOpenDevOptions.setOnClickListener {
            openDeveloperOptions()
        }

        btnOpenAbout.setOnClickListener {
            openAboutDevice()
        }

        btnOpenStockSettings.setOnClickListener {
            openStockSettings()
        }

        btnEnableAdbTcp.setOnClickListener {
            enableAdbTcpCommands()
        }

        btnCheckAdb.setOnClickListener {
            checkAdbPort()
        }
    }

    override fun onResume() {
        super.onResume()
        checkAdbPort()
    }

    private fun checkAdbPort() {
        tvAdbStatus.text = "🔍 Đang kiểm tra cổng ADB 5555..."
        tvAdbStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_orange))

        thread {
            var isOpen = false
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress("127.0.0.1", 5555), 1000)
                socket.close()
                isOpen = true
            } catch (e: Exception) {
                isOpen = false
            }

            val ip = getLocalIpAddress()

            runOnUiThread {
                if (isOpen) {
                    tvAdbStatus.text = "🟢 CỔNG ADB 5555 ĐÃ MỞ THÀNH CÔNG!"
                    tvAdbStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_green))
                    tvAdbInstruction.text = "Lệnh kết nối từ máy tính: adb connect $ip:5555"
                } else {
                    tvAdbStatus.text = "🔴 CỔNG ADB 5555 ĐANG ĐÓNG"
                    tvAdbStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_orange))
                    tvAdbInstruction.text = "Hãy bấm Nút 1 để Bật 'Gỡ lỗi USB'. IP thiết bị: $ip"
                }
            }
        }
    }

    private fun openDeveloperOptions() {
        val intents = listOf(
            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
            Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.device.development.DevelopmentSettingsActivity")),
            Intent().setComponent(ComponentName("com.google.android.tv.settings", "com.google.android.tv.settings.device.development.DevelopmentSettingsActivity")),
            Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings")),
            Intent("com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS")
        )

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(this, "Đang mở Tùy chọn nhà phát triển...", Toast.LENGTH_SHORT).show()
                return
            } catch (e: Exception) {
                // Thử intent tiếp theo
            }
        }
        Toast.makeText(this, "Chưa kích hoạt quyền Nhà phát triển. Hãy bấm Nút 2 để mở khóa trước!", Toast.LENGTH_LONG).show()
    }

    private fun openAboutDevice() {
        val intents = listOf(
            Intent(Settings.ACTION_DEVICE_INFO_SETTINGS),
            Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.device.about.AboutActivity")),
            Intent().setComponent(ComponentName("com.google.android.tv.settings", "com.google.android.tv.settings.device.about.AboutActivity")),
            Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.DeviceInfoSettings")),
            Intent("android.settings.DEVICE_INFO_SETTINGS")
        )

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(this, "Hãy tìm dòng 'Số bản dựng' và bấm OK 7 lần!", Toast.LENGTH_LONG).show()
                return
            } catch (e: Exception) {
                // Thử intent tiếp theo
            }
        }
        Toast.makeText(this, "Không thể mở trực tiếp Giới thiệu thiết bị.", Toast.LENGTH_SHORT).show()
    }

    private fun openStockSettings() {
        val intents = listOf(
            Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings")),
            Intent().setComponent(ComponentName("com.google.android.tv.settings", "com.google.android.tv.settings.MainSettings")),
            Intent(Settings.ACTION_SETTINGS),
            Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings"))
        )

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(this, "Đang mở Cài đặt gốc Android...", Toast.LENGTH_SHORT).show()
                return
            } catch (e: Exception) {
                // Thử tiếp
            }
        }
        Toast.makeText(this, "Không thể mở Cài đặt gốc.", Toast.LENGTH_SHORT).show()
    }

    private fun enableAdbTcpCommands() {
        Toast.makeText(this, "Đang gửi lệnh kích hoạt ADB TCP 5555...", Toast.LENGTH_SHORT).show()
        thread {
            try {
                Runtime.getRuntime().exec(arrayOf("setprop", "service.adb.tcp.port", "5555")).waitFor()
                Runtime.getRuntime().exec(arrayOf("settings", "put", "global", "adb_enabled", "1")).waitFor()
                Runtime.getRuntime().exec(arrayOf("settings", "put", "global", "adb_wifi_enabled", "1")).waitFor()
                Runtime.getRuntime().exec(arrayOf("stop", "adbd")).waitFor()
                Runtime.getRuntime().exec(arrayOf("start", "adbd")).waitFor()
            } catch (e: Exception) {
                // pass
            }
            Thread.sleep(1500)
            checkAdbPort()
        }
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addrs = iface.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        return addr.hostAddress ?: ""
                    }
                }
            }
        } catch (e: Exception) {
            // pass
        }
        return "192.168.1.174"
    }
}
