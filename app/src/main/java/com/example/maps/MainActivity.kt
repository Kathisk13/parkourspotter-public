package com.example.maps

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.maps.utils.fileExists
import com.example.maps.utils.mapboxMap
import com.example.maps.utils.setCameraPos
import com.example.maps.utils.writeExternalFile
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Name of the geojson file where the spots will be stored
 */
const val SPOTSFILE_NAME = "spots.geojson"

class MainActivity : AppCompatActivity(), OnMapClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        close_button.setOnClickListener { closePopup() }
        mapboxMap = mapView.getMapboxMap()
        mapboxMap.addOnMapClickListener(this@MainActivity)

        //if the user clicked on the "Go to Map" button in the Detailsactivity, move the camera to the parkourspot
        var currentSpot: Point? = null
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            currentSpot = bundle.getSerializable("point") as Point
        }
        if(currentSpot != null){
            setCameraPos(currentSpot)
        }

        //if it doesn't yet exists, create the file to store spots, so there are no errors and the user has an example
        if (!fileExists(this, SPOTSFILE_NAME, null)) {
            writeExternalFile(
                this, SPOTSFILE_NAME, "{\n" +
                        "    \"type\": \"FeatureCollection\",\n" +
                        "    \"features\": [\n" +
                        "        {\n" +
                        "            \"type\": \"Feature\",\n" +
                        "            \"properties\": {\n" +
                        "                \"Name\": \"Raschplatz\",\n" +
                        "                \"Beschreibung\": \"Gut für Precision Jumps und es gibt eine leicht schräge Mauer, so dass in verschiedenen Höhen Wallclimbs, vaults und cathangs ausprobiert werden können. Leider ist die Mauer ziemlich rau.\",\n" +
                        "                \"id\": \"0\"\n" +
                        "            },\n" +
                        "            \"geometry\": {\n" +
                        "                \"type\": \"Point\",\n" +
                        "                \"coordinates\": [\n" +
                        "                  9.760558605194092,\n" +
                        "                  52.346953491760125\n" +
                        "                ]\n" +
                        "            }\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}"
            )
        }
    }

    /**
     * Closes the help popup
     */
    private fun closePopup(){
        close_button.isVisible = false
        help.isVisible = false
    }

    /**
     * Starts the activity to create a new spot
     */
    override fun onMapClick(point: Point): Boolean {
        val intent = Intent(this, EditSpotActivity::class.java)
        intent.putExtra("point", point)
        startActivity(intent)
        return true
    }
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    /**
     * Starts the activity showing the list of spots
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val myIntent = Intent(applicationContext, SpotlistActivity::class.java)
        startActivityForResult(myIntent, 0)
        return true
    }

    /**
     * Creates the menu in the actionbar
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }
}