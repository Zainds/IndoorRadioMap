package Museum.com

import android.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.here.android.mpa.common.MapSettings
import com.here.android.mpa.common.OnEngineInitListener
import com.here.android.mpa.mapping.AndroidXMapFragment


class MainActivity : AppCompatActivity() {

    // map embedded in the map fragment
    private var map: Map<*, *>? = null

    // map fragment embedded in this activity
    private var mapFragment: AndroidXMapFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    private fun initialize() {
        setContentView(R.layout.activity_main)

        // Search for the map fragment to finish setup by calling init().
        mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapfragment) as AndroidXMapFragment?

        // Set up disk map cache path for this application
        // Use path under your application folder for storing the disk cache
        MapSettings.setDiskCacheRootPath(
            applicationContext.getExternalFilesDir(
                null
            ) + File.separator.toString() + ".here-maps"
        )
        mapFragment!!.init { error ->
            if (error == OnEngineInitListener.Error.NONE) {
                // retrieve a reference of the map from the map fragment
                map = mapFragment!!.map
                // Set the map center to the Vancouver region (no animation)
                map!!.setCenter(
                    GeoCoordinate(49.196261, -123.004773, 0.0),
                    java.util.Map.Animation.NONE
                )
                // Set the zoom level to the average between min and max
                map!!.setZoomLevel((map!!.getMaxZoomLevel() + map!!.getMinZoomLevel()) / 2)
            } else {
                println("ERROR: Cannot initialize Map Fragment")
            }
        }
    }
}