package com.example.whisper_waves.ui.chat

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.whisper_waves.databinding.FragmentChatBinding
import com.example.whisper_waves.databinding.ToolbarAddonChatBinding
import com.example.whisper_waves.util.convertFileToByteArray
import java.io.File

class ChatFragment : Fragment() {
    private val selectImageIntentRequestCode = 1
    companion object {
        const val ARGS_KEY_USER_ID = "bundle_user_id"
        const val ARGS_KEY_OTHER_USER_ID = "bundle_other_user_id"
        const val ARGS_KEY_CHAT_ID = "bundle_other_chat_id"
    }
    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false
    private val requestAudioPermissionCode = 2
    val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE=123
    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(
            requireContext().applicationContext,
            requireArguments().getString(ARGS_KEY_USER_ID)!!,
            requireArguments().getString(ARGS_KEY_OTHER_USER_ID)!!,
            requireArguments().getString(ARGS_KEY_CHAT_ID)!!
        )
    }

    private lateinit var viewDataBinding: FragmentChatBinding
    private lateinit var listAdapter: MessagesListAdapter
    private lateinit var listAdapterObserver: RecyclerView.AdapterDataObserver
    private lateinit var toolbarAddonChatBinding: ToolbarAddonChatBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentChatBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            lifecycleOwner = this@ChatFragment
        }
        setHasOptionsMenu(true)

        toolbarAddonChatBinding =
            ToolbarAddonChatBinding.inflate(inflater, container, false)
                .apply { viewmodel = viewModel }
        toolbarAddonChatBinding.lifecycleOwner = this.viewLifecycleOwner

        // Initialize MediaRecorder
        mediaRecorder = MediaRecorder()

        return viewDataBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter.unregisterAdapterDataObserver(listAdapterObserver)
        mediaRecorder.release() // Release MediaRecorder here
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupCustomToolbar()
        setupListAdapter(requireContext())
        viewDataBinding.imageView.setOnClickListener(){
            this.activity?.let { it1 -> selectMedia(it1) }
        }
        viewDataBinding.audioRecordView.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                checkAudioPermissions()
            }
        }
    }


    private fun setupCustomToolbar() {
        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.customView = toolbarAddonChatBinding.root
    }

    private fun setupListAdapter(context: Context) {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            listAdapterObserver = object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    viewDataBinding.messagesRecyclerView.scrollToPosition(positionStart)
                }
            }
            listAdapter =
                MessagesListAdapter(viewModel, context,requireArguments().getString(ARGS_KEY_USER_ID)!!)
            listAdapter.registerAdapterDataObserver(listAdapterObserver)
            viewDataBinding.messagesRecyclerView.adapter = listAdapter
        } else {
            throw Exception("The viewmodel is not initialized")
        }
    }

    private fun selectMedia(activity: Activity) {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                activity,
                permissions,
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            startSelectImageIntent()

        }
    }

    private fun startSelectImageIntent() {
        val selectImageIntent = Intent(Intent.ACTION_GET_CONTENT)
        selectImageIntent.type = "*/*"
        startActivityForResult(selectImageIntent, selectImageIntentRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == selectImageIntentRequestCode) {
            data?.data?.let { uri ->
                convertFileToByteArray(requireContext(), uri).let {
                    viewModel.uploadUserImage(it)
                }
            }
        }
    }
    private var audioFileName: String = ""
    private var recordStartTime: Long = 0
    private var recordEndTime: Long = 0
    private fun startRecording() {
        recordStartTime = System.currentTimeMillis()
        Log.e("RECORD", "recordStartTime: ${recordStartTime}")
        if (!::mediaRecorder.isInitialized) {
            mediaRecorder = MediaRecorder()
        }

        // Reset MediaRecorder
        mediaRecorder.reset()

        audioFileName = "${requireContext().cacheDir}/audio_${System.currentTimeMillis()}.mp3"
        Log.d("tempfilestart","${audioFileName.chars()}")
        try {
            mediaRecorder.apply {
                Log.e("RECORD", "starting recording:")
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFileName)

                prepare()
                start()
                Log.e("RECORD", "recording")
                isRecording = true
            }
        } catch (e: Exception) {
            Log.e("RECORD", "Error starting recording: ${e.message}")
            isRecording = false
        }
    }




    private fun stopRecording() {
        recordEndTime = System.currentTimeMillis()
        Log.e("RECORD", "recordEndTime: ${recordEndTime}")
        mediaRecorder.apply {
            Log.e("RECORD", "STOPPED recording")
            stop()
            reset()
            // release() removed from here
            isRecording = false
        }

        val tempFile = File(audioFileName)
        Log.d("tempfilestop","${tempFile.name}")
        try {
            if (tempFile.exists()) {
                Log.d("FILE_CREATION", "Temporary audio file found")
                // Convert audio file to byte array
                val byteArray = tempFile.readBytes()
                // Upload byte array to server

                viewModel.uploadUserAudio(byteArray)
            } else {
                Log.e("FILE_CREATION", "Temporary audio file not found")
            }
        } catch (e: Exception) {
            Log.e("FILE_CREATION", "Error accessing audio file: ${e.message}")
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestAudioPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start recording
                startRecording()
            } else {
                // Permission denied, show message or handle accordingly
            }
        }
    }

    private fun checkAudioPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.RECORD_AUDIO
                )
            } != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions,
                requestAudioPermissionCode
            )
        } else {
            // Permission already granted, start recording
            startRecording()
        }
    }


}
