package com.example.csac.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.csac.overlay.OverlayService
import com.example.csac.R
import com.example.csac.databinding.FragmentMainBinding
import com.example.csac.getDefaultPreferences
import com.example.csac.models.Save
import com.google.gson.Gson
import java.io.File

class MainFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var navController: NavController
    private lateinit var binding: FragmentMainBinding
    private lateinit var overlayIntent: Intent
    private var selectedSave: Save? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = activity as MainActivity
        mainActivity.supportActionBar?.title = "CSAC"
        mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
        loadSave()

        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(LayoutInflater.from(mainActivity))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        // :: operator gets OverlayService's metadata
        overlayIntent = Intent(mainActivity.applicationContext, OverlayService::class.java)

        // Configure UI
        val powerImage = if(OverlayService.isRunning()) R.drawable.power_on else R.drawable.power_off
        binding.powerButton.setImageResource(powerImage)
        if(selectedSave != null) {
            val ellipsis = if(selectedSave!!.name.length > 12) "..." else ""
            binding.selectedSave.text = String.format("Selected: %s%s", selectedSave!!.name.take(12), ellipsis)
        } else {
            binding.selectedSave.setText(R.string.default_selected_save)
        }

        // Add click listeners
        binding.powerButton.setOnClickListener { toggleOverlay() }
        binding.savesButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("selected", selectedSave?.name)
            navController.navigate(R.id.action_mainFragment_to_savesFragment, bundle)
        }
        binding.settingsButton.setOnClickListener {
            navController.navigate(R.id.action_mainFragment_to_settingsFragment)
        }
    }

    private fun loadSave() {
        val preferences = getDefaultPreferences(activity as Context)
        val saveName = preferences.getString("saveName", "")
        try {
            val file = File("${context?.filesDir}/saves/${saveName}")
            selectedSave = Gson().fromJson(file.readText(), Save::class.java)
        } catch(e: Exception) {
            selectedSave = null
            preferences.edit().putString("saveName", "").apply()
        }
    }

    private fun toggleOverlay() {
        // Check if OverlayService was running before toggling
        val isRunning = OverlayService.isRunning()
        val successful = mainActivity.toggleOverlay(!isRunning, selectedSave)
        if(successful && !isRunning) {
            binding.powerButton.setImageResource(R.drawable.power_on)
        } else {
            binding.powerButton.setImageResource(R.drawable.power_off)
        }
    }
}