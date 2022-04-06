package com.example.airquality

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.airquality.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    // 런타임 권한 요청 시 필요한 요청 코드
    private val PERMISSIONS_REQUEST_CODE = 100

    // 요청할 권한 목록 (GPS & LOCATION)
    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION )

    // 위치 서비스 요청 시 필요한 Launcher (런쳐)
    lateinit var getGPSPermissionLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAllPermissions()

    }

    private fun checkAllPermissions() {
        if(!isLocationServicesAvailable()) {
            showDialogForLocationServiceSetting();
        } else {
            isRunTimePermissionGranted();
        }
    }

    fun isLocationServicesAvailable() : Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        // 위치 서비스(GPS)가 켜져있는지 확인하는 과정인데, 위치 서비스는 GPS 또는 네트워크를 Provider로 설정할 수 있다.
        // GPS Provider => 위성 신호를 수신하여 위치를 판독
        // Network Provider => WIFI 네트워크, 기지국 등으로부터 위치를 구함.
        // 때문에, 두 Provider 중 하나가 있다면 true를 반환하도록 함.
    }

    // 권한 요청을 하고난 후의 결과값은 Activity에서 구현되어 있는 아래 함수를 통해 오버라이드하여 처리함.
    // 여기서 모든 Permission이 허용되었는지 확인하고, 만약 허용되지 않은 권한이 있다면 앱을 종료함.
    override fun onRequestPermissionsResult( // 요청 권한에 대한 결과 function
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE와 같고, 요청한 퍼미션 개수 (2개)만큼 수신되었다면
            var checkResult = true

            // 모든 퍼미션을 허용했는지 체크
            for (result in grantResults) {
                if(result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }
            if(checkResult) {
                // 위치값을 가져올 수 있음.
            } else {
                // 퍼미션이 거부되었다면 앱 종료
                Toast.makeText(
                    this@MainActivity,
                    "권한이 거부 되었습니다. 앱을 다시 실행하여 권한을 허용 해 주세요.",
                    Toast.LENGTH_LONG
                        ).show()
                finish()
            }
        }
    }

    private fun isRunTimePermissionGranted() {

        // 위치 퍼미션을 가지고 있는지 체크
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

            // 권한이 한개라도 없다면 퍼미션을 요청함.
            ActivityCompat.requestPermissions(this@MainActivity,
            REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }
    }


    private fun showDialogForLocationServiceSetting() {
        // 먼저 ActivityResultLauncher를 설정 해 주고, 이 런처를 이용하여 결과값을
        // 반환해야하는 Intent를 실행할 수 있음.

        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            result -> // 결과값을 받았을때 로직
                    if(result.resultCode == Activity.RESULT_OK) {

                        // 사용자가 GPS를 활성화 시켰는지 확인하는 작업
                        if(isLocationServicesAvailable()) {
                            isRunTimePermissionGranted() // 런타임 권한 확인
                        } else {
                            // 위치 서비스가 허용되지 않아버렸다면 앱 종료
                            Toast.makeText(
                                this@MainActivity,
                                "위치 서비스를 사용할 수 없어 앱을 종료합니다.",
                                Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
        }
        val builder : AlertDialog.Builder = AlertDialog.Builder(
            this@MainActivity)  // 사용자에게 의사를 물어보는 AlertDialog 생성
        builder.setTitle("위치 서비스 활성화") // 제목 설정
        builder.setMessage( // 내용 설정
            "위치 서비스가 꺼져있습니다. 설정해야만 앱을 사용할 수 있습니다.")
        builder.setCancelable(true) // 다이얼로그의 바깥 창을 터치하면 창이 닫힘.
        builder.setPositiveButton("설정",
            DialogInterface.OnClickListener {
                    dialog, id ->
                val callGPSSettingIntent =
                    Intent(Settings. ACTION_LOCATION_SOURCE_SETTINGS)
                getGPSPermissionLauncher.launch(callGPSSettingIntent)
            })
        builder.setNegativeButton("취소",
            DialogInterface.OnClickListener {
                    dialog, id ->
                dialog.cancel()
                Toast.makeText(this@MainActivity,
                    "기기에서 위치서비스(GPS) 설정 후 사용 해 주세요.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            })
        builder.create().show() // 다이얼로그 생성 및 보여주기
        // builder.create()
        // builder.show() 합친거
    }
}









