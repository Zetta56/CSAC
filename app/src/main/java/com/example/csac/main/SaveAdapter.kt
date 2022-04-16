package com.example.csac.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.csac.R
import com.example.csac.models.Clicker
import com.example.csac.models.Save
import com.example.csac.overlay.OverlayService
import com.example.csac.overlay.SavePopup
import kotlinx.serialization.json.Json
import java.io.File

class SaveAdapter(
    private val activity: Activity,
    private val fileNames: ArrayList<String>,
    selected: String
) : RecyclerView.Adapter<SaveAdapter.ViewHolder>() {

    private var selectedSaveName = selected
    private var selectedPosition = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val saveName: TextView = view.findViewById(R.id.saveName)
        val check: ImageView = view.findViewById(R.id.check)
        val renameButton: ImageButton = view.findViewById(R.id.renameButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.save_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = File("${activity.filesDir}/saves/${fileNames[position]}")
        val saveName = Json.decodeFromString(Save.serializer(), file.readText()).name
        if(selectedSaveName == saveName) {
            holder.check.visibility = View.VISIBLE
            selectedPosition = holder.adapterPosition
        } else {
            holder.check.visibility = View.GONE
        }
        holder.saveName.text = saveName

        holder.saveName.setOnClickListener {
            if(selectedSaveName == saveName) deselectSave(position) else selectSave(position, saveName)
        }
        holder.renameButton.setOnClickListener {
            SavePopup(activity, saveName, true, fun(name) { renameSave(position, name) })
        }
        holder.deleteButton.setOnClickListener { deleteSave(position) }
    }

    override fun getItemCount(): Int {
        return fileNames.size
    }

    private fun selectSave(position: Int, saveName: String) {
        val preferences = activity.getPreferences(Context.MODE_PRIVATE)
        preferences.edit().putString("saveName", saveName).apply()
        selectedSaveName = saveName
        notifyItemChanged(position)
        notifyItemChanged(selectedPosition)
        // selectedPosition must be changed last to deselect the previous save
        selectedPosition = position
        reloadOverlay()
    }

    private fun deselectSave(position: Int) {
        val preferences = activity.getPreferences(Context.MODE_PRIVATE)
        preferences.edit().putString("saveName", "").apply()
        selectedSaveName = ""
        selectedPosition = -1
        notifyItemChanged(position)
    }

    private fun renameSave(position: Int, name: String) {
        // Delete save file
        val oldFile = File("${activity.filesDir}/saves/${fileNames[position]}")
        val save = Json.decodeFromString(Save.serializer(), oldFile.readText())
        save.name = name
        oldFile.delete()

        // Create new save file
        val newFile = File("${activity.filesDir}/saves/${name}")
        newFile.writeText(Json.encodeToString(Save.serializer(), save))
        fileNames[position] = name
        notifyItemChanged(position)
    }

    private fun deleteSave(position: Int) {
        val name = fileNames[position]
        val file = File("${activity.filesDir}/saves/${name}")
        file.delete()
        fileNames.remove(name)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }

    private fun reloadOverlay() {
        // OverlayService can only run if permissions have already been granted
        if(OverlayService.isRunning()) {
            val overlayIntent = Intent(activity, OverlayService::class.java)
            val file = File("${activity.filesDir}/saves/${selectedSaveName}")
            val selectedSave = Json.decodeFromString(Save.serializer(), file.readText())
            val clickers = selectedSave.clickers.map { c -> Clicker(c) }
            overlayIntent.putParcelableArrayListExtra("clickers", ArrayList(clickers))

            activity.stopService(overlayIntent)
            if (Build.VERSION.SDK_INT >= 26) {
                activity.startForegroundService(overlayIntent)
            } else {
                activity.startService(overlayIntent)
            }
        }
    }
}