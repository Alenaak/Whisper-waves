package com.example.whisper_waves.ui.chat

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whisper_waves.R

import com.example.whisper_waves.data.db.entity.Message
import com.example.whisper_waves.databinding.ListItemMessageReceivedBinding
import com.example.whisper_waves.databinding.ListItemMessageSentBinding

class MessagesListAdapter internal constructor(
    private val viewModel: ChatViewModel,
    private val context: Context,
    private val userId: String
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    private val holderTypeMessageReceived = 1
    private val holderTypeMessageSent = 2
    private var mediaPlayer: MediaPlayer? = null
    private var playingPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            holderTypeMessageSent -> {
                val binding = ListItemMessageSentBinding.inflate(layoutInflater, parent, false)
                SentViewHolder(binding)
            }
            holderTypeMessageReceived -> {
                val binding = ListItemMessageReceivedBinding.inflate(layoutInflater, parent, false)
                ReceivedViewHolder(binding)
            }
            else -> {
                throw Exception("Error reading holder type")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder.itemViewType) {
            holderTypeMessageSent -> {
                val sentViewHolder = holder as SentViewHolder
                sentViewHolder.bind(viewModel, message)
                setMessageStatus(sentViewHolder.binding.statusText, message.status)
                sentViewHolder.updatePlaybackState(position == playingPosition)
            }

            holderTypeMessageReceived -> {
                val receivedViewHolder = holder as ReceivedViewHolder
                receivedViewHolder.bind(viewModel, message)
                setMessageStatus(receivedViewHolder.binding.statusText, message.status)
            }
        }
    }

    private fun setMessageStatus(statusTextView: TextView, status: Message.MessageStatus) {
        statusTextView.text = when (status) {
            Message.MessageStatus.SENT -> "Sent"
            Message.MessageStatus.DELIVERED -> "Delivered"
            Message.MessageStatus.SEEN -> "Seen"
            Message.MessageStatus.NOT_SENT -> "Not Sent"
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderID != userId) {
            holderTypeMessageReceived
        } else {
            holderTypeMessageSent
        }
    }

    inner class SentViewHolder(val binding: ListItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val playButton: ImageButton = binding.pauseButton
        private val seekBar: SeekBar = binding.seekBar
        private var currentPlayingPosition: Int = -1
        private var currentSeekBar: SeekBar? = null // Store the SeekBar reference
        private var currentMediaPlayer: MediaPlayer? = null // Store the MediaPlayer reference
        private var currentAudioRef: String? = null // Store the current audio reference

        init {
            playButton.setOnClickListener {
                togglePlayback()
            }
        }


        fun bind(viewModel: ChatViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.statusText.text = getStatusText(item.status)
            binding.executePendingBindings()
            item.imageRef?.let { imageRef ->
                Glide.with(binding.root)
                    .load(imageRef)
                    .into(binding.sentImageView)
            }

        }

        fun updatePlaybackState(isPlaying: Boolean) {
            val iconResource = if (isPlaying) R.drawable.pause else R.drawable.play
            playButton.setImageResource(iconResource)
        }


         private fun togglePlayback() {
              val position = adapterPosition
              if (position == RecyclerView.NO_POSITION) return

              val message = getItem(position)
              val audioRef = message.audioRef ?: return

              if (position != currentPlayingPosition) {
                  // New audio is clicked, stop previous playback and start new one

                  if (currentMediaPlayer?.isPlaying == true) {
                      releasePreviousMediaPlayer() // Release previous MediaPlayer instance if it's playing
                  }
                  startPlayback(audioRef, position)
                  currentPlayingPosition = position
                  currentSeekBar = seekBar // Update the current SeekBar reference
                  updatePlaybackState(true)
              } else {
                  // Same audio is clicked, toggle playback
                  currentMediaPlayer?.let { player ->
                      if (player.isPlaying) {
                          player.pause()
                          updatePlaybackState(false)
                      } else {
                          player.start()
                          currentSeekBar = seekBar // Update the current SeekBar reference
                          updateSeekBar(seekBar)
                          updatePlaybackState(true)
                      }
                  }
              }
          }


        private fun startPlayback(audioRef: String, position: Int) {
            currentMediaPlayer = MediaPlayer().apply {
                setDataSource(audioRef)
                prepareAsync()

                setOnPreparedListener {
                    val duration = it.duration // Get duration in milliseconds
                    val durationSeconds = duration / 1000 // Convert duration to seconds
                    Log.d("durationSeconds","${durationSeconds}")
                    // Update the timer text in the layout
                    binding.audioDurationText.text = formatDuration(durationSeconds)

                    start()
                    currentSeekBar = seekBar // Update the current SeekBar reference
                    updateSeekBar(seekBar)
                    updatePlaybackState(true) // Update playback state after starting playback
                }

                setOnCompletionListener {
                    releaseMediaPlayer() // Release MediaPlayer when playback completes
                    updatePlaybackState(false)
                    // Reset seekbar position
                    seekBar.progress = 0
                }
            }
            currentAudioRef = audioRef // Store the current audio reference
        }


        private fun formatDuration(durationSeconds: Int): String {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }




        private fun updateSeekBar(seekBar: SeekBar) {
            val handler = Handler()
            handler.postDelayed(object : Runnable {
                override fun run() {
                    currentMediaPlayer?.let {
                        if (it.isPlaying && seekBar == currentSeekBar) { // Only update the current SeekBar
                            seekBar.max = it.duration
                            seekBar.progress = it.currentPosition
                        }
                    }
                    handler.postDelayed(this, 1000)
                }
            }, 0)
        }

        private fun releasePreviousMediaPlayer() {
            currentMediaPlayer?.apply {
                release()
            }
        }

        private fun resetMediaPlayer(audioRef: String) {
            currentMediaPlayer?.apply {
                reset()
                setDataSource(audioRef)
                prepareAsync()
            }
        }
    }


    inner class ReceivedViewHolder(val binding: ListItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val playButton: ImageButton = binding.pauseButton
        private val seekBar: SeekBar = binding.seekBar
        private var currentPlayingPosition: Int = -1
        private var currentSeekBar: SeekBar? = null // Store the SeekBar reference
        private var currentMediaPlayer: MediaPlayer? = null // Store the MediaPlayer reference
        private var currentAudioRef: String? = null // Store the current audio reference

        init {
            playButton.setOnClickListener {
                togglePlayback()
            }
        }

        fun bind(viewModel: ChatViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.statusText.text = getStatusText(item.status)
            binding.executePendingBindings()
            item.imageRef?.let { imageRef ->
                Glide.with(binding.root)
                    .load(imageRef)
                    .into(binding.sentImageView)
            }
        }

        fun updatePlaybackState(isPlaying: Boolean) {
            val iconResource = if (isPlaying) R.drawable.pause else R.drawable.play
            playButton.setImageResource(iconResource)
        }

        private fun togglePlayback() {
            val position = adapterPosition
            if (position == RecyclerView.NO_POSITION) return

            val message = getItem(position)
            val audioRef = message.audioRef ?: return

            if (position != currentPlayingPosition) {
                // New audio is clicked, stop previous playback and start new one
                if (currentMediaPlayer?.isPlaying == true) {
                    releasePreviousMediaPlayer() // Release previous MediaPlayer instance if it's playing
                }
                startPlayback(audioRef, position)
                currentPlayingPosition = position
                currentSeekBar = seekBar // Update the current SeekBar reference
                updatePlaybackState(true)
            } else {
                // Same audio is clicked, toggle playback
                currentMediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                        updatePlaybackState(false)
                    } else {
                        player.start()
                        currentSeekBar = seekBar // Update the current SeekBar reference
                        updateSeekBar(seekBar)
                        updatePlaybackState(true)
                    }
                }
            }
        }

        private fun startPlayback(audioRef: String, position: Int) {
            currentMediaPlayer = MediaPlayer().apply {
                setDataSource(audioRef)
                prepareAsync()

                setOnPreparedListener {
                    val duration = it.duration // Get duration in milliseconds
                    val durationSeconds = duration / 1000 // Convert duration to seconds
                    // Update the timer text in the layout
                    binding.audioDurationText.text = formatDuration(durationSeconds)

                    start()
                    currentSeekBar = seekBar // Update the current SeekBar reference
                    updateSeekBar(seekBar)
                    updatePlaybackState(true) // Update playback state after starting playback
                }

                setOnCompletionListener {
                    releaseMediaPlayer() // Release MediaPlayer when playback completes
                    updatePlaybackState(false)
                    // Reset seekbar position
                    seekBar.progress = 0
                }
            }
            currentAudioRef = audioRef // Store the current audio reference
        }

        private fun updateSeekBar(seekBar: SeekBar) {
            val handler = Handler()
            handler.postDelayed(object : Runnable {
                override fun run() {
                    currentMediaPlayer?.let {
                        if (it.isPlaying && seekBar == currentSeekBar) { // Only update the current SeekBar
                            seekBar.max = it.duration
                            seekBar.progress = it.currentPosition
                        }
                    }
                    handler.postDelayed(this, 1000)
                }
            }, 0)
        }

        private fun releasePreviousMediaPlayer() {
            currentMediaPlayer?.apply {
                release()
            }
        }

        private fun formatDuration(durationSeconds: Int): String {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun getStatusText(status: Message.MessageStatus): String {
        return when (status) {
            Message.MessageStatus.SENT -> "Sent"
            Message.MessageStatus.DELIVERED -> "Delivered"
            Message.MessageStatus.SEEN -> "Seen"
            Message.MessageStatus.NOT_SENT -> "Not Sent"
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.apply {
            release()
            mediaPlayer = null
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.epochTimeMs == newItem.epochTimeMs
    }
}