package com.example.maps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.maps.utils.fileExists
import com.example.maps.utils.getPhotoFile
import com.example.maps.utils.readExternalFile
import com.example.maps.utils.setImage
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import kotlinx.android.synthetic.main.spot_details.*

/**
 * Activity showing the details of one spot
 */
class DetailsActivity: AppCompatActivity() {

    // Random number, so the request is recognized
    private val cameraRequest = 1888
    // Id of the spot, also used as the file name of the picture
    private lateinit var spotId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.spot_details)

        val spotName: TextView = findViewById(R.id.spot_header)
        val spotDescription: TextView = findViewById(R.id.spot_description)
        val goToMap: ImageButton = findViewById(R.id.save)

        //index of the shown spot in the feature list; use 0 if there is an error getting the actual index
        var currentSpotPos = 0
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            currentSpotPos = bundle.getInt("spot")
        }

        val spotFile = readExternalFile(this, SPOTSFILE_NAME)
        val featureCollection = FeatureCollection.fromJson(spotFile)
        val spotList = featureCollection.features()

        if (spotList?.isNotEmpty() == true) {
            val spot = spotList[currentSpotPos]
            if (spot != null) {
                spotId = spot.getStringProperty("id")
                //try to set the image to the corresponding spot image, if there is none, use the default
                if(fileExists(this, spotId, Environment.DIRECTORY_PICTURES)){
                    val file = getPhotoFile(this, spotId)
                    val uri: Uri =
                        FileProvider.getUriForFile(this, this.applicationContext.packageName + ".provider", file)
                    detail_image.setImageURI(uri)
                } else {
                    detail_image.setImageResource(R.drawable.ic_launcher_background)
                }
                //display the name and description of the spot
                spotName.text = spot.getStringProperty("Name")
                spotDescription.text = spot.getStringProperty("Beschreibung")

                editPicture.setOnClickListener { getPermissions() }
                goToMap.setOnClickListener { clickMapButton(spot.geometry() as Point) }
                edit_button.setOnClickListener { clickEditSpot(currentSpotPos)}
            }
        }
    }

    /**
     * Opens the camera app and stores the picture
     * Camera actions modified from: https://medium.com/developer-student-clubs/android-kotlin-camera-using-gallery-ff8591c26c3e
     to*/
    private fun getPermissions() {
        if (!hasPermissions()) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), cameraRequest)
        } else
            takePhoto()
    }
    private fun takePhoto(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = getPhotoFile(this, spotId)
        val uri: Uri =
            FileProvider.getUriForFile(this, this.applicationContext.packageName + ".provider", file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, cameraRequest)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == cameraRequest && hasPermissions()) {
            takePhoto()
        }
    }

    /** Convenience method used to check if all permissions required by this app are granted */
    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraRequest) {
            setImage(this, spotId, detail_image)
        }
    }

    /**
     * Go to the spot location on the map
     */
    private fun clickMapButton(point: Point){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("point", point)
        startActivity(intent)
    }

    /**
     * Open Activity to edit the spot
     */
    private fun clickEditSpot(spot: Int){
        val intent = Intent(this, EditSpotActivity::class.java)
        intent.putExtra("spot", spot)
        startActivity(intent)
    }

    /**
     * Go to list of the spots when the bar icon in the menu is clicked
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val myIntent = Intent(applicationContext, SpotlistActivity::class.java)
        startActivityForResult(myIntent, 0)
        return true
    }

    /**
     * Create the menu
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }
}
