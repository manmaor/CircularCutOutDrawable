package com.example.firebaseabtest

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.firebaseabtest.databinding.FragmentFirstBinding
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings


class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private val remoteConfig = Firebase.remoteConfig
    private val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 10
    }

    init {
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.buttonFirst.setOnClickListener {
////            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }

        // configuration of the shape for the outline
        val shape = ShapeAppearanceModel.Builder()
            .setAllCorners(RoundedCornerTreatment())
            .setAllCornerSizes(16f)
            .build()

        // configuration of the CutOutDrawable - replace with themed values instead of hard coded colors
        val drawable = CutoutDrawable(shape).apply {
            setStroke(4f, Color.BLACK)
            fillColor = ColorStateList.valueOf(Color.WHITE)
        }

        drawable.strokeWidth = 25f

        // content is the view with @+id/content
        binding.content.background = drawable
        // label is the view with @+id/label
        binding.label.addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
            // offset the position by the margin of the content view
            val realLeft = left - binding.content.left
            val realTop = top - binding.content.top
            val realRigth = right - binding.content.left
            val realBottom = bottom - binding.content.top
            // update the cutout part of the drawable
            drawable.setCutout(
                realLeft.toFloat(),
                realTop.toFloat(),
                realRigth.toFloat(),
                realBottom.toFloat()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}