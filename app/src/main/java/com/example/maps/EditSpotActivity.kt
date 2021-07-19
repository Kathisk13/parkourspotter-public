package com.example.maps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.example.maps.utils.*
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import kotlinx.android.synthetic.main.spot_details_edit.*

/**
 * Activity to change or create a spot
 */
class EditSpotActivity: AppCompatActivity() {
    // Id of the spot, also used as the name of corresponding image
    var spotId = "default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.spot_details_edit)

        val spotName: TextView = findViewById(R.id.spot_header_edit)
        val spotDescription: TextView = findViewById(R.id.spot_description_edit)
        val saveButton: ImageButton = findViewById(R.id.save)

        val bundle: Bundle? = intent.extras
        // point clicked on the map to create a new spot at (when coming from the map)
        var pointClicked: Point? = null
        //index of the shown spot (when coming from the DetailsActivity)
        var currentSpotPos = 0

        if (bundle != null) {
            if(bundle.getSerializable("point") != null)
                pointClicked = bundle.getSerializable("point") as Point
            currentSpotPos = bundle.getInt("spot")
        }

        val spotFile = readExternalFile(this, SPOTSFILE_NAME)
        val collection = FeatureCollection.fromJson(spotFile)
        // if coming from the map, save the spot when clicking on save
        if(pointClicked != null) {
            saveButton.setOnClickListener {
                clickSaveButton(collection, pointClicked)
            }
            //disable deletion of the spot
            delete.isVisible = false
            delete_label.isVisible = false
        } else {
            //if coming from the DetailsActivity, get the properties of the spot and display them
            //enable deletion of the spot
            delete.isVisible = true
            delete_label.isVisible = true
            val spot = collection.features()?.get(currentSpotPos)
            if (spot != null) {
                spotName.text = spot.getStringProperty("Name")
                spotDescription.text = spot.getStringProperty("Beschreibung")
                spotId = spot.getStringProperty("id")
                setImage(this, spotId, detail_image_edit)
                saveButton.setOnClickListener { clickChangeButton(currentSpotPos, collection)}
            }
        }
        editPicture_edit.setOnClickListener { getPermissions() }
        cancel.setOnClickListener { cancelChanges() }
        delete.setOnClickListener { deleteSpot(currentSpotPos, spotId, collection) }
    }

    /**
     * Don't save the changes made to the text of the spot and go back to the SpotlistActivity
     */
    private fun cancelChanges(){
        val intent = Intent(this, SpotlistActivity::class.java)
        startActivity(intent)
    }

    /**
     * Delete the spot from the list
     */
    private fun deleteSpot(spotPos: Int, spotId: String, collection: FeatureCollection){
        collection.features()?.removeAt(spotPos)
        println("go to spotlist")
        writeExternalFile(this, SPOTSFILE_NAME, collection.toJson())
        val intent = Intent(this, SpotlistActivity::class.java)
        val file = getPhotoFile(this, spotId)
        if(!file.delete())
            Toast.makeText(this, "Das Löschen des Fotos vom Spot hat leider nicht funktioniert", Toast.LENGTH_LONG).show()
        startActivity(intent)
    }

    /**
     * Save the spot and then go back to the list of the spots
     */
    private fun clickSaveButton(collection: FeatureCollection, point: Point){
        var features = collection.features()
        val name = spot_header_edit.text.toString()
        val description = spot_description_edit.text.toString()
        var properties = JsonObject()
        properties.addProperty("Name", name)
        properties.addProperty("Beschreibung", description)
        val id = "$name$description".hashCode()
        properties.addProperty("id", id)
        if(fileExists(this, "default", Environment.DIRECTORY_PICTURES)) {
            val default = getPhotoFile(this, "default")
            val newPicture = getPhotoFile(this, id.toString())
            if (!default.renameTo(newPicture))
                Toast.makeText(this, "There was a problem saving the picture", Toast.LENGTH_SHORT)
                    .show()
        }
        val newFeature = Feature.fromGeometry(point, properties)
        features?.add(newFeature)
        writeExternalFile(this,SPOTSFILE_NAME, "${collection.toJson()}")
        Toast.makeText(this,"Saved the spot", Toast.LENGTH_LONG).show()
        val intent = Intent(this, SpotlistActivity::class.java)
        startActivity(intent)
    }

    /**
     * Random number to recognize the request
     */
    private val cameraRequest = 1888

    /**
     * Opens the camera app and stores the picture
     * Camera actions modified from: https://medium.com/developer-student-clubs/android-kotlin-camera-using-gallery-ff8591c26c3e
     */
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

    /**
     * Save the current name and description to the spot at index spotPos in the featurecollection
     */
    private fun clickChangeButton(spotPos: Int, collection: FeatureCollection){
        var spot = collection.features()?.get(spotPos)
        val name = spot_header_edit.text.toString()
        val description = spot_description_edit.text.toString()
        spot?.addStringProperty("Name", name)
        spot?.addStringProperty("Beschreibung", description)
        println("collection: $collection")
        writeExternalFile(this,SPOTSFILE_NAME, collection.toJson())
        Toast.makeText(this,"Saved the spot", Toast.LENGTH_LONG).show()
        val intent = Intent(this, SpotlistActivity::class.java)
        startActivity(intent)
    }
}
