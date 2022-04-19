package com.example.airquality

import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import java.lang.Exception
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.PackageManagerCompat

// LocationProvider - 사용 가능 여부를 확인하고, 2가지 경우로 생각.
/*
GPS 기반 위치 / 네트워크(WIFI) 기반 위치에 따라 아래 3가지 경우로 나눠서 반환
1. 두 위치 모두 사용 가능한 경우
 - 두 위치 요청 후 정확도 비교, 정확도가 더 높은 위치 반환

 2. 한가지 위치만 사용 가능한 경우
 - 있는 위치 요청 후 반환

 3. 두 위치 모두 사용 불가능한 경우
  - null 반환
 */

class LocationProvider(val context : Context) {

    // Location은 위도, 경도, 고도 등 위치에 관련된 정보를 가지고 있는 Class이다.
    private var location: Location? = null
    // LocationManager는 시스템 위치 서비스에 접근을 제공하는 Class이다.
    private var locationManager : LocationManager? = null

    init {
        // 초기화 시에 기본적으로 항상 위치를 가져옴.
        getLocation();
    }

    private fun getLocation() : Location? {
        try {
            // 먼저 위치 시스템 서비스를 가져옴
            locationManager = context.getSystemService(
                Context.LOCATION_SERVICE) as LocationManager

            var gpsLocation : Location? = null
            var networkLocation : Location? = null

            //GPS Provider와 Network Provider가 활성화 되어있는지 확인
            val isGPSEnabled : Boolean = locationManager!!.isProviderEnabled(
                LocationManager.GPS_PROVIDER)
            val isNetworkEnabled : Boolean = locationManager!!.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER)

            if (!isGPSEnabled && !isNetworkEnabled) {
                // 3번의 경우와 같이 GPS와 Network 둘 다 사용 불가능한 상황이면. (3번)
                return null // Null을 반환함. (사용불가이므로)
            }
            else {
                val hasFineLocationPermission =
                    ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                // ACCESS_COARSE_LOCATION보다 더 정밀하게 위치 정보를 얻어옴
                val hasCoarseLocaitonPermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    // 도시 Block 단위의 정밀도의 위치 정보를 얻기
                    )

                // 위 두개 권한이 없다면 null을 반환함.
                if ( hasFineLocationPermission !=
                        PackageManager.PERMISSION_GRANTED ||
                        hasCoarseLocaitonPermission !=
                        PackageManager.PERMISSION_GRANTED
                ) return null

                // 네트워크를 통한 위치 파악이 가능한 경우에 위치를 가져옴
                if (isNetworkEnabled) {
                    networkLocation =
                        locationManager?.getLastKnownLocation(
                            LocationManager.NETWORK_PROVIDER)
                }
                // GPS를 통한 위치 파악이 가능한 경우에 위치를 가져옴
                if (isGPSEnabled) {
                    gpsLocation =
                        locationManager?.getLastKnownLocation(
                            LocationManager.GPS_PROVIDER)
                }

                if(gpsLocation != null && networkLocation !=null) {
                    // 두개 위치가 모두 null이 아닌, 정보를 갖고있는 경우 정확도가 높은것으로 선택함. (1번)
                    if(gpsLocation.accuracy > networkLocation.accuracy) {
                        location = gpsLocation
                        return gpsLocation
                    } else {
                        location = networkLocation
                        return networkLocation
                    }
                } else {
                    // 가능한 위치 정보가 1개만 있는 2번의 경우
                    if (gpsLocation != null) {
                        location = gpsLocation
                    }
                    if (networkLocation != null) {
                        location = networkLocation
                    }
                }

            }
        } catch(e: Exception) {
            e.printStackTrace() // 에러 출력
        }
        return location
    }

    // 위도 정보를 가져오는 함수 (Latitude)
    fun getLocationLatitude() : Double {
        return location?.latitude ?: 0.0 // 참이면 location?.latitude를 통해 현재 값을 가져오고, null이면 0.0 반환
    }
    // 경도 정보를 가져오는 함수 (Longitude)
    fun getLocationLongitude() : Double {
        return location?.longitude ?: 0.0 // 참이면 현재 값을 가져오고, null이면 0.0 반환
    }

}
