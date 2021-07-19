package com.example.maps
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.maps.utils.readExternalFile
import com.mapbox.geojson.FeatureCollection
import kotlinx.android.synthetic.main.activity_spotlist.*

/**
 * Activity displaying a list of all the added spots
 */
class SpotlistActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spotlist)
        //extract the spots from the spots.geojson file
        val spotsfile = readExternalFile(this, SPOTSFILE_NAME)
        val featureCollection = FeatureCollection.fromJson(spotsfile)
        var spotList = featureCollection.features()
        spot_list.adapter = spotList?.let { SpotAdapter(this, it) { spot -> adapterOnClick(spot) } }
        map_button.setOnClickListener { goToMap() }
    }

    /**
     * Show the details of the spot, when one is clicked
     */
    private fun adapterOnClick(spot: Int) {
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("spot", spot)
        startActivity(intent)
    }

    /**
     * Go back to the map
     */
    private fun goToMap() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}