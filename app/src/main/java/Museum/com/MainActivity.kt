package Museum.com

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import com.here.android.mpa.mapping.Map
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.AndroidXMapFragment
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    // map embedded in the map fragment
    private var map: Map? = null

    // map fragment embedded in this activity
    private var mapFragment: AndroidXMapFragment? = null

    var gPosition: GeoPosition? = null


    //permissions request code
    val REQUEST_CODE_ASK_PERMISSIONS = 1


    // Permissions that need to be explicitly requested from end user.
    val REQUIRED_SDK_PERMISSIONS = arrayOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    lateinit var posManager: PositioningManager

    var paused = false

    var positionListener = object: PositioningManager.OnPositionChangedListener {
        override fun onPositionUpdated(
            p0: PositioningManager.LocationMethod?,
            p1: GeoPosition?,
            p2: Boolean
        ) {
            if (!paused){
                gPosition = p1
                map?.setCenter(p1!!.coordinate, Map.Animation.NONE)
            }
        }

        override fun onPositionFixChanged(
            p0: PositioningManager.LocationMethod?,
            p1: PositioningManager.LocationStatus?
        ) {
        }
    }

    override fun onResume() {
        super.onResume()
        paused = false
        if (posManager != null){
            posManager.start(PositioningManager.LocationMethod.GPS_NETWORK)
        }
    }

    override fun onPause() {
        if (posManager != null)
            posManager.stop()
        super.onPause()
        paused = true
    }

    override fun onDestroy() {
        if (posManager != null)
            posManager.removeListener(positionListener)
        map = null
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        setContentView(R.layout.activity_main)
    }

    private fun initialize() {
        setContentView(R.layout.activity_main)

        // Search for the map fragment to finish setup by calling init().
        mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapfragment) as AndroidXMapFragment?

        // Set up disk map cache path for this application
        // Use path under your application folder for storing the disk cache
        var succes = MapSettings.setIsolatedDiskCacheRootPath(
            "${applicationContext.getExternalFilesDir(null)}${File.separator}.here-maps", "{indoorservice}")
        mapFragment!!.init { error ->
            if (error == OnEngineInitListener.Error.NONE) {
                // retrieve a reference of the map from the map fragment
                map = mapFragment!!.map
                // Set the map center to the Vancouver region (no animation)
                map!!.setCenter(
                    GeoCoordinate(49.196261, -123.004773, 0.0),
                    Map.Animation.NONE
                )
                // Set the zoom level to the average between min and max
                map!!.zoomLevel((map!!.maxZoomLevel / map!!.minZoomLevel) / 2)
            } else {
                println("ERROR: Cannot initialize Map Fragment")
            }
        }
    }

    //Checks the dynamically-controlled permissions and requests missing permissions from end user.
    fun checkPermissions() {
        val missingPermissions: MutableList<String> = ArrayList()
        // check all required dynamic permissions
        for (permission in REQUIRED_SDK_PERMISSIONS) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            val permissions = missingPermissions
                .toTypedArray()
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS)
        } else {
            val grantResults = IntArray(REQUIRED_SDK_PERMISSIONS.size)
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED)
            onRequestPermissionsResult(
                REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                grantResults
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                var index = permissions.size - 1
                while (index >= 0) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(
                            this, "Required permission '" + permissions[index]
                                    + "' not granted, exiting", Toast.LENGTH_LONG
                        ).show()
                        finish()
                        return
                    }
                    --index
                }
                // all permissions were granted
                initialize()
            }
        }
    }
}
