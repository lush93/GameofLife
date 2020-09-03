package com.nogotech.gameoflife.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nogotech.gameoflife.R
import com.nogotech.gameoflife.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)

        binding.homeViewModel = homeViewModel

        binding.lifecycleOwner = this

        homeViewModel.stateCount.observe(viewLifecycleOwner, Observer { stateCount ->
            if(stateCount > 0){
                binding.gameView.nextState()
            }
        })

        binding.startButton.setOnClickListener{
            if(homeViewModel.isRunning.value!!) homeViewModel.pause()
            else homeViewModel.start()
        }

        binding.seekBar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                homeViewModel.frameRate.value = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        return binding.root
    }
}